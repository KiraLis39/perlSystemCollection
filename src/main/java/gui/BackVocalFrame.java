package gui;

import fox.components.AlarmItem;
import fox.components.PlayPane;
import door.MainClass;
import fox.fb.FoxFontBuilder;
import fox.out.Out;
import registry.Codes;
import registry.Registry;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicToolBarUI;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.awt.event.*;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BackVocalFrame extends JFrame implements WindowListener, ComponentListener {
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static ExecutorService executor;

    private static BackVocalFrame frame;
    private static JPanel basePane, centerPlaylistsPane, playDatePane, downBtnsPane, downShedulePane;
    private static JScrollPane playDateScroll, playListsScroll;
    private static JButton bindListBtn, clearBindBtn, moveUpBtn, moveDownBtn, removeBtn, addTrackBtn;
    private static JLabel nowPlayedLabel;
    private static JProgressBar playProgress;
    private static JFileChooser fch = new JFileChooser("./resources/audio/");
    private static JToolBar toolBar;

    private static PlayDataItem[] dayItems = new PlayDataItem[7];
    private static String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private static Font headersFontSmall = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private static Font btnsFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, false);
    private static Font btnsFont2 = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);

    private static int daysCounter = 0;
    private SimpleDateFormat weakday = new SimpleDateFormat("EEEE", Locale.US);


    public BackVocalFrame() {
        frame = this;
        Out.Print("Build the frame...");

        try {setIconImage(new ImageIcon(ImageIO.read(new File("./resources/icons/0.png"))).getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Back vocal studio v." + Registry.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        basePane = new JPanel(new BorderLayout(3,3)) {
            {
                setBackground(Color.BLACK);

                centerPlaylistsPane = new JPanel(new BorderLayout(3,3)) {
                    {
                        setOpaque(false);
                    }
                };

                playListsScroll = new JScrollPane(centerPlaylistsPane) {
                    {
                        setBorder(null);
                        getViewport().setBorder(null);
                        getViewport().setBackground(Color.BLACK);
                        setBackground(Color.BLACK);
                        getViewport().setForeground(Color.WHITE);
                        setOpaque(false);
                        getVerticalScrollBar().setUnitIncrement(18);
                        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    }
                };

                downShedulePane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setBackground(Color.BLACK);

                        toolBar = new JToolBar("Still draggable") {
                            {
                                setBorder(new EmptyBorder(0,0,1,0));

                                moveUpBtn = new JButton("Move it Up") {
                                    {
                                        setBackground(Color.RED);
                                        setForeground(Color.BLUE);
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                getSelectedItem().moveSelectedUp();
                                            }
                                        });
                                    }
                                };

                                addTrackBtn = new JButton("Add track") {
                                    {
                                        setForeground(Color.GREEN);
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                fch.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                                fch.setMultiSelectionEnabled(true);
                                                fch.setDialogTitle("Choose tracks:");

                                                int result = fch.showOpenDialog(BackVocalFrame.this);
                                                if (result == JFileChooser.APPROVE_OPTION ) {
                                                    getSelectedItem().getPlayPane().setTracks(fch.getSelectedFiles());
                                                } else {
                                                    System.out.println("Dir was not chousen...");
                                                }
                                            }
                                        });
                                    }
                                };

                                removeBtn = new JButton("Remove track") {
                                    {
                                        setForeground(Color.RED);
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                if (getSelectedItem().getPlayPane().getSelectedIndex() == -1) {return;}

                                                int req = JOptionPane.showConfirmDialog(null,
                                                        "Delete track #" + (getSelectedItem().getPlayPane().getSelectedIndex() + 1) + "?",
                                                        "Sure?", JOptionPane.WARNING_MESSAGE);

                                                if (req == 0) {
                                                    getSelectedItem().removeSelected();
                                                }
                                            }
                                        });
                                    }
                                };

                                moveDownBtn = new JButton("Move it Down") {
                                    {
                                        setForeground(Color.BLUE);
                                        setFont(btnsFont2);
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                getSelectedItem().moveSelectedDown();
                                            }
                                        });
                                    }
                                };

                                add(moveUpBtn);
                                add(new JSeparator());
                                add(addTrackBtn);
                                add(removeBtn);
                                add(new JSeparator());
                                add(moveDownBtn);
                            }
                        };

                        playDatePane = new JPanel(new GridLayout(1, 7, 1,0)) {
                            {
                                setBackground(Color.BLACK);
                                setBorder(null);
                            }
                        };

                        playDateScroll = new JScrollPane(playDatePane) {
                            {
                                setBorder(null);
                                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                                getVerticalScrollBar().setUnitIncrement(16);
                            }
                        };

                        JPanel downPane = new JPanel(new BorderLayout()) {
                            {
                                setOpaque(false);
                                setBorder(new EmptyBorder(1,0,0,0));

                                downBtnsPane = new JPanel(new FlowLayout(0, 3, 3)) {
                                    {
                                        setBackground(Color.DARK_GRAY);
                                        setBorder(new EmptyBorder(0,0,1,0));

                                        bindListBtn = new JButton("Bind to dir") {
                                            {
                                                setFont(btnsFont);
                                                setEnabled(false);
                                                setFocusPainted(false);
                                                setBackground(new Color(0.3f, 0.5f, 0.2f, 1.0f));
                                                setForeground(Color.BLACK);
                                                addActionListener(new ActionListener() {
                                                    @Override
                                                    public void actionPerformed(ActionEvent e) {
                                                        int req = JOptionPane.showConfirmDialog(null,
                                                                "Rebuild the playlist?", "Sure?", JOptionPane.WARNING_MESSAGE);

                                                        if (req == 0) {
                                                            fch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                                            fch.setMultiSelectionEnabled(false);
                                                            fch.setDialogTitle("Choose folder:");

                                                            int result = fch.showOpenDialog(BackVocalFrame.this);
                                                            // Если директория выбрана, покажем ее в сообщении
                                                            if (result == JFileChooser.APPROVE_OPTION) {
                                                                System.out.println("Chousen dir: " + fch.getSelectedFile());
                                                                getSelectedItem().getPlayPane().clearTracks();
                                                                getSelectedItem().getPlayPane().setTracks(fch.getSelectedFile());
                                                            } else {
                                                                System.out.println("Dir was not chousen...");
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        };

                                        clearBindBtn = new JButton("Clear bind") {
                                            {
                                                setFont(btnsFont);
                                                setEnabled(false);
                                                setFocusPainted(false);
                                                setBackground(new Color(0.5f, 0.2f, 0.2f, 1.0f));
                                                setForeground(Color.BLACK);
                                                addActionListener(new ActionListener() {
                                                    @Override
                                                    public void actionPerformed(ActionEvent e) {
                                                        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this,
                                                                "Clear current playlist?", "Confirm:", JOptionPane.OK_OPTION);
                                                        if (req == 0) {
                                                            Out.Print("Clearing the playlist " + getSelectedItem().getName());
                                                            getSelectedItem().getPlayPane().clearTracks();
                                                        }
                                                    }
                                                });
                                            }
                                        };

                                        playProgress = new JProgressBar(0, 0, 100) {
                                            {
                                                setFont(btnsFont);
                                                setStringPainted(true);
                                            }
                                        };

                                        nowPlayedLabel = new JLabel() {
                                            {
                                                setFont(headersFontSmall);
                                                setForeground(Color.WHITE);
                                            }
                                        };

                                        add(bindListBtn);
                                        add(clearBindBtn);
                                        add(new JSeparator(1));
                                        add(playProgress);
                                        add(new JSeparator(1));
                                        add(nowPlayedLabel);
                                    }
                                };

                                add(downBtnsPane);
                            }
                        };

                        add(toolBar, BorderLayout.NORTH);
                        add(playDateScroll, BorderLayout.CENTER);
                        add(downPane, BorderLayout.SOUTH);
                    }
                };

                add(playListsScroll, BorderLayout.CENTER);
                add(downShedulePane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        addWindowListener(this);
        addComponentListener(this);

        Out.Print("Show the frame...");
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        loadDays();

        setMinimumSize(new Dimension(dayItems[0].getWidth() * 7 + 48, 700));
        setLocationRelativeTo(null);
        repaint();
        playProgress.setPreferredSize(new Dimension(frame.getWidth() / 3, 27));

        try {
            Out.Print("Starting the Executors...");

            executor = Executors.newFixedThreadPool(2);
            executor.execute(() -> {
                String today = weakday.format(new Date());

                while (true) {
                    try {
                        for (PlayDataItem weakdayItem : getWeekdayItems()) {
                            if (!weakdayItem.getName().equalsIgnoreCase(today)) {continue;}

                            if (!weakdayItem.inSchedulingTimeAccept()) {
                                if (weakdayItem.isPlayed()) {
                                    weakdayItem.stop();
                                    JOptionPane.showConfirmDialog(BackVocalFrame.this, "Timer out! Music has stopped.", "Timer out:", JOptionPane.DEFAULT_OPTION);
                                }
                            } else {
                                if (weakdayItem.getPlayPane().isEmpty()) {continue;}

                                if (!weakdayItem.isPlayed() && !weakdayItem.isPaused() && !weakdayItem.isHandStopped()) {
                                    weakdayItem.play();
                                    weakdayItem.setSelected(true);
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            executor.execute(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss (zZ)");
                sdf.setTimeZone(TimeZone.getTimeZone("+0"));
                Out.Print("== Launch time is: <" + sdf.format(System.currentTimeMillis() - MainClass.getStartTime()) + "> ==");

                while (true) {
                    try {
                        for (PlayDataItem weakdayItem : getWeekdayItems()) {
                            String time;
                            ArrayList<AlarmItem> ail = weakdayItem.getAlarmData();
                            for (AlarmItem s : ail) {
                                if (s.isWasPlayed()) {continue;}

                                time = s.getTime();
                                if (weakdayItem.isTimeCome(time)) {
                                    weakdayItem.pause();
                                    weakdayItem.playAlarm(s.getTrack());
                                    s.wasPlayed(true);
                                    while (weakdayItem.alarmThreadIsAlive()) {
                                        Thread.yield();
                                    }
                                    weakdayItem.resume();
                                }
                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Out.Print("Play control executor was interrupted.");
                        Thread.currentThread().interrupt();
                    }
                }
            });
            executor.shutdown(); //shutdown executor
//            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
//                System.out.println("AWAIT");
//            }
        } catch (Exception e) {
            Out.Print("Executor loading exception: " + e.getMessage());
        }
    }

    public static void resetDownPaneSelect() {
        Out.Print("Reset frame panels selections...");
        for (PlayDataItem comp : getWeekdayItems()) {
            comp.setSelected(false);
        }
    }

    public static void showPlayList(PlayPane playpane) {
        centerPlaylistsPane.removeAll();

        if (playpane != null) {
            centerPlaylistsPane.add(new JLabel(playpane.getName() + "`s playlist:") {{
                setBorder(new EmptyBorder(3, 6, 0, 0));
                setFont(headersFontSmall);
                setForeground(Color.WHITE);
            }}, BorderLayout.NORTH);
            centerPlaylistsPane.add(playpane, BorderLayout.CENTER);

            Out.Print("The playlist named '" + playpane.getName() + "' was added to CENTER.");
        }

        playListsScroll.repaint();
        playListsScroll.revalidate();
    }

    public static ArrayList<PlayDataItem> getWeekdayItems() {
        ArrayList<PlayDataItem> result = new ArrayList<>(7);

        for (Component comp : playDatePane.getComponents()) {
            if (comp instanceof PlayDataItem) {
                result.add((PlayDataItem) comp);
            }
        }

        return result;
    }

    public static void updatePlayedLabelText() {
        new Thread(() -> {
            try {Thread.sleep(250);
            } catch (InterruptedException e) {/* IGNORE */}

            List<PlayDataItem> played = getSoundedItems();
            String mes = "<html>Playing: ";
            for (PlayDataItem playItem : played) {
                mes += "<b color='YELLOW'>" + playItem.getName() + ":</b> '" + playItem.getActiveTrackName() + "' ";
            }

            nowPlayedLabel.setText(mes);
            setProgress(100 / getSelectedItem().getPlayPane().getRowsCount() * (getSelectedItem().getIndexOfPlayed() + 1));
            centerPlaylistsPane.repaint();
        }).start();
    }

    public static void setPlayedLabelText(String mes) {
        new Thread(() -> {
            try {Thread.sleep(250);
            } catch (InterruptedException e) {/* IGNORE */}

            nowPlayedLabel.setText(mes);
            centerPlaylistsPane.repaint();
        }).start();
    }

    public static JFrame getFrame() {return frame;}

    public static PlayDataItem getSelectedItem() {
        for (PlayDataItem comp : getWeekdayItems()) {
            if (comp.isSelected()) {
                return comp;
            }
        }
        return null;
    }

    private static List<PlayDataItem> getSoundedItems() {
        List<PlayDataItem> result = new ArrayList<>();

        for (PlayDataItem comp : getWeekdayItems()) {
            if (comp.isPlayed()) {
                result.add(comp);
            }
        }

        return result;
    }

    public static void enableControls(boolean enable) {
        toolBar.setVisible(enable);
        bindListBtn.setEnabled(enable);
        clearBindBtn.setEnabled(enable);
        if (!enable) {
            centerPlaylistsPane.removeAll();
            centerPlaylistsPane.repaint();
        }
    }

    private void stateChanged() {
//        if (trayIcon != null) {
//            trayIcon.setImage(updatedImage);
//        }
    }

    private static void loadDays() {
        playListsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        toolBar.setVisible(false);

        playProgress.setString("Load media...");
        playProgress.setIndeterminate(true);

        for (String day : days) {
            Out.Print("\nTry to load the day '" + day + "'...");

            try {
                // META loading:
                String meta = Files.readString(Paths.get("./resources/scheduler/" + day + ".meta"), StandardCharsets.UTF_8);
                String[] data = meta.split("NN_");

                dayItems[daysCounter] = new PlayDataItem(
                        day,
                        data[1].split("_EE")[1],
                        data[2].split("_EE")[1],
                        Boolean.parseBoolean(data[3].split("_EE")[1]));


                // ALARMS loading:
                List<String> alarms = Files.lines(Paths.get("./resources/scheduler/" + day + ".alarms"), StandardCharsets.UTF_8).collect(Collectors.toList());
                for (String alarm : alarms) {
                    try {
                        String time = alarm.split(">")[0];
                        Path track = Paths.get(alarm.split(">")[1]);

                        if (Files.notExists(track)) {
                            Out.Print("Alarm file not exist:", Out.LEVEL.WARN);
                        } else {
                            if (time.length() == 8) {
                                dayItems[daysCounter].addAlarm(time, track);
                            } else {
                                Out.Print("Time is not correct: " + time, Out.LEVEL.WARN);
                            }
                        }
                    } catch (Exception e) {
                        Out.Print("Alarms loading exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }


                // LIST loading:
                List<String> tracks = Files.lines(Paths.get("./resources/scheduler/" + day + ".list"), StandardCharsets.UTF_8).collect(Collectors.toList());
                for (String track : tracks) {
                    try {
                        dayItems[daysCounter].addTrack(Paths.get(track));
                    } catch (Exception e) {
                        if (Files.notExists(Paths.get(track))) {
                            Out.Print("Track not exist:", Out.LEVEL.WARN, e.getStackTrace());
                        } else {
                            Out.Print("Unknown err:", Out.LEVEL.ERROR, e.getStackTrace());
                        }
                    }
                }

            } catch (IllegalArgumentException iae) {
                Out.Print("Err:", Out.LEVEL.WARN, iae.getStackTrace());
                iae.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException aibe) {
                Out.Print("Err:", Out.LEVEL.WARN, aibe.getStackTrace());
                aibe.printStackTrace();
            } catch (MalformedInputException mie) {
                Out.Print("Err:", Out.LEVEL.WARN, mie.getStackTrace());
                mie.printStackTrace();
            } catch (NoSuchFileException fnf) {
                Out.Print("PlayList for " + day + " is not exist.", Out.LEVEL.WARN);
                dayItems[daysCounter] =
                        new PlayDataItem(
                                day,
                                "12:00:00", "12:00:00",
                                true);
            } catch (Exception e) {
                Out.Print("Meta loading err:", Out.LEVEL.WARN, e.getStackTrace());
                e.printStackTrace();
            }

            try {playDatePane.add(dayItems[daysCounter]);
            } catch (Exception e) {
                Out.Print("Is playDatePane`s add err: " + e.getMessage(), Out.LEVEL.ERROR, e.getStackTrace());
                e.printStackTrace();
            }

            daysCounter++;
        }

        Out.Print("Loading tracks accomplished.");
        resetDownPaneSelect();

        toolBar.setVisible(true);

        playProgress.setString(null);
        playProgress.setIndeterminate(false);
        playListsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }


    private static void setProgress(int prog) {
        if (prog < 0) {prog = 0;} else if (prog > 100) {prog = 100;}
        playProgress.setValue(prog);
    }

    private void tray() throws AWTException {
        Out.Print("Tray the frame...");

        frame.dispose();
        tray.add(trayIcon);
        trayIcon.displayMessage("BVS", "Плеер работает в фоновом режиме", TrayIcon.MessageType.INFO);
    }

    private void detray() {
        Out.Print("De-Tray the frame...");
        BackVocalFrame.this.setVisible(true);
        BackVocalFrame.this.setState(JFrame.NORMAL);
        tray.remove(trayIcon);
    }

    private void exit() {
        executor.shutdownNow();

        // saving days:
        for (PlayDataItem wdItem : getWeekdayItems()) {
            wdItem.saveToFile();
        }

        BackVocalFrame.this.dispose();
        MainClass.exit(Codes.OLL_OK);
    }


    // Listeners:
    @Override
    public void windowOpened(WindowEvent e) {
    	Out.Print("The frame is open now.");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this,
                "Are You sure?..", "Save and exit?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
        if (req == 0) {
            exit();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        Out.Print("Frame is closed now.");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();

            PopupMenu popup = new PopupMenu() {
                {
                    MenuItem defaultItem = new MenuItem("Show the frame");
                    defaultItem.addActionListener(e13 -> detray());

                    MenuItem close = new MenuItem("Exit");
                    close.addActionListener(e12 -> exit());

                    add(defaultItem);
                    add(close);
                }
            };




            trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("./resources/icons/0.png"), "BVF", popup);
            trayIcon.addActionListener(e1 -> detray());
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("BackVocalStudio");

            try {tray();
            } catch (AWTException awtException) {
                awtException.printStackTrace();
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, Arrays.stream(awtException.getStackTrace()).toArray());
            }
        }
    }

    @Override
    public void windowDeiconified(WindowEvent e) {detray();}

    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    @Override
    public void componentResized(ComponentEvent e) {
//        resizeReq = true;
//        downShedulePane.setPreferredSize(new Dimension(0, (int) (frame.getHeight() / 3f)));
//        downShedulePane.revalidate();
//        downShedulePane.repaint();
//        frame.revalidate();
//        frame.repaint();
    }

    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
}
