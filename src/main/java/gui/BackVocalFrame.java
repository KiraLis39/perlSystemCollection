package gui;

import core.PlayDateItem;
import core.Playlist;
import door.MainClass;
import fox.out.Out;
import registry.Codes;
import registry.Registry;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.*;
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

    private TrayIcon trayIcon;
    private SystemTray tray;
    private Boolean isTrayed;
    private ExecutorService executor;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private static JPanel basePane, centerPlaylistsPane;
    private static JPanel playDatePane;
    private static JScrollPane playDateScroll, playListsScroll;

    public BackVocalFrame() {
        loadUIM();

        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
//        String time = sdf.format(Instant.now().toEpochMilli());

        Out.Print("Build the frame...");

        setTitle("Back vocal studio v." + Registry.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 450));

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
                    }
                };

                JPanel rightShedulePane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setBackground(Color.darkGray);
                        setPreferredSize(new Dimension(350, 0));

                        playDatePane = new JPanel(new GridLayout(7, 1, 3,3)) {
                            {
                                add(new PlayDateItem("Понедельник", new Playlist(null)));
                                add(new PlayDateItem("Вторник", new Playlist(null)));
                                add(new PlayDateItem("Среда", new Playlist(null)));
                                add(new PlayDateItem("Четверг", new Playlist(null)));
                                add(new PlayDateItem("Пятница", new Playlist(null)));
                                add(new PlayDateItem("Суббота", new Playlist(null), Color.CYAN));
                                add(new PlayDateItem("Воскресенье", new Playlist(null), Color.CYAN));
                            }
                        };

                        playDateScroll = new JScrollPane(playDatePane) {
                            {
                                setBorder(null);
                                getViewport().setPreferredSize(new Dimension(350, 0));
                                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                            }
                        };

                        add(playDateScroll, BorderLayout.CENTER);
                    }
                };

                JPanel downBtnsPane = new JPanel(new FlowLayout(0, 3, 3)) {
                    {
                        JButton bindListBtn = new JButton("Bind to dir") {
                            {
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
                                        
                                        JFileChooser fch = new JFileChooser("./resources/");
                                        fch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                        fch.setMultiSelectionEnabled(false);
                                        fch.setDialogTitle("Choose play-folder:");

                                        int result = fch.showOpenDialog(BackVocalFrame.this);
                                        // Если директория выбрана, покажем ее в сообщении
                                        if (result == JFileChooser.APPROVE_OPTION ) {
                                            Path plDir = Paths.get(fch.getSelectedFile().toURI());

                                            selectedItemList.getPlayList().setMusicDir(plDir);
                                        }
                                    }
                                });
                            }
                        };

                        add(bindListBtn);
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

        executor = Executors.newSingleThreadExecutor();
//		executor.submit(() -> {
//			some runnable code...
//		});
//		executor.shutdown(); //shutdown executor
//		executor.awaitTermination(5, TimeUnit.SECONDS); //ожидая завершения запущенных задач в течение 5 секунд
//		executor.shutdownNow(); //cancel non-finished tasks

//        stateChanged();
    }

    public static int getRightPaneWidth() {
        return playDateScroll.getWidth();
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
        centerPlaylistsPane.removeAll();
        centerPlaylistsPane.add(playlist);

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

    private void loadUIM() {
        Out.Print("Set the UIManagers view.");

        try {UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
                e2.printStackTrace();
                Out.Print(BackVocalFrame.class, Out.LEVEL.ERROR, Arrays.stream(e2.getStackTrace()).toArray());
            }
        }
    }

    private void stateChanged() {
//        if (trayIcon != null) {
//            trayIcon.setImage(updatedImage);
//        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        System.out.println("Opening the frame...");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int req = JOptionPane.showConfirmDialog(BackVocalFrame.this, "Are You sure?..", "Exit?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
        if (req == 0) {
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

    private void tray() throws AWTException {
        Out.Print("Traying the frame...");
        tray.add(trayIcon);
        trayIcon.displayMessage("BVS", "Плеер работает в фоновом режиме", TrayIcon.MessageType.INFO);
        isTrayed = true;
    }

    private void detray() {
        Out.Print("De-Traying the frame...");
        BackVocalFrame.this.setVisible(true);
        BackVocalFrame.this.setState(JFrame.NORMAL);
        tray.remove(trayIcon);
        isTrayed = false;
    }

    @Override
    public void windowDeiconified(WindowEvent e) {detray();}

    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
