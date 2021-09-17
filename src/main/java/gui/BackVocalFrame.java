package gui;

import core.PlayDateItem;
import core.Playlist;
import door.MainClass;
import fox.fb.FoxFontBuilder;
import fox.out.Out;
import registry.Codes;
import registry.Registry;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackVocalFrame extends JFrame implements WindowListener {
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static ExecutorService executor;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private static JPanel basePane, centerPlaylistsPane;
    private static JPanel playDatePane;
    private static JScrollPane playDateScroll, playListsScroll;
    private static JButton bindListBtn, clearBindBtn;
    private static JLabel nowPlayedLabel;
    private static JProgressBar playProgress;

    private static PlayDateItem[] dayItems = new PlayDateItem[7];
    private static String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private static Font headersFontSmall = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.HARLOW_S_I, 12, true);


    public BackVocalFrame() {
        loadUIM();

        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
//        String time = sdf.format(Instant.now().toEpochMilli());

        Out.Print("Build the frame...");

        setTitle("Back vocal studio v." + Registry.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 900));

        basePane = new JPanel(new BorderLayout(3,3)) {
            {
                setBackground(Color.darkGray);

                centerPlaylistsPane = new JPanel(new BorderLayout(3,3)) {
                    {
                        setBackground(Color.gray);
                    }
                };

                playListsScroll = new JScrollPane(centerPlaylistsPane) {
                    {
                        setBorder(null);
                        getVerticalScrollBar().setUnitIncrement(16);
                    }
                };

                JPanel rightShedulePane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setBackground(Color.darkGray);
                        setPreferredSize(new Dimension(400, 0));

                        playDatePane = new JPanel(new GridLayout(7, 1, 3,3)) {
                            {

                            }
                        };

                        playDateScroll = new JScrollPane(playDatePane) {
                            {
                                setBorder(null);
                                getViewport().setPreferredSize(new Dimension(350, 0));
                                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                getVerticalScrollBar().setUnitIncrement(8);
                            }
                        };

                        add(playDateScroll, BorderLayout.CENTER);
                    }
                };

                JPanel downBtnsPane = new JPanel(new FlowLayout(0, 3, 3)) {
                    {
                        bindListBtn = new JButton("Bind to dir") {
                            {
                                setEnabled(false);
                                setFocusPainted(false);
                                addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        Component[] comps = playDatePane.getComponents();
                                        PlayDateItem selectedItemList = null;
                                        for (Component comp : comps) {
                                            if (comp instanceof PlayDateItem) {
                                                if (((PlayDateItem) comp).isSelected()) {
                                                    selectedItemList = ((PlayDateItem) comp);
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        JFileChooser fch = new JFileChooser("./resources/audio/");
                                        fch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                        fch.setMultiSelectionEnabled(false);
                                        fch.setDialogTitle("Choose play-folder:");

                                        int result = fch.showOpenDialog(BackVocalFrame.this);
                                        // Если директория выбрана, покажем ее в сообщении
                                        if (result == JFileChooser.APPROVE_OPTION ) {
                                            Path plDir = Paths.get(fch.getSelectedFile().toURI());

                                            selectedItemList.setPlayList(plDir);
                                        }
                                    }
                                });
                            }
                        };

                        clearBindBtn = new JButton("Clear bind") {
                            {
                                setEnabled(false);
                                setFocusPainted(false);
                                addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this, "Clear current playlist?", "Confirm:", JOptionPane.OK_OPTION);
                                        if (req == 0) {
                                            Out.Print("Clearing the playlist " + getSelectedItem().getName());
                                            getSelectedItem().setPlayList(null);
                                        }
                                    }
                                });
                            }
                        };

                        playProgress = new JProgressBar(0, 0, 100) {
                            {
                                setPreferredSize(new Dimension(BackVocalFrame.this.getWidth() / 3, 30));
                            }
                        };

                        nowPlayedLabel = new JLabel("Now played: ");

                        add(bindListBtn);
                        add(clearBindBtn);
                        add(new JSeparator(1));
                        add(playProgress);
                        add(new JSeparator(1));
                        add(nowPlayedLabel);
                    }
                };

                add(playListsScroll, BorderLayout.CENTER);
                add(rightShedulePane, BorderLayout.EAST);
                add(downBtnsPane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        addWindowListener(this);

        Out.Print("Show the frame...");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        loadTracksDB();

        try {
            executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                while (true) {
                    for (PlayDateItem weakdayItem : getWeakdayItems()) {
                        if (weakdayItem.isPlayed()) {
                            if (!weakdayItem.inSchedulingTimeAccept()) {
                                weakdayItem.stop();
                                JOptionPane.showConfirmDialog(BackVocalFrame.this, "Timer out! Music has stopped.", "Timer out:", JOptionPane.DEFAULT_OPTION);
                            }
                        }
                    }

                    try {Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
//            executor.shutdown(); //shutdown executor
//            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
//                System.out.println("AWAIT");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        repaint();
    }

    public static void resetRightPaneSelect() {
        Component[] comps = playDatePane.getComponents();
        for (Component comp : comps) {
            if (comp instanceof PlayDateItem) {
                ((PlayDateItem) comp).setSelected(false);
            }
        }

    }

    public static void showPlayList(Playlist playlist) {
        if (playlist == null) {
            centerPlaylistsPane.removeAll();
            playListsScroll.repaint();
            playListsScroll.revalidate();
            return;
        }

        centerPlaylistsPane.removeAll();
        centerPlaylistsPane.add(new JLabel(playlist.getName()) {{setBorder(new EmptyBorder(3, 6, 0, 0)); setFont(headersFontSmall);}}, BorderLayout.NORTH);
        centerPlaylistsPane.add(playlist, BorderLayout.CENTER);

        System.out.println("Added playlist named " + playlist.getName());
        playListsScroll.repaint();
        playListsScroll.revalidate();
    }

    public static ArrayList<PlayDateItem> getWeakdayItems() {
        ArrayList<PlayDateItem> result = new ArrayList<>();

        for (Component comp : playDatePane.getComponents()) {
            if (comp instanceof PlayDateItem) {
                result.add((PlayDateItem) comp);
            }
        }

        return result;
    }

    public static void updatePlayedLabelText() {
        List<PlayDateItem> played = getSoundedItems();
        String mes = "<html>Playing: ";
        for (PlayDateItem playItem : played) {
            mes += "<b>" + playItem.getName() + "</b>: '" + playItem.getActiveTrackName() + "' ";
        }

        nowPlayedLabel.setText(mes);
    }

    private PlayDateItem getSelectedItem() {
        Component[] comps = playDatePane.getComponents();
        for (Component comp : comps) {
            if (comp instanceof PlayDateItem) {
                if (((PlayDateItem) comp).isSelected()) {
                    return ((PlayDateItem) comp);
                }
            }
        }

        return null;
    }

    private static List<PlayDateItem> getSoundedItems() {
        List<PlayDateItem> result = new ArrayList<>();

        Component[] comps = playDatePane.getComponents();
        for (Component comp : comps) {
            if (comp instanceof PlayDateItem) {
                if (((PlayDateItem) comp).isPlayed()) {
                    result.add((PlayDateItem) comp);
                }
            }
        }

        return result;
    }

    public static void enableControls(boolean enable) {
        bindListBtn.setEnabled(enable);
        clearBindBtn.setEnabled(enable);
    }

    private void loadUIM() {
        Out.Print("Set the UIManagers view.");

        try {UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
//                e2.printStackTrace();
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, Arrays.stream(e2.getStackTrace()).toArray());
            }
        }
    }

    private void stateChanged() {
//        if (trayIcon != null) {
//            trayIcon.setImage(updatedImage);
//        }
    }


    private static void loadTracksDB() {
        String dayString;
        int counter = 0;
        for (String day : days) {
            Out.Print("Try to load the day '" + day + "'...");

            try {
                dayString = Files.readString(Paths.get("./resources/scheduler/" + day + ".db"), StandardCharsets.UTF_8);
                String[] data = dayString.split("NN_");

                System.out.println("Date in: " + data[1]);
                System.out.println(data[1].split("_EE")[1]);

                dayItems[counter] = new PlayDateItem(
                        day,
                        Paths.get(data[4].split("_EE")[1]),
                        data[1].split("_EE")[1],
                        data[2].split("_EE")[1],
                        data[3].split("_EE")[1],
                        Boolean.parseBoolean(data[5].split("_EE")[1]));
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException aibe) {
                aibe.printStackTrace();
            } catch (MalformedInputException mie) {
                mie.printStackTrace();
            } catch (NoSuchFileException fnf) {
                Out.Print("PlayList for " + day + " is not exist.", Out.LEVEL.WARN);
                dayItems[counter] =
                        new PlayDateItem(
                                day,
                                Paths.get("null"),
                                "00:00:00", "23:59:59", "00:00:00",
                                true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                playDatePane.add(dayItems[counter]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter++;
        }
    }

    private static void saveTracksDB() {
        ArrayList<PlayDateItem> wdItems = getWeakdayItems();
        for (PlayDateItem wdItem : wdItems) {
            wdItem.saveToFile();
        }
    }

    public static void setProgress(int prog) {
        if (prog < 0) {prog = 0;} else if (prog > 100) {prog = 100;}
        playProgress.setValue(prog);
    }

    private void tray() throws AWTException {
        Out.Print("Traying the frame...");
        tray.add(trayIcon);
        trayIcon.displayMessage("BVS", "Плеер работает в фоновом режиме", TrayIcon.MessageType.INFO);
    }

    private void detray() {
        Out.Print("De-Traying the frame...");
        BackVocalFrame.this.setVisible(true);
        BackVocalFrame.this.setState(JFrame.NORMAL);
        tray.remove(trayIcon);
    }


    // Listeners:
    @Override
    public void windowOpened(WindowEvent e) {
        System.out.println("Opening the frame...");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this, "Are You sure?..", "Exit?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
        if (req == 0) {
            executor.shutdownNow();
            saveTracksDB();
            BackVocalFrame.this.dispose();
            MainClass.exit(Codes.OLL_OK);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        Out.Print("Closing the frame...");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("./resources/tray.png");

            ActionListener listener = e1 -> {detray();};

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Test");
            defaultItem.addActionListener(listener);
            popup.add(defaultItem);

            MenuItem close = new MenuItem("Close");
            close.addActionListener(e12 -> MainClass.exit(Codes.OLL_OK));
            popup.add(close);

            trayIcon = new TrayIcon(image, "Tray Demo", popup);
            trayIcon.addActionListener(listener);
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
}
