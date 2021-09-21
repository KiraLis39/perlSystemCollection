package fox.components;

import fox.fb.FoxFontBuilder;
import fox.out.Out;
import gui.BackVocalFrame;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.stream.Collectors;

public class PlayDataItem extends JPanel implements MouseListener {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat weakday = new SimpleDateFormat("EEEE", Locale.US);

    private AdvancedPlayer musicPlayer;
    private PlayPane playpane;

    private JPanel dayControlPane;
    private JLabel dayNameLabel, inLabelH, inLabelM, inLabelS , outLabelH, outLabelM, outLabelS;
    private JCheckBox repeatCBox;
    private JButton startPlayBtn, nextPlayBtn, stopPlayBtn;

    private Font btnFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, true);
    private Font titleFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL, 12, true);

    private Color defBkgColor = Color.GRAY, secondColor, defTextColor = Color.BLACK;
    private String timerIn = "00:00:00", timerOut = "23:59:59";

    private boolean isSelected = false, repeat, isOver, isPlaying, isPaused, isHandStopped = false;
    private Thread musicThread, alarmThread;
    private int pausedOnFrame;
    private PlaybackListener pb;

    private DefaultListModel<AlarmItem> arm = new DefaultListModel();
    private JList<AlarmItem> alarmList;
    private JFileChooser fch = new JFileChooser("./resources/audio/");


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // backbround:
        if (playpane == null || playpane.isEmpty()) {
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
        if (playpane == null || playpane.isEmpty()) {
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


    public PlayDataItem(String name, String _timerIn, String _timerOut, Boolean _repeat) {
        alarmList = new JList(arm);

        setName(name);
        this.timerIn = _timerIn;
        this.timerOut = _timerOut;
        this.repeat = _repeat;
        secondColor = defBkgColor;
        if (getName().equals("Saturday") || getName().equals("Sunday")) {
            secondColor = defBkgColor = Color.CYAN;
        }
        playpane = new PlayPane(this);

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
                                                isHandStopped = false;
                                                if (playpane.getSelectedIndex() != playpane.getPlayedIndex()) {
                                                    stop();
                                                    index = playpane.getSelectedIndex();
                                                    play();
                                                    return;
                                                }

                                                play();
                                                setSelected(true);
                                                playpane.repaint();
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
//                                                playpane.selectRow(playpane.getSelectedIndex() + 1 >= playpane.getRowsCount() ? 0 : playpane.getSelectedIndex() + 1);
                                                playNext();
                                                setSelected(true);

                                                playpane.repaint();
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
                                        addActionListener(e -> {
                                            isHandStopped = true;
                                            stop();
                                        });
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
                BackVocalFrame.setProgress(100 / playpane.getRowsCount() * index);
            }

            @Override
            public void playbackFinished(PlaybackEvent evt) {
                super.playbackFinished(evt);
                pausedOnFrame = evt.getFrame();
                System.out.println("stopped by playbackFinished");

                if (repeat && !isPaused && !isHandStopped) {
                    System.err.println("skipped by playbackFinished");
                    playNext();
                } else {BackVocalFrame.setProgress(100);}

            }
        };
    }

    public synchronized void saveToFile() {
        try {

            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".meta"), StandardCharsets.UTF_8)) {
                osw.write(
                        "NN_T_IN_EE" + timerIn +
                            "NN_T_OUT_EE" + timerOut +
                            "NN_REP_EE" + repeat
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".alarms"), StandardCharsets.UTF_8)) {
                for (int i = 0; i < arm.size(); i++) {
                    osw.write(arm.get(i).getTime() + ">" + arm.get(i).getTrack() + "\r\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream("./resources/scheduler/" + getName() + ".list"), StandardCharsets.UTF_8)) {

                for (Path track : playpane.getTracks()) {
                    osw.write(track.toString() + "\r\n");
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
        boolean accept = true;
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
            System.err.println("Point 00");
            accept = false;
        } else if (nowHour == outHour) {
            if (nowMinute > outMinute) {
                System.err.println("Point FW");
                accept = false;
            } else if (nowMinute == outMinute) {
                if (nowSecond > outSecond) {
                    System.err.println("Point BE");
                    accept = false;
                }
            }
        }

        if (nowHour < inHour) {
            System.err.println("Point 01");
            accept = false;
        } else if (nowHour == inHour && nowMinute < inMinute) {
            System.err.println("Point 02");
            accept = false;
        } else if (nowHour == inHour && nowMinute == inMinute && nowSecond < inSecond) {
            System.err.println("Point 03");
            accept = false;
        }

        return accept;
    }

    public void checkScheduleLaunch() {
        // если сегодня день и время активности - нужно сразу запуститься.
        Date todayIs = new Date();

        boolean todayIsCurrentItemList = false;
        System.out.println("WD: '" + weakday.format(todayIs) + "', '" + getName() + "'.");
        if (weakday.format(todayIs).equalsIgnoreCase(getName())) {
            todayIsCurrentItemList = true;
        }

        boolean currentDaysPlaylistIsNotEmpty = !playpane.isEmpty();
        if (todayIsCurrentItemList && currentDaysPlaylistIsNotEmpty) {
            BackVocalFrame.resetDownPaneSelect();
            setSelected(true);
            play();
        }
    }


    // Audio control:
    int index;
    public synchronized void play() {
        if (isPlaying) {
            stop();
            return;
        }

        if (playpane == null) {
            JOptionPane.showConfirmDialog(this, "Playlist is empty!", "Info:", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }

        index = playpane.getSelectedIndex();
        runTheThread();

        Out.Print("Media: music: the '" + playpane.getTrack(playpane.getSelectedIndex()).toFile().getName() + "' exist into musicMap and play now...");
    }

    private synchronized void runTheThread() {
        if (musicPlayer != null) {
            musicPlayer.close();
        }

        musicThread = new Thread(() -> {
            isPlaying = true;

            try {
                URI uri = playpane.getTrack(index).toFile().toURI();

                try (
                        InputStream s = uri.toURL().openStream();
                        BufferedInputStream mp3 = new BufferedInputStream(s)) {

                    musicPlayer = new AdvancedPlayer(mp3);
                    musicPlayer.setPlayBackListener(pb);

                    BackVocalFrame.updatePlayedLabelText();

                    musicPlayer.play();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });
        musicThread.start();
    }

    public synchronized void playNext() {
        System.out.println("PLAY_NEXT!");

        index++;
        if (index >= playpane.getRowsCount()) {
            index = 0;
        }
//      playpane.selectRow(index);
        runTheThread();
    }

    public synchronized void pause() {
        System.out.println("PAUSED!");

        try {
            isPaused = true;
            musicPlayer.stop();
        } catch (Exception f) {/* IGNORE */}
    }

    public synchronized void resume() {
        System.out.println("RESUMED!");

        if (isPaused) {
            isPaused = false;
            play();
        }
    }

    public synchronized void stop() {
        System.out.println("STOPPED!");

        try {musicPlayer.close();
        } catch (Exception e) {/* IGNORE BY NOW */}

        try {musicThread.interrupt();
        } catch (Exception e) {/* IGNORE BY NOW */}

        try {alarmThread.interrupt();
        } catch (Exception e) {/* IGNORE BY NOW */}

        isPlaying = false;

        BackVocalFrame.setProgress(0);
        BackVocalFrame.updatePlayedLabelText();

        playpane.repaint();
    }

    public synchronized void playAlarm(Path alarmFilePath) {
        alarmThread = new Thread(() -> {
            try {
                URI uri = alarmFilePath.toFile().toURI();
                try (
                        InputStream s = uri.toURL().openStream();
                        BufferedInputStream mp3 = new BufferedInputStream(s)) {
                    new AdvancedPlayer(mp3).play();
                }
            } catch (IOException | JavaLayerException e) {
                e.printStackTrace();
            }
        });
        alarmThread.start();
    }


    // Getters & setters:
    public boolean isHandStopped() {
        return isHandStopped;
    }

    public void addTrack(Path path) {
        playpane.add(path);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setBackground(isSelected ? Color.green.darker() : null);

        if (selected) {
            BackVocalFrame.showPlayList(playpane);

            if (playpane != null && playpane.getRowsCount() > 0) {
                BackVocalFrame.setProgress(100 / playpane.getRowsCount() * index);
            }
        }

        BackVocalFrame.enableControls(selected);
        playpane.repaint();
    }
    public boolean isSelected() {return this.isSelected;}

    public boolean isPlayed() {
        return isPlaying;
    }

    public String getActiveTrackName() {
        return playpane.getTrack(index).toFile().getName();
    }


    // Listeners:
    @Override
    public void mouseClicked(MouseEvent e) {
        if (isSelected) {
            BackVocalFrame.resetDownPaneSelect();
            return;
        }
        BackVocalFrame.resetDownPaneSelect();
        setSelected(true);
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
        return "PDate item '" + getName() + "' (" + playpane.getRowsCount() + " tracks)";
    }

    public void moveSelectedUp() {
        playpane.moveSelectedUp();
    }

    public void removeSelected() {
        playpane.removeSelected();
    }

    public void moveSelectedDown() {
        playpane.moveSelectedDown();
    }

    public PlayPane getPlayPane() {return playpane;}

    public void addAlarm(String time, Path track) {
        arm.addElement(new AlarmItem(time, track));
    }

    public boolean isPaused() {return isPaused;}

    public ArrayList<AlarmItem> getAlarmData() {
        ArrayList<AlarmItem> result = new ArrayList<>();

        for (int i = 0; i < arm.size(); i++) {
            result.add(arm.get(i));
        }

        return result;
    }

    public boolean alarmThreadIsAlive() {
        return alarmThread.isAlive();
    }

    public boolean isTimeCome(String time) {
        if (time.length() == 8 && time.contains(":")) {
            String nowTime = new Date().toString().split(" ")[3];
            int now = Integer.parseInt(nowTime.replaceAll(":", ""));
            int need = Integer.parseInt(time.replaceAll(":", ""));
//            System.out.println("NOW: " + nowTime + "; alarm check time: " + time + "; pass: " + (now - need));

            if (now > need && now - need < 100) {
                return true;
            }
        }
        return false;
    }


    // subframes:
    private class AlarmsDialog extends JDialog {

        public AlarmsDialog(JFrame parent) {
            super(parent, "Alarms list:", true);

            setMinimumSize(new Dimension(400, 600));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel basePane = new JPanel() {
                {
                    setBackground(Color.MAGENTA);
                    setLayout(new BorderLayout());

                    JPanel centerAlarmsListPane = new JPanel(new BorderLayout()) {
                        {
                            setBackground(Color.DARK_GRAY);

                            add(alarmList);
                        }
                    };

                    JPanel downButtonsPane = new JPanel() {
                        {
                            setBackground(Color.DARK_GRAY);
                            setBorder(new EmptyBorder(0,3,3,3));
                            setLayout(new GridLayout(1, 2, 3, 3));

                            JButton addAlarmBtn = new JButton("Add alarm") {
                                {
                                    setBackground(new Color(0.75f,1.0f,0.75f,1.0f));
                                    addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            String alarmInitTime =
                                                    JOptionPane.showInputDialog(
                                                            AlarmsDialog.this,
                                                            "Input init time:", "00:00:00");
                                            Path alarmFilePath = null;

                                            fch.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                            fch.setMultiSelectionEnabled(false);
                                            fch.setDialogTitle("Choose alarm:");

                                            int result = fch.showOpenDialog(AlarmsDialog.this);
                                            if (result == JFileChooser.APPROVE_OPTION ) {
                                                alarmFilePath = fch.getSelectedFile().toPath();

                                                if (!alarmInitTime.isBlank() && alarmInitTime.length() == 8) {
                                                    arm.addElement(new AlarmItem(alarmInitTime, alarmFilePath));
                                                } else {
                                                    JOptionPane.showConfirmDialog(AlarmsDialog.this, "Something wrong!", "Canceled.", JOptionPane.DEFAULT_OPTION);
                                                }
                                            }
                                        }
                                    });
                                }
                            };

                            JButton remAlarmBtn = new JButton("Remove alarm") {
                                {
                                    setBackground(new Color(1.0f,0.75f,0.75f,1.0f));
                                    addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            AlarmItem toDelete = alarmList.getSelectedValue();
                                            int req = JOptionPane.showConfirmDialog(AlarmsDialog.this, "Delete selected?", "Confirm:", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
                                            if (req == 0) {
                                                arm.removeElement(toDelete);
                                            }
                                        }
                                    });
                                }
                            };

                            add(addAlarmBtn);
                            add(remAlarmBtn);
                        }
                    };

                    add(centerAlarmsListPane, BorderLayout.CENTER);
                    add(downButtonsPane, BorderLayout.SOUTH);
                }
            };

            add(basePane);

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }
}
