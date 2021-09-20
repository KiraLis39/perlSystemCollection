package fox.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;


public class MyCellRenderer extends JPanel implements ListCellRenderer {
    private JLabel label = null;
    private int cellHeight;

    public MyCellRenderer(int cellHeight) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.cellHeight = cellHeight;
//        setOpaque(true);

        label = new JLabel();
//        label.setOpaque(false);

        add(label);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf) {
        BufferedImage ico = ((ListItem) value).getIcon();
        if (ico.getWidth() >= cellHeight || ico.getHeight() >= cellHeight) {
            BufferedImage tmp = new BufferedImage(cellHeight - 8, cellHeight - 8, BufferedImage.TYPE_INT_ARGB);
            Graphics g = tmp.getGraphics();
            g.drawImage(ico, 0,0,tmp.getWidth(), tmp.getHeight(), null);
            g.dispose();
            ico = tmp;
        }

        label.setIcon(new ImageIcon(ico));
        label.setText(((ListItem) value).getText());

//        label.setBorder(new EmptyBorder(0,0,0,0));

        if (iss) {
            setBackground(Color.lightGray);
            label.setForeground(Color.BLACK);
        } else {
            setBackground(list.getBackground());
            label.setForeground(Color.WHITE);
        }


        return this;
    }
}
