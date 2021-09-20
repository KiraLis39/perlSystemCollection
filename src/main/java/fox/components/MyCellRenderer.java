package fox.components;

import fox.fb.FoxFontBuilder;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class MyCellRenderer extends JPanel implements ListCellRenderer {
    private JButton label;
    private static int cellHeight;
    private Font trackSelectedFont = FoxFontBuilder.setFoxFont(FoxFontBuilder.FONT.CONSOLAS, 14, true);

    public static int getCellHeight() { return cellHeight; }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public MyCellRenderer(int cellHeight) {
        this.cellHeight = cellHeight;

        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        label = new JButton() {
            {
                setHorizontalTextPosition(JButton.RIGHT);
                setHorizontalAlignment(JButton.LEFT);
            }
        };

        add(label, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf) {
        setEnabled(list.isEnabled());
        setFont(list.getFont());

        BufferedImage ico = ((ListRow) value).getImIcon();
        if (ico.getWidth() >= cellHeight || ico.getHeight() >= cellHeight) {
            BufferedImage tmp = new BufferedImage(cellHeight - 8, cellHeight - 8, BufferedImage.TYPE_INT_ARGB);
            Graphics g = tmp.getGraphics();
            g.drawImage(ico, 0,0,tmp.getWidth(), tmp.getHeight(), null);
            g.dispose();
            ico = tmp;
        }

        label.setIcon(new ImageIcon(ico));
        label.setText("<html><b>(" + ((ListRow) value).getCount() + ")</b> " + ((ListRow) value).getListItemText());
//        label.setBorder(new EmptyBorder(0,0,0,0));

        if (iss) {
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
            label.setFont(trackSelectedFont);
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(Color.WHITE);
            label.setFont(null);
        }

        return this;
    }
}
