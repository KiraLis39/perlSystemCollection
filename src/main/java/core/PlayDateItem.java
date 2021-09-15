package core;

import gui.BackVocalFrame;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class PlayDateItem extends JPanel implements MouseListener {
    private Playlist playlist;
    private Color defBkgColor;
    private boolean isSelected = false;
    private Long timerIn, timerOut, alarmTime;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(isSelected ? Color.GREEN : defBkgColor);
        g.fillRect(0,0,getWidth(),getHeight());

        g.setColor(playlist.isTracksExists() ? Color.ORANGE : Color.DARK_GRAY);
        g.fillOval(getWidth() - 64, 8, 16, 16);
    }

    public PlayDateItem(String name, Playlist playlist) {
        this(name, playlist, null);
    }

    public PlayDateItem(String name, Playlist playlist, Color backgroundColor) {
        setName(name);
        this.playlist = playlist;
        playlist.setOwner(this);
        defBkgColor = backgroundColor == null ? Color.WHITE : backgroundColor;

        setBackground(defBkgColor);
        setLayout(new BorderLayout(3, 3));

        add(new JLabel(playlist.getName()), BorderLayout.NORTH);

        setBorder(new EmptyBorder(3,3,3,3));
        addMouseListener(this);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        setBackground(isSelected ? Color.green : null);
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        BackVocalFrame.resetRightPaneSelect();
        setSelected(true);
        BackVocalFrame.showPlayList(playlist);
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {setBackground(Color.YELLOW);}

    @Override
    public void mouseExited(MouseEvent e) {setBackground(null);}

    @Override
    public void setBackground(Color bg) {
        super.setBackground(isSelected ? Color.GREEN : bg == null ? defBkgColor : bg);
    }

    public Playlist getPlayList() {return playlist;}

    @Override
    public String toString() {
        return "PDate item '" + getName() + "' (" + playlist.getTracksCount() + " tracks)";
    }

    public void saveToFile() {
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("./resources/scheduler/" + getName() + ".db"))) {
            osw.write(
                    "::T_IN:" + getTimerIn() +
                    "::T_OUT:" + getTimerOut() +
                    "::ALARM:" + getAlarmTime() +
                    "::PATH:" + playlist.getMusicDirectory()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getAlarmTime() {
        return alarmTime;
    }

    private Long getTimerOut() {
        return timerOut;
    }

    private Long getTimerIn() {
        return timerIn;
    }
}
