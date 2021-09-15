package core;

import gui.BackVocalFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PlayDateItem extends JPanel implements MouseListener {

    private String weakdayName;
    private Color defBkgColor;

    public PlayDateItem(String weakDayName) {
        this(weakDayName, null);
    }

    public PlayDateItem(String weakDayName, Color backgroundColor) {
        defBkgColor = backgroundColor;
        setBackground(defBkgColor);
        setLayout(new BorderLayout(3, 3));
//        setPreferredSize(new Dimension(BackVocalFrame.getRightPaneWidth() - 6,100));
        add(new JLabel(weakDayName), BorderLayout.NORTH);
        this.weakdayName = weakDayName;

        setBorder(new EmptyBorder(3,3,3,3));
        addMouseListener(this);
    }

    public String getWeakdayName() {
        return weakdayName;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setBackground(Color.YELLOW);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setBackground(defBkgColor);
    }
}
