package gui;

import fox.components.AlarmItem;
import fox.components.PlayPane;
import fox.fb.FoxFontBuilder;
import fox.out.Out;
import fox.render.FoxRender;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class PlayDataItem extends JPanel implements MouseListener, ActionListener {
//    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat weakday = new SimpleDateFormat("EEEE", Locale.US);

    private Player player;
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
    private int pausedOnFrame, indexOfPlayed;

    private DefaultListModel<AlarmItem> arm = new DefaultListModel();
    private JList<AlarmItem> alarmList;
    private JFileChooser fch = new JFileChooser("./resources/audio/");
    private JButton alarmsBtn;
    private Color alarmsBack = Color.GRAY;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = FoxRender.setMedRender((Graphics2D) g);

        // backbround:
        if (playpane == null || playpane.isEmpty()) {
            g2D.setColor(defBkgColor);
            g2D.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 2, 12, 12);
        } else {
            if (isOver) {
                g2D.setColor(Color.YELLOW);
                g2D.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 2, 12, 12);
                defTextColor = Color.BLACK;
            } else {
                if (getName().equals("Sunday") || getName().equals("Saturday")) {
                    g2D.setColor(Color.CYAN.darker());
                } else {
                    g2D.setColor(Color.DARK_GRAY);
                }
                g2D.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 2, 12, 12);
                defTextColor = Color.WHITE;
            }
        }

        if (isSelected) {
            g2D.setStroke(new BasicStroke(2));
            g2D.setColor(Color.GREEN);
            g2D.drawRoundRect(1,1,getWidth() - 3,getHeight() - 3,9,9);
//            g2D.dispose();
        }

        // oval:
        float opacity = 0.75f;
        if (playpane == null || playpane.isEmpty()) {
            g2D.setColor(new Color(0.35f, 0.35f, 0.35f, opacity));
            g2D.fillRoundRect(getWidth() - 28, 6, 20, 13, 9, 9);
        } else {
            opacity = 0f;

            for (int i = 0; i < 10; i++) {
                opacity += 0.07;
                if (opacity > 1f) {opacity = 1f;}
                g2D.setColor(new Color(1.0f, 0.8f, 0.3f, opacity));
                g2D.fillRoundRect(getWidth() - 33 + i, 3 + (i), (int) (30.5f - (i * 2)), 19 - (i * 2), 15, 15);
            }

            opacity = 1f;
            g2D.setColor(new Color(1.0f, 0.8f, 0.25f, opacity));
            g2D.fillRoundRect(getWidth() - 28, 6, 20, 13, 9, 9);

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

        setOpaque(false);
        setLayout(new BorderLayout());

        dayNameLabel = new JLabel(getName()) {{setFont(titleFont); setBorder(new EmptyBorder(3,6,0,0));}};

        dayControlPane = new JPanel(new BorderLayout()) {
            {
                setOpaque(false);
                setBorder(new EmptyBorder(0,6,1,6));

                JPanel upSchedulePane = new JPanel(new BorderLayout(3,0)) {
                    {
                        setOpaque(false);

                        JPanel inTimePane = new JPanel(new GridLayout(2,3, 3,0)) {
                            {
                                setOpaque(false);

                                inLabelH = new JLabel("In-Hour:") {{setHorizontalAlignment(JLabel.CENTER);}};
                                JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(8,0,23,1)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerIn.split(":")[0]));
                                        getEditor().setBackground(Color.BLACK);
//                                        getEditor().getComponent(0).setForeground(Color.GREEN);
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
                                        getEditor().setBackground(Color.BLACK);
//                                        getEditor().getComponent(0).setForeground(Color.GREEN);
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
                                        getEditor().setBackground(Color.BLACK);
//                                        getEditor().getComponent(0).setForeground(Color.GREEN);
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

                                outLabelH = new JLabel("Out-Hour:") {{setHorizontalAlignment(JLabel.RIGHT);}};
                                JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(8,0,23,1)) {
                                    {
                                        setEditor(new JSpinner.NumberEditor(this,"00"));
                                        setValue(Integer.parseInt(timerOut.split(":")[0]));
                                        getEditor().setBackground(Color.BLACK);
//                                        getEditor().getComponent(0).setForeground(Color.RED);
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
                                        getEditor().setBackground(Color.BLACK);
//                                        getEditor().getComponent(0).setForeground(Color.RED);
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

                                        getEditor().setBackground(Color.BLACK);
//                                        getEditor().getComponent(0).setForeground(Color.RED);
//                                        ((JSpinner.NumberEditor) getEditor()).getTextField().setSelectedTextColor(Color.WHITE);

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

                        int btnsDim = 28;

                        alarmsBtn = new JButton() {
                            BufferedImage im;

                            {
                                try {im = ImageIO.read(new File("./resources/icons/alarm.png"));
                                } catch (IOException e) {e.printStackTrace();}
                            }

                            @Override
                            public void paintComponent(Graphics g) {
                                if (im != null) {
                                    g.setColor(alarmsBack);
                                    g.fillRoundRect(0,0,32,32,6,6);

                                    g.drawImage(im, 3, 3, 26, 26, null);

                                    g.setColor(Color.DARK_GRAY);
                                    g.drawRoundRect(1,1,29,29,3,3);
                                } else {super.paintComponent(g);}
                            }

                            {
                                try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/alarm.png").toUri().toURL())));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                setPreferredSize(new Dimension(32, 32));
                                setActionCommand("alarmBtn");
                                addActionListener(PlayDataItem.this);
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

                                        g.drawImage(im, 1, 1, getWidth() - 4, btnsDim, null);
                                    }

                                    {
                                        setFont(btnFont);
                                        try {setIcon(new ImageIcon(ImageIO.read(Paths.get("./resources/icons/play.png").toUri().toURL())));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        setFocusPainted(false);
                                        setPreferredSize(new Dimension(32, 32));
                                        setActionCommand("play");
                                        addActionListener(PlayDataItem.this);
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
                                            g.drawImage(im, 1, 1, getWidth() - 4, btnsDim, null);
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
                                        setActionCommand("next");
                                        addActionListener(PlayDataItem.this);
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
                                            g.drawImage(im, 1, 1, getWidth() - 4, btnsDim, null);
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
                                        setActionCommand("stop");
                                        addActionListener(PlayDataItem.this);
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
//            System.err.println("Point 00");
            accept = false;
        } else if (nowHour == outHour) {
            if (nowMinute > outMinute) {
//                System.err.println("Point FW");
                accept = false;
            } else if (nowMinute == outMinute) {
                if (nowSecond > outSecond) {
//                    System.err.println("Point BE");
                    accept = false;
                }
            }
        }

        if (nowHour < inHour) {
//            System.err.println("Point 01");
            accept = false;
        } else if (nowHour == inHour && nowMinute < inMinute) {
//            System.err.println("Point 02");
            accept = false;
        } else if (nowHour == inHour && nowMinute == inMinute && nowSecond < inSecond) {
//            System.err.println("Point 03");
            accept = false;
        }

        return accept;
    }

//    public void checkScheduleLaunch() {
//        // если сегодня день и время активности - нужно сразу запуститься.
//        Date todayIs = new Date();
//
//        boolean todayIsCurrentItemList = false;
//        System.out.println("WD: '" + weakday.format(todayIs) + "', '" + getName() + "'.");
//        if (weakday.format(todayIs).equalsIgnoreCase(getName())) {
//            todayIsCurrentItemList = true;
//        }
//
//        boolean currentDaysPlaylistIsNotEmpty = !playpane.isEmpty();
//        if (todayIsCurrentItemList && currentDaysPlaylistIsNotEmpty) {
//            BackVocalFrame.resetDownPaneSelect();
//            setSelected(true);
//            play(0);
//        }
//    }

    // Audio control:
    public synchronized void play() {
        if (playpane == null) {
            JOptionPane.showConfirmDialog(this, "Playlist is empty!", "Info:", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }
        if (isPlaying) {stop();}

        Out.Print("The track '" + playpane.getTrack(indexOfPlayed).toFile().getName() + "' is played now...");
        musicThread = new Thread(() -> {
            while (indexOfPlayed < playpane.getRowsCount()) {
                BackVocalFrame.updatePlayedLabelText();

                try (BufferedInputStream mp3 = new BufferedInputStream(new FileInputStream(playpane.getTrack(indexOfPlayed).toFile()))) {
                    isPlaying = true;
                    player = new Player(mp3, FactoryRegistry.systemRegistry().createAudioDevice());
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {player.close();
                    } catch (Exception e) {/* IGNORE */}
                }

                indexOfPlayed++;
                if (repeat && !isPaused && !isHandStopped) {
                    if (indexOfPlayed >= playpane.getRowsCount()) {
                        System.out.println("REPEAT INITIATED!");
                        indexOfPlayed = 0;
                    }
                }
            }
        });
        musicThread.start();
    }

    public synchronized void playNext() {
        System.out.println("PLAY_NEXT!");

//        indexOfPlayed++;
        if (indexOfPlayed >= playpane.getRowsCount()) {
            indexOfPlayed = 0;
        }
        play();
    }

    public synchronized void pause() {
        try {
            isPaused = true;
//            pausedOnFrame = player.getPosition();
            stop();
            System.out.println("PAUSED! On frame #" + pausedOnFrame);
        } catch (Exception f) {/* IGNORE */}
    }

    public synchronized void resume() {
        System.out.println("RESUMED! From frame #" + pausedOnFrame);

        if (isPaused) {
            isPaused = false;
            play();
        }
    }

    public synchronized void stop() {
        System.out.println("STOPPED!");

        try {
            player.close();
            pausedOnFrame = player.getPosition();
        } catch (Exception e) {/* IGNORE BY NOW */}

        try {
            musicThread.interrupt();
            musicThread.stop();
        } catch (Exception e) {/* IGNORE BY NOW */}

        try {
            alarmThread.interrupt();
            alarmThread.stop();
        } catch (Exception e) {/* IGNORE BY NOW */}

        isPlaying = false;

        BackVocalFrame.updatePlayedLabelText();

        playpane.repaint();
    }

    public synchronized void playAlarm(Path alarmFilePath) {
        BackVocalFrame.setPlayedLabelText("<html><b color='RED'>Alarm:</b> " + alarmFilePath.toFile().getName());
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

    public void addTrack(Path path) {playpane.add(path);}

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setBackground(isSelected ? Color.green.darker() : null);

        if (selected) {
            BackVocalFrame.showPlayList(playpane);

            if (playpane != null && playpane.getRowsCount() > 0) {
//                BackVocalFrame.setProgress(100 / playpane.getRowsCount() * (indexOfPlayed + 1));
                BackVocalFrame.updatePlayedLabelText();
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
        return playpane.getTrack(indexOfPlayed).toFile().getName();
    }


    public void moveSelectedUp() {
        if (indexOfPlayed == playpane.getSelectedIndex()) {
            if (playpane.moveSelectedUp()) {
                indexOfPlayed--;
            }
        } else {
            playpane.moveSelectedUp();
        }
    }

    public void removeSelected() { // indexOfPlayed
        if (indexOfPlayed == playpane.getSelectedIndex()) {
            playpane.getOwner().stop();
        } else {
            if (indexOfPlayed > playpane.getSelectedIndex()) {
                indexOfPlayed--;
            }
        }

        playpane.removeSelected();
    }

    public void moveSelectedDown() {
        if (playpane.getSelectedIndex() == -1) {return;}

        if (indexOfPlayed == playpane.getSelectedIndex()) {
            if (playpane.moveSelectedDown()) {
                indexOfPlayed++;
            }
        } else {
            playpane.moveSelectedDown();
        }
    }


    public PlayPane getPlayPane() {return playpane;}

    public void addAlarm(String time, Path track) {
        arm.addElement(new AlarmItem(time, track));
        alarmsBack = Color.GREEN;
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

    public int getIndexOfPlayed() {return indexOfPlayed;}

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {

            case "alarmBtn": new AlarmsDialog(BackVocalFrame.getFrame());
                break;

            case "play":
                if (inSchedulingTimeAccept()) {
                    isHandStopped = false;
                    stop();

                    if (playpane.getSelectedIndex() != playpane.getPlayedIndex()) {
                        indexOfPlayed = playpane.getSelectedIndex();
                        play();
                        return;
                    }

                    play();
                    setSelected(true);
                    playpane.repaint();
                } else {
                    JOptionPane.showConfirmDialog(PlayDataItem.this, "Its not a schedule time!", "Not yet:", JOptionPane.DEFAULT_OPTION);
                }
                break;

            case "next":
                if (inSchedulingTimeAccept()) {
                    stop();
//                  playpane.selectRow(playpane.getSelectedIndex() + 1 >= playpane.getRowsCount() ? 0 : playpane.getSelectedIndex() + 1);
                    playNext();
//                    setSelected(true);

                    playpane.repaint();
                }
                break;

            case "stop":
                isHandStopped = true;
                stop();
                break;



            default:
        }
    }


    // subframes:
    private class AlarmsDialog extends JDialog {

        public AlarmsDialog(JFrame parent) {
            super(parent, "Alarms list:", true);

            setMinimumSize(new Dimension(400, 400));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel basePane = new JPanel() {
                {
                    setBackground(Color.MAGENTA);
                    setLayout(new BorderLayout());

                    alarmList.setBackground(Color.DARK_GRAY);
                    alarmList.setForeground(Color.WHITE);

                    JScrollPane centerAlarmsListPane = new JScrollPane(alarmList) {
                        {
                            setBorder(null);
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
                                                try {
                                                    alarmFilePath = fch.getSelectedFile().toPath();

                                                    if (!alarmInitTime.isBlank() &&
                                                            alarmInitTime.length() == 8 &&
                                                            alarmInitTime.contains(":") &&
                                                            alarmInitTime.split(":").length == 3 &&
                                                            Integer.parseInt(alarmInitTime.split(":")[0]) < 23 &&
                                                            Integer.parseInt(alarmInitTime.split(":")[1]) < 59 &&
                                                            Integer.parseInt(alarmInitTime.split(":")[2]) < 59
                                                    ) {
                                                        arm.addElement(new AlarmItem(alarmInitTime, alarmFilePath));
                                                        alarmsBack = Color.GREEN;
                                                    } else {
                                                        JOptionPane.showConfirmDialog(AlarmsDialog.this, "Wrong data!", "Canceled.", JOptionPane.DEFAULT_OPTION);
                                                    }
                                                } catch (Exception e2) {
                                                    e2.printStackTrace();
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
                                            int req = JOptionPane.showConfirmDialog(AlarmsDialog.this, "Delete alarm on " + toDelete.getTime() + "?", "Confirm:", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
                                            if (req == 0) {
                                                arm.removeElement(toDelete);
                                                if (arm.size() == 0) {
                                                    alarmsBack = Color.DARK_GRAY;
                                                }
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
}
