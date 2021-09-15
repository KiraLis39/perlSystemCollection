package fox.out;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public class Out {
	public static enum LEVEL {DEBUG, INFO, ACCENT, WARN, ERROR, CRITICAL}
	private static LEVEL errLevel = LEVEL.DEBUG; // уровень, от которого сообщения будут обрабатываться
	
	private final static Charset charset = StandardCharsets.UTF_8;
	
	private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	private final static SimpleDateFormat fnc = new SimpleDateFormat("HH.mm.ss");
	
	private static Deque<LEVEL> typeDeque = new ArrayDeque<LEVEL>();
	private static Stack<String> messageStack = new Stack<String>();
	private static File HTMLdir = new File("./log/"), HTMLlog;
	
	private static Thread LogThread;
	
	private static boolean free = true, enabled = true, thanNextLine = false;

	private static String currentDate, currentTime, address;
	
	private static int logCount = 0; // счетчик сообщений в логе
	private static int sleepTime = 250;
	private static int logsCountAllow = 10;	// сколько логов хранить по-умолчанию

	
 	private Out() {}
 	
 	private static void start() {
 		if (LogThread == null) {
			LogThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Print("INFO: Out", LEVEL.INFO, Thread.currentThread().getName() + " is started with write-level " + errLevel.name() + " (#" + errLevel.ordinal() + ")");
					if (!checkFiles()) {throw new RuntimeException("ERR: Out: Files creating is fail!");}
					
					while (enabled || !Thread.currentThread().isInterrupted()) {
						if (messageStack.isEmpty()) {
							try {Thread.sleep(sleepTime);
							} catch (InterruptedException ie) {Thread.currentThread().interrupt();}
						} else {
							if (free) {
								free = false;
								
								if (messageStack.size() > 30) {LogThread.setPriority(Thread.MAX_PRIORITY);
								} else if (messageStack.size() > 15) {LogThread.setPriority(Thread.NORM_PRIORITY);
								} else {LogThread.setPriority(Thread.MIN_PRIORITY);}
								
								if (typeDeque.size() != messageStack.size()) {System.err.println("WARN: Out messageArray has size: " + messageStack.size() + ", but typeDeque`s size: " + typeDeque.size());}
								
								logHTML();
							}

							try {Thread.sleep(sleepTime);} catch (InterruptedException ie) {Thread.currentThread().interrupt();
							} catch (Exception e) {e.printStackTrace();}
						}
					}
					
					Print("INFO: Out", LEVEL.ACCENT, Thread.currentThread().getName() + " was stopped.");
				}
			})

			{
				{
					setName("FoxLib39: OutLogThread");
					setPriority(Thread.MIN_PRIORITY);
					setDaemon(true);
					start();
				}
			};
		}
 	}
	
	private static boolean checkFiles() {
		// Пытаемся максимум три раза создать необходимую директорию логов:
		int tryes = 3;
		do {
			tryes--;
			HTMLdir.mkdirs();
		} while (!HTMLdir.exists() && tryes > 0);
		if (!HTMLdir.exists()) {throw new RuntimeException("The HTMLdir '" + HTMLdir + "' can`t created!");}
		
		// удаляем лишние файлы, если их больше, чем разрешено хранить:
		logsCleaner();

		// Готовим новый лог-файл:
		String timeData = fnc.format(System.currentTimeMillis());		
		currentDate = sdf.format(System.currentTimeMillis());
		HTMLlog = new File(HTMLdir.getPath() + "/" + timeData + " log.html");
		
		// Пытаемся максимум три раза создать и открыть лог-файл:
		tryes = 3;
		while (!HTMLlog.exists() && tryes > 0) {
			tryes--;			
			try {
				HTMLlog.createNewFile();
				openLogFile();
			} catch (IOException e) {
				System.err.println("ERR: Out: checkFiles: Creating or opening \"" + HTMLlog + "\" is FAILED!");
				e.printStackTrace();
			}
		}
		if (!HTMLlog.exists()) {throw new RuntimeException("The HTMLlog '" + HTMLlog + "' can`t created!");}
		
		return true;
	}
		
	private static void logsCleaner() {
		File[] logsCount = HTMLdir.listFiles();
		if (logsCount.length >= logsCountAllow) {
			for (int i = 0; i < logsCount.length - (logsCountAllow - 1); i++) {logsCount[i].delete();}
		}
		logsCount = null;
	}

	private static void openLogFile() {
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(HTMLlog, true), charset)) {
			osw.write("<!DOCTYPE html><HTML lang=\"ru\"><HEAD><meta charset=\"UTF-8\"><title>" + currentDate + "</title></HEAD><BODY>");
		} catch (Exception e) {e.printStackTrace();}
	}
	
	// вывод в файл лога:
	private synchronized static void logHTML() {
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(HTMLlog, true), charset)) {
			currentTime = fnc.format(System.currentTimeMillis());
			address = messageStack.pop();

			osw.write(
					"<p style='font-family:arial,fixedsys,consolas;font-size:10px;font color=#000'>" + currentTime + "<br>" + 
					"<font style='font-family:consolas,arial,garamond;font-size:12px;'>");
			
			switch (typeDeque.pollLast()) {
				case DEBUG: osw.write("<font color='#666'>" + logCount + ") " + address);
					break;	
				case INFO: osw.write("<font color='#000'>" + logCount + ") " + address);
					break;	
				case ACCENT: osw.write("<font color='#0094de'>" + logCount + ") " + address);
					break;						
				case WARN: osw.write("<font color='#e0a800'>" + logCount + ") " + address);
					break;						
				case ERROR: osw.write("<font color='#bf4c28'><h3>" + logCount + ") " + address + "</h>");
					break;						
				default:	osw.write("<font color='#ff0000'><h2>" + logCount + ") " + address + "</h>"); // CRITICAL
			}
			
			osw.write("</p></font>\n");
		} catch (Exception de) {System.err.println(de.getMessage());
		} finally {
			logCount++;
			free = true;
		}
	}
	
	// базовая печать сообщений в консоль (до вывода в файл лога):
	public synchronized static void Print(String message) {Print(Out.class, LEVEL.INFO, message, Thread.currentThread());}	
	public synchronized static void Print(Class<?> clazz, Exception e) {Print(clazz, LEVEL.ERROR, e.getStackTrace());}
	public synchronized static void Print(Class<?> clazz, LEVEL level, Exception e) {Print(clazz, level, e.getStackTrace());}	
	public synchronized static void Print(Class<?> clazz, LEVEL level, Object[] messages) {Print(clazz.getName(), level, messages);}	
	public synchronized static void Print(Class<?> clazz, LEVEL level, String message) {Print(clazz, level, message, Thread.currentThread());}
	public synchronized static void Print(Class<?> clazz, LEVEL level, String message, Thread srcThread) {Print(clazz.getName() + " -> " + srcThread.getStackTrace()[1].getMethodName(), level, message);}
	public synchronized static void Print(String address, LEVEL level, Object[] messages) {
		for (int i = 0; i < messages.length; i++) {Print(address, level, messages[i].toString());}
	}
	
	private synchronized static void Print(String address, LEVEL level, String message) {
		if (level == LEVEL.CRITICAL) {throw new RuntimeException("!!! CRITICAL ERROR !!!\n" + address + ": " + message);}
		
		if (isEnabled()) {
			if (message.startsWith("\n")) {
				System.out.println();
				message = message.substring(1, message.length());
			}
			if (message.endsWith("\n")) {thanNextLine = true;}
			
			String resultString;
			switch (level) {
				case ERROR:		resultString = "[ERROR]\t" + address + ": " + message;
					break;
					
				case WARN: 		resultString = "[WARN]\t" + address + ": " + message;
					break;
					
				case ACCENT:	resultString = "[ATTENTION]\t" + address + ": " + message;
					break;
				
				case INFO:		resultString = "[INFO]\t" + address + ": " + message;
					break;
					
				case DEBUG: 
				default:		resultString = "[DEBUG]\t" + address + ": " + message;
			}

			if (thanNextLine) {
				System.out.println(); 
				thanNextLine = false;
			}
			
			if (LogThread == null) {start();}
			if (level.ordinal() >= errLevel.ordinal()) {
				typeDeque.addFirst(level);
				messageStack.insertElementAt(address + ": " + message, 0);
				
				if (level == LEVEL.ERROR || level == LEVEL.WARN) {System.err.println(resultString);				
				} else {System.out.println(resultString);}
			}
		}
	}

	// изменение папки логов по-умолчанию:
	public static String getLogPath() {return HTMLdir.getPath();}
	public static File getLogFile() {return HTMLlog;}

	// сколько максимально файлов лога хранить:
	public static int getLogsCoutAllow() {return logsCountAllow;}
	public static void setLogsCoutAllow(int _logsCountAllow) {logsCountAllow = _logsCountAllow;}
	
	// от какого и выше уровня обрабатывать сообщения:
	public static void setErrorLevel(LEVEL lvl) {errLevel = lvl;}
	public static LEVEL getErrorLevel() {return errLevel;}

	public static boolean isEnabled() {return enabled;}
	public static void setEnabled(boolean d) {
		if (enabled == d) {return;}
		enabled = d;
		
		if (LogThread == null || !LogThread.isAlive()) {start();}
	}
}
