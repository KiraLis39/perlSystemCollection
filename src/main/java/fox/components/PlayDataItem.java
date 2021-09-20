package fox.components;

import fox.fb.FoxFontBuilder;
import fox.out.Out;
import gui.BackVocalFrame;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class PlayDataItem extends JPanel implements MouseListener {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

    private AdvancedPlayer musicPlayer;
    private Playlist playlist;

    private JPanel dayControlPane;
    private JLabel dayNameLabel, inLabelH, inLabelM, inLabelS , outLabelH, outLabelM, outLabelS;
    private JCheckBox repeatCBox;
    private JButton startPlayBtn, nextPlayBtn, stopPlayBtn;

    private Font btnFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private Font titleFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL, 12, true);

    private Color defBkgColor = Color.GRAY, secondColor, defTextColor = Color.BLACK;
    private String timerIn = "00:00:00", timerOut = "23:59:59", alarmTime = "00:00:00";

    private boolean isSelected = false, repeat, isOver, isPlaying;
    private Thread musicThread;
    private int pausedOnFrame;
    private PlaybackListener pb;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // backbround:
        if (playlist == null || playlist.isEmpty()) {
            g.setColor(defBkgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            if (!isOver) {
                if (getName().equals("Sunday") || getName().equals("Saturday")) {
                    g.setColor(Color.CYAN.darker());
                } else {
                    g.setColor(Color.DARK_GRAY);
                }
                g.fillRect(0, 0, getWidth(), getHeight());
                defTextColor = Color.WHITE;
            } else {
                g.setColor(Color.YELLOW);
                g.fillRect(0, 0, getWidth(), getHeight());
                defTextColor = Color.BLACK;
            }
        }

        if (isSelected) {
            Graphics2D g2D = (Graphics2D) g;
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(Color.GREEN);
            g2D.drawRoundRect(1,1,getWidth() - 2,getHeight() - 2,3,3);
//            g2D.dispose();
        }

        // oval:
        if (playlist == null || playlist.isEmpty()) {
            g.setColor(Color.GRAY);
            g.fillOval(getWidth() - 19, 3, 16, 16);
        } else {
            g.setColor(Color.ORANGE);
            g.fillOval(getWidth() - 19, 3, 16, 16);
        }

        recolor();
    }

    private void recolor() {
        dayNameLabel.setForeground(defTextColor);
        inLabelM.setForeground(defTextColor);
        inLabelS.setForeground(defTextColor);
        inLabelH.setForeground(defTextColor);
        outLabelH.setForeground(defTextColor);
        outLabelM.setForeground(defTextColor);
        outLabelS.setForeground(defTextColor);
        repeatCBox.setForeground(defTextColor);
    }


    public PlayDataItem(String name, String _timerIn, String _timerOut, String _alarmTime, Boolean _repeat) {
        setName(name);
        this.timerIn = _timerIn;
        this.timerOut = _timerOut;
        this.alarmTime = _alarmTime;
        this.repeat = _repeat;
        secondColor = defBkgColor;
        if (getName().equals("Saturday") || getName().equals("Sunday")) {
            secondColor = defBkgColor = Color.CYAN;
        }
        playlist = new Playlist(this);

        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        dayNameLabel = new JLabel(getName()) {{setFont(titleFont);}};

        dayControlPane = new JPanel(new BorderLayout()) {
            {
                setOpaque(false);
                setBorder(new EmptyBorder(0,0,1,6));

                JPanel upSchedulePane = new JPanel(new BorderLayout(3,0)) {
                    {
                        setOpaque(false);

                        JPanel inTimePane = new JPanel(new GridLayout(2,3, 3,0)) {
                            {
                                setOpaque(false);
                                setBackground(Color.ORANGE.darker());

                                inLabelH = new JLabel("In-Hour:") {{setHorizontalAlignment(JLabel.CENTER);}};
                                JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(8,0,23,1)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerIn.split(":")[0]));
                                        addChangeListener(new ChangeListener() {
                                            @Override
                                            public void stateChanged(ChangeEvent e) {
                                                timerIn = getValue() + ":" + timerIn.split(":")[1] + ":" + timerIn.split(":")[2];
                                            }
                                        });
                                    }
                                };

                                inLabelM = new JLabel("In-Min.:") {{setHorizontalAlignment(JLabel.CENTER);}};
                                JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,1)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerIn.split(":")[1]));
                                        addChangeListener(new ChangeListener() {
                                            @Override
                                            public void stateChanged(ChangeEvent e) {
                                                timerIn = timerIn.split(":")[0] + ":" + getValue() + ":" + timerIn.split(":")[2];
                                            }
                                        });
                                    }
                                };

                                inLabelS = new JLabel("In-Sec.:") {{setHorizontalAlignment(JLabel.CENTER);}};
                                JSpinner secondSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,5)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerIn.split(":")[2]));
                                        addChangeListener(new ChangeListener() {
                                            @Override
                                            public void stateChanged(ChangeEvent e) {
                                                timerIn = timerIn.split(":")[0] + ":" + timerIn.split(":")[1] + ":" + getValue();
                                            }
                                        });
                                    }
                                };

                                add(inLabelH);
                                add(inLabelM);
                                add(inLabelS);
                                add(hourSpinner);
                                add(minuteSpinner);
                                add(secondSpinner);
                            }
                        };

                        JPanel outTimePane = new JPanel(new GridLayout(2,3, 3, 0)) {
                            {
                                setOpaque(false);
                                setBackground(Color.CYAN.darker());

                                outLabelH = new JLabel("Out-Hour:") {{setHorizontalAlignment(JLabel.RIGHT);}};
                                JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(8,0,23,1)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerOut.split(":")[0]));
                                        addChangeListener(new ChangeListener() {
                                            @Override
                                            public void stateChanged(ChangeEvent e) {
                                                timerOut = getValue() + ":" + timerOut.split(":")[1] + ":" + timerOut.split(":")[2];
                                            }
                                        });
                                    }
                                };

                                outLabelM = new JLabel("Out-Min.:") {{setHorizontalAlignment(JLabel.RIGHT);}};
                                JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,1)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerOut.split(":")[1]));
                                        addChangeListener(new ChangeListener() {
                                            @Override
                                            public void stateChanged(ChangeEvent e) {
                                                timerOut = timerOut.split(":")[0] + ":" + getValue() + ":" + timerOut.split(":")[2];
                                            }
                                        });
                                    }
                                };

                                outLabelS = new JLabel("Out-Sec.:") {{setHorizontalAlignment(JLabel.RIGHT);}};
                                JSpinner secondSpinner = new JSpinner(new SpinnerNumberModel(0,0,59,5)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerOut.split(":")[2]));
                                        addChangeListener(new ChangeListener() {
                                            @Override
                                            public void stateChanged(ChangeEvent e) {
                                                timerOut = timerOut.split(":")[0] + ":" + timerOut.split(":")[1] + ":" + getValue();
                                            }
                                        });
                                    }
                                };

                                add(outLabelH);
                                add(outLabelM);
                                add(outLabelS);
                                add(hourSpinner);
                                add(minuteSpinner);
                                add(secondSpinner);
                            }
                        };

                        add(inTimePane, BorderLayout.NORTH);
                        add(outTimePane, BorderLayout.SOUTH);
                    }
                };

                JPanel downOptionsPane = new JPanel(new BorderLayout(3,3)) {
                    {
                        setOpaque(false);
                        setBorder(new EmptyBorder(6,0,0,0));

                        JButton alarmsBtn = new JButton() {
                            BufferedImage im;

                            {
                                try {im = ImageIO.read(new File("./resources/icons/alarm.png"));
                                } catch (IOException e) {e.printStackTrace();}
                            }

                            @Override
                            public void paintComponent(Graphics g) {
                                if (im != null) {
                                    g.drawImage(im, 1, 1, 30, 30, null);
                                    g.drawRoundRect(0,0,31,31,6,6);
                                } else {super.paintComponent(g);}
                            }

                            {
                                try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/alarm.png").toUri().toURL())));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                setPreferredSize(new Dimension(32, 32));
                                addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        new AlarmsDialog(BackVocalFrame.getFrame());
                                    }
                                });
                            }
                        };

                        JPanel btnsPane = new JPanel(new GridLayout(1,0)) {
                            {
                                setOpaque(false);

                                startPlayBtn = new JButton() {
                                    BufferedImage im;

                                    @Override
                                    public void paintComponent(Graphics g) {
//                                        if (im == null) {
                                            try {
                                                if (isPlaying) {
                                                    im = ImageIO.read(new File("./resources/icons/playPressed.png"));
                                                } else {
                                                    im = ImageIO.read(new File("./resources/icons/play.png"));
                                                }
                                            } catch (IOException e) {e.printStackTrace();}
//                                        }

                                        g.drawImage(im, 1, 1, 30, 30, null);
                                    }

                                    {
                                        setFont(btnFont);
                                        try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/play.png").toUri().toURL())));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(32, 32));
                                        addActionListener(e -> {
                                            if (inSchedulingTimeAccept()) {
                                                play();
                                                selectThisItem();
                                            } else {
                                                JOptionPane.showConfirmDialog(PlayDataItem.this, "Its not a schedule time!", "Not yet:", JOptionPane.DEFAULT_OPTION);
                                            }
                                        });
                                    }
                                };

                                nextPlayBtn = new JButton() {
                                    BufferedImage im;

                                    {
                                        try {im = ImageIO.read(new File("./resources/icons/next.png"));
                                        } catch (IOException e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void paintComponent(Graphics g) {
                                        if (im != null) {
                                            g.drawImage(im, 1, 1, 30, 30, null);
                                        } else {super.paintComponent(g);}
                                    }
                                    {
                                        setFont(btnFont);
                                        try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/next.png").toUri().toURL())));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(32, 32));
                                        addActionListener(e -> {
                                            if (isPlaying && inSchedulingTimeAccept()) {
                                                stop();
                                                playlist.selectRow(playlist.getSelectedIndex() + 1 >= playlist.getRowsCount() ? 0 : playlist.getSelectedIndex() + 1);
                                                play();
                                                selectThisItem();
                                            } else {
                                                JOptionPane.showConfirmDialog(PlayDataItem.this, "Not available now.", "Not yet:", JOptionPane.DEFAULT_OPTION);
                                            }
                                        });
                                    }
                                };

                                stopPlayBtn = new JButton() {
                                    BufferedImage im;

                                    {
                                        try {im = ImageIO.read(new File("./resources/icons/stop.png"));
                                        } catch (IOException e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void paintComponent(Graphics g) {
                                        if (im != null) {
                                            g.drawImage(im, 1, 1, 30, 30, null);
                                        } else {super.paintComponent(g);}
                                    }
                                    {
                                        setFont(btnFont);
                                        try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/stop.png").toUri().toURL())));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(32, 32));
                                        addActionListener(e -> stop());
                                    }
                                };

                                add(startPlayBtn);
                                add(nextPlayBtn);
                                add(stopPlayBtn);
                            }
                        };

                        JPanel repeatPane = new JPanel(new BorderLayout(3,3)) {
                            {
                                setOpaque(false);

                                repeatCBox = new JCheckBox("Repeat") {
                                    {
                                        setFont(btnFont);
                                        setForeground(defTextColor);
                                        setSelected(repeat);
                                        addItemListener(new ItemListener() {
                                            public void itemStateChanged(ItemEvent e) {
                                                repeat = isSelected();
                                            }
                                        });
                                    }
                                };

                                add(repeatCBox, BorderLayout.EAST);
                            }
                        };

                        add(alarmsBtn, BorderLayout.WEST);
                        add(btnsPane, BorderLayout.CENTER);
                        add(repeatPane, BorderLayout.EAST);
                    }
                };

                add(upSchedulePane, BorderLayout.CENTER);
                add(downOptionsPane, BorderLayout.SOUTH);
            }
        };

        add(dayNameLabel, BorderLayout.NORTH);
        add(dayControlPane, BorderLayout.CENTER);

        setBorder(new EmptyBorder(3,3,3,3));
        addMouseListener(this);

        pb = new PlaybackListener() {
            @Override
            public void playbackStarted(PlaybackEvent evt) {
                super.playbackStarted(evt);

                BackVocalFrame.setProgress(100 / playlist.getRowsCount() * index);
            }

            @Override
            public void playbackFinished(PlaybackEvent evt) {
                super.playbackFinished(evt);
                if (repeat) {
                    index++;
                    if (index >= playlist.getRowsCount()) {
                        index = 0;
                    }
                    isPlaying = false;
                    playlist.selectRow(index);
                    play();
                } else {BackVocalFrame.setProgress(100);}
            }
        };
    }

    public synchronized void saveToFile() {
        try {

            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("./resources/scheduler/" + getName() + ".meta"), Charset.forName("UTF-8"))) {
                osw.write(
                        "NN_T_IN_EE" + timerIn +
                                "NN_T_OUT_EE" + timerOut +
                                "NN_ALARM_EE" + alarmTime +
                                "NN_REP_EE" + repeat
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("./resources/scheduler/" + getName() + ".list"), Charset.forName("UTF-8"))) {

                for (Path track : playlist.getTracks()) {
                    osw.write(track.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Out.Print("Error with saving PlayDataItem: " + e.getMessage());
        }
    }

    public boolean inSchedulingTimeAccept() {
        String nowTime = new Date().toString().split(" ")[3];

        int nowHour = Integer.parseInt(nowTime.split(":")[0]);
        int nowMinute = Integer.parseInt(nowTime.split(":")[1]);
        int nowSecond = Integer.parseInt(nowTime.split(":")[2]);

        int inHour = Integer.parseInt(timerIn.split(":")[0]);
        int inMinute = Integer.parseInt(timerIn.split(":")[1]);
        int inSecond = Integer.parseInt(timerIn.split(":")[2]);

        int outHour = Integer.parseInt(timerOut.split(":")[0]);
        int outMinute = Integer.parseInt(timerOut.split(":")[1]);
        int outSecond = Integer.parseInt(timerOut.split(":")[2]);

        if (nowHour > outHour) {
            return false;
        } else if (nowHour == outHour) {
            if (nowMinute > outMinute) {
                return false;
            } else if (nowMinute == outMinute) {
                if (nowSecond > outSecond) {
                    return false;
                }
            }
        }

        if (nowHour < inHour) {
            System.out.println("Point 01");
            return false;
        } else if (nowHour == inHour && nowMinute < inMinute) {
            System.out.println("Point 02");
            return false;
        } else if (nowHour == inHour && nowMinute == inMinute && nowSecond < inSecond) {
            System.out.println("Point 03");
            return false;
        }

        return true;
    }

    // Audio control:
    int index;
    public synchronized void play() {
        if (playlist == null) {
            JOptionPane.showConfirmDialog(this, "Playlist is empty!", "Info:", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }
        if (isPlaying) {
            return;
        }

        index = playlist.getSelectedIndex();
        musicThread = new Thread(() -> {
            URI uri;
            BufferedInputStream mp3 = null;

            try {
                uri = playlist.getTrack(index).toFile().toURI();
                mp3 = new BufferedInputStream(uri.toURL().openStream());

                musicPlayer = new AdvancedPlayer(mp3, FactoryRegistry.systemRegistry().createAudioDevice());
                musicPlayer.setPlayBackListener(pb);

                isPlaying = true;
                startPlayBtn.setBackground(Color.GREEN);
                playlist.selectRow(index);

                BackVocalFrame.updatePlayedLabelText();
                musicPlayer.play();
            } catch (IOException | JavaLayerException e) {
                e.printStackTrace();
            } finally {
                if (mp3 != null) {
                    try {mp3.close();
                    } catch (IOException e) {e.printStackTrace();}
                }
                stop();
            }
        });
        musicThread.start();

        Out.Print("Media: music: the '" + playlist.getTrack(playlist.getSelectedIndex()).toFile().getName() + "' exist into musicMap and play now...");
    }

    public synchronized void stop() {
        System.out.println("STOPPED!");
        isPlaying = false;
        startPlayBtn.setBackground(null);

        try {
            try {
                musicPlayer.close();
            } catch (Exception e) {/* IGNORE BY NOW */}

            try {
                musicThread.interrupt();
            } catch (Exception e) {/* IGNORE BY NOW */}
        } catch (Exception e) {e.printStackTrace();}

        BackVocalFrame.setProgress(0);
        BackVocalFrame.updatePlayedLabelText();
    }


    // Getters & setters:
    public void addTrack(Path path) {
        Out.Print("Adding the track '" + path.toFile().getName() + "' to day '" + getName() + "'...");
        playlist.add(path);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setBackground(isSelected ? Color.green.darker() : null);
        BackVocalFrame.enableControls(selected);
    }
    public boolean isSelected() {return this.isSelected;}

    private void selectThisItem() {
        BackVocalFrame.resetDownPaneSelect();
        setSelected(true);
        BackVocalFrame.showPlayList(playlist);
        if (playlist != null && playlist.getRowsCount() > 0) {BackVocalFrame.setProgress(100 / playlist.getRowsCount() * index);}
    }

    public boolean isPlayed() {
        return isPlaying;
    }

    public String getActiveTrackName() {
        return playlist.getTrack(index).toFile().getName();
    }


    // Listeners:
    @Override
    public void mouseClicked(MouseEvent e) {
        if (isSelected) {
            BackVocalFrame.resetDownPaneSelect();
            return;
        }
        selectThisItem();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        isOver = true;
        defBkgColor = Color.YELLOW;
        defTextColor = Color.BLACK;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        isOver = false;
        defBkgColor = secondColor;
        repaint();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(isSelected ? Color.GREEN : bg == null ? defBkgColor : bg);
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    @Override
    public String toString() {
        return "PDate item '" + getName() + "' (" + playlist.getRowsCount() + " tracks)";
    }

    public void moveSelectedUp() {
        playlist.moveSelectedUp();
    }

    public void removeSelected() {
        playlist.removeSelected();
    }

    public void moveSelectedDown() {
        playlist.moveSelectedDown();
    }

    public Playlist getPlaylist() {return playlist;}


    // subframes:
    private static class AlarmsDialog extends JDialog {

        public AlarmsDialog(JFrame parent) {
            super(parent, "Alarm list:", true);

            setPreferredSize(new Dimension(400, 800));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);



            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }
}
