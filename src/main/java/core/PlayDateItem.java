package core;

import fox.out.Out;
import gui.BackVocalFrame;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;


public class PlayDateItem extends JPanel implements MouseListener {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

    private AdvancedPlayer musicPlayer;
    private Path plDirectory;
    private Playlist playlist;
    private int currentPlayedIndex = 0;

    private Color defBkgColor = Color.WHITE, secondColor;
    private Instant timerIn, timerOut, alarmTime;

    private boolean isSelected = false, repeat = false;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(isSelected ? Color.GREEN : defBkgColor);
        g.fillRect(0,0,getWidth(),getHeight());

        g.setColor(playlist != null && playlist.isTracksExists() ? Color.ORANGE : Color.DARK_GRAY);
        g.fillOval(getWidth() - 19, 3, 16, 16);
    }


    public PlayDateItem(String name, Path plDirectory, Instant _timerIn, Instant _timerOut, Instant _alarmTime, Boolean _repeat) {
        setName(name);
        this.plDirectory = plDirectory;
        this.timerIn = _timerIn;
        this.timerOut = _timerOut;
        this.alarmTime = _alarmTime;
        this.repeat = _repeat;
        secondColor = defBkgColor;
        if (getName().equals("Saturday") || getName().equals("Sunday")) {
            defBkgColor = Color.CYAN;
        }
        try {
            playlist = new Playlist(this,
                    Files.walk(plDirectory, 1).filter(m -> (m.toFile().getName().endsWith(".mp3"))).collect(Collectors.toList()));
        } catch (NoSuchFileException npe) {
            Out.Print(getName() + " player has no playlist!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        setBackground(defBkgColor);
        setLayout(new BorderLayout(3, 3));

        JLabel dayNameLabel = new JLabel(getName());

        JPanel dayControlPane = new JPanel(new GridLayout(2,4)) {
            {
                JPanel inTimePane = new JPanel(new BorderLayout(3,3)) {
                    {
                        JSpinner timeSpinner = new JSpinner( new SpinnerDateModel() );
                        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
                        timeSpinner.setEditor(timeEditor);
                        timeSpinner.setValue(Date.from(timerIn));
                        timeSpinner.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                timerIn = ((Date) timeSpinner.getValue()).toInstant();
                            }
                        });

                        add(new JLabel("In:"), BorderLayout.NORTH);
                        add(timeSpinner, BorderLayout.CENTER);
                    }
                };

                JPanel outTimePane = new JPanel(new BorderLayout(3,3)) {
                    {
                        JSpinner timeSpinner = new JSpinner( new SpinnerDateModel() );
                        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
                        timeSpinner.setEditor(timeEditor);
                        timeSpinner.setValue(Date.from(timerOut));
                        timeSpinner.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                timerOut = ((Date)timeSpinner.getValue()).toInstant();
                            }
                        });

                        add(new JLabel("Out:"), BorderLayout.NORTH);
                        add(timeSpinner, BorderLayout.CENTER);
                    }
                };

                JPanel alarmTimePane = new JPanel(new BorderLayout(3,3)) {
                    {
                        JSpinner timeSpinner = new JSpinner( new SpinnerDateModel() );
                        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
                        timeSpinner.setEditor(timeEditor);
                        timeSpinner.setValue(Date.from(alarmTime));
                        timeSpinner.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                alarmTime = ((Date)timeSpinner.getValue()).toInstant();
                            }
                        });

                        add(new JLabel("Alarm:"), BorderLayout.NORTH);
                        add(timeSpinner, BorderLayout.CENTER);
                    }
                };

                JPanel repeatPane = new JPanel(new BorderLayout(3,3)) {
                    {
                        JCheckBox italicBox = new JCheckBox("Repeat");
                        italicBox.setSelected(repeat);
                        italicBox.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                repeat = italicBox.isSelected();
                            }
                        });

                        add(new JLabel("Options:"), BorderLayout.NORTH);
                        add(italicBox, BorderLayout.WEST);

                        JButton startPlayBtn = new JButton(">") {
                            {
                                addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        play();
                                    }
                                });
                            }
                        };

                        add(startPlayBtn, BorderLayout.CENTER);
                    }
                };

                add(inTimePane, BorderLayout.WEST);
                add(outTimePane, BorderLayout.WEST);
                add(alarmTimePane, BorderLayout.WEST);
                add(repeatPane, BorderLayout.WEST);
            }
        };

        add(dayNameLabel, BorderLayout.NORTH);
        add(dayControlPane, BorderLayout.CENTER);

        setBorder(new EmptyBorder(3,3,3,3));
        addMouseListener(this);
    }

    public void saveToFile() {
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("./resources/scheduler/" + getName() + ".db"), Charset.forName("UTF-8"))) {
            osw.write(
                    "NN_T_IN_EE" + getTimerIn() +
                            "NN_T_OUT_EE" + getTimerOut() +
                            "NN_ALARM_EE" + getAlarmTime() +
                            "NN_PATH_EE" + plDirectory +
                            "NN_REP_EE" + repeat
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Audio control:
    private void play() {
        stopMusic();

        new Thread(() -> {
            try (InputStream fis = new FileInputStream(playlist.getTrack(currentPlayedIndex).toFile())) {
                musicPlayer = new AdvancedPlayer(fis);
                musicPlayer.play();
                currentPlayedIndex++;
            } catch (IOException | JavaLayerException e) {e.printStackTrace();
            } finally {musicPlayer.close();}
        }).start();

        Out.Print("Media: music: the '" + playlist.getTrack(currentPlayedIndex).toFile().getName() + "' exist into musicMap and play now...");

    }

    public void stopMusic() {
        try {musicPlayer.close();
        } catch (Exception e) {/* IGNORE */}
    }


    // Getters & setters:
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setBackground(isSelected ? Color.green : null);
    }
    public boolean isSelected() {return this.isSelected;}

    private Instant getAlarmTime() {return alarmTime;}
    private Instant getTimerOut() {return timerOut;}
    private Instant getTimerIn() {return timerIn;}

    public void setPlayList(Path newPlayListPath) {
        try {
            playlist = new Playlist(
                    this,
                    Files.walk(newPlayListPath, 1)
                            .filter(m -> (m.toFile().getName().endsWith(".mp3")))
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Listeners:
    @Override
    public void mouseClicked(MouseEvent e) {
        BackVocalFrame.resetRightPaneSelect();
        setSelected(true);
        BackVocalFrame.showPlayList(playlist);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        defBkgColor = Color.YELLOW;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
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
        return "PDate item '" + getName() + "' (" + playlist.getTracksCount() + " tracks)";
    }
}
