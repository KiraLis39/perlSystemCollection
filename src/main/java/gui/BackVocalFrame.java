package gui;

import core.PlayDateItem;
import core.Playlist;
import door.MainClass;
import fox.components.ListItem;
import fox.components.MyCellRenderer;
import fox.fb.FoxFontBuilder;
import fox.out.Out;
import registry.Codes;
import registry.Registry;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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

    private static JPanel basePane, centerPlaylistsPane;
    private static JPanel playDatePane;
    private static JScrollPane playDateScroll, playListsScroll;
    private static JButton bindListBtn, clearBindBtn;
    private static JLabel nowPlayedLabel;
    private static JProgressBar playProgress;

    private static PlayDateItem[] dayItems = new PlayDateItem[7];
    private static String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private static Font headersFontSmall = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private static BackVocalFrame frame;

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
                        getVerticalScrollBar().setUnitIncrement(16);
                    }
                };

                JPanel downShedulePane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setBackground(Color.darkGray);
//                        setPreferredSize(new Dimension(400, 0));

                        playDatePane = new JPanel(new GridLayout(1, 0, 3,0)) {
                            {
                                setBackground(Color.BLACK);
                            }
                        };

                        playDateScroll = new JScrollPane(playDatePane) {
                            {
                                setBorder(null);
                                getViewport().setPreferredSize(new Dimension(BackVocalFrame.this.getWidth(), 160));
//                                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                getVerticalScrollBar().setUnitIncrement(8);
                            }
                        };

                        JPanel downBtnsPane = new JPanel(new FlowLayout(0, 3, 3)) {
                            {
                                setOpaque(false);

                                bindListBtn = new JButton("Bind to dir") {
                                    {
                                        setEnabled(false);
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
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
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
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

                                    }
                                };

                                nowPlayedLabel = new JLabel("Now played: ") {
                                    {
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

                        add(playDateScroll, BorderLayout.CENTER);
                        add(downBtnsPane, BorderLayout.SOUTH);
                    }
                };

                add(playListsScroll, BorderLayout.CENTER);
                add(downShedulePane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        addWindowListener(this);

        loadTracksDB();
        Out.Print("Show the frame...");
        pack();
        setVisible(true);
        setMinimumSize(new Dimension(dayItems[0].getWidth() * 7 + 48, 700));
        setLocationRelativeTo(null);
        playProgress.setPreferredSize(new Dimension(frame.getWidth() / 3, 30));

        try {
            Out.Print("Starting the Executor...");
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
            Out.Print("Executor loading exception: " + e.getMessage());
        }

        repaint();
    }

    public static void resetDownPaneSelect() {
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
        centerPlaylistsPane.add(new JLabel(playlist.getName() + "`s playlist:") {{
            setBorder(new EmptyBorder(3, 6, 0, 0));
            setFont(headersFontSmall);
            setForeground(Color.WHITE);
        }}, BorderLayout.NORTH);
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

    public static JFrame getFrame() {return frame;}

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

    private void stateChanged() {
//        if (trayIcon != null) {
//            trayIcon.setImage(updatedImage);
//        }
    }


    private static void loadTracksDB() {
        Out.Print("Loading the tracks...");

        String dayString;
        int counter = 0;
        for (String day : days) {
            Out.Print("Try to load the day '" + day + "'...");

            try {
                dayString = Files.readString(Paths.get("./resources/scheduler/" + day + ".db"), StandardCharsets.UTF_8);
                String[] data = dayString.split("NN_");

                Out.Print("Date in: " + data[1].split("_EE")[1]);

                dayItems[counter] = new PlayDateItem(
                        day,
                        Paths.get(data[4].split("_EE")[1]),
                        data[1].split("_EE")[1],
                        data[2].split("_EE")[1],
                        data[3].split("_EE")[1],
                        Boolean.parseBoolean(data[5].split("_EE")[1]));
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
                dayItems[counter] =
                        new PlayDateItem(
                                day,
                                Paths.get("null"),
                                "00:00:00", "23:59:59", "00:00:00",
                                true);
            } catch (Exception e) {
            	Out.Print("Err:", Out.LEVEL.WARN, e.getStackTrace());
                e.printStackTrace();
            }

            try {
//                dayItems[counter].setPreferredSize(new Dimension(frame.getWidth() / 7, 0));
                playDatePane.add(dayItems[counter]);
            } catch (Exception e) {
            	Out.Print("Err:", Out.LEVEL.ERROR, e.getStackTrace());
                e.printStackTrace();
            }
            
            Out.Print("Counter increase on 1 now.");
            counter++;
        }

        Out.Print("Loading tracks accomplished.");
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

        frame.dispose();
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
    	Out.Print("Opening the frame...");
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
            Image image = Toolkit.getDefaultToolkit().getImage("./resources/icons/0.png");

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Test");
            defaultItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detray();
                }
            });
            popup.add(defaultItem);

            MenuItem close = new MenuItem("Close");
            close.addActionListener(e12 -> MainClass.exit(Codes.OLL_OK));
            popup.add(close);

            trayIcon = new TrayIcon(image, "BVF", popup);
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detray();
                }
            });
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
