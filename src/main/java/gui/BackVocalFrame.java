package gui;

import door.MainClass;
import registry.Registry;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class BackVocalFrame extends JFrame implements WindowListener {

    private TrayIcon trayIcon;
    private SystemTray tray;

    public BackVocalFrame() {
        loadUIM();

        setTitle("Back vocal studio v." + Registry.version);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(800, 400));
        setResizable(false);



        addWindowListener(this);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

//        stateChanged();
    }

    private void loadUIM() {
        try {UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {e2.printStackTrace();}
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
            MainClass.exit(0);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        System.out.println("Closing the frame...");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("./resources/tray.png");

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
//                    BackVocalFrame.this.setVisible(true);
//                    BackVocalFrame.this.setState(JFrame.NORMAL);
                    tray.remove(trayIcon);
                }
            };

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Test");
            defaultItem.addActionListener(listener);
            popup.add(defaultItem);

            MenuItem close = new MenuItem("Close");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainClass.exit(0);
                }
            });
            popup.add(close);

            trayIcon = new TrayIcon(image, "Tray Demo", popup);
            trayIcon.addActionListener(listener);
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("BackVocalStudio");

            try {
                tray.add(trayIcon);
                trayIcon.displayMessage("BVS", "Плеер работает в фоновом режиме", TrayIcon.MessageType.INFO);
            } catch (AWTException awtException) {
                awtException.printStackTrace();
            }
        }
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        tray.remove(trayIcon);
    }

    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
