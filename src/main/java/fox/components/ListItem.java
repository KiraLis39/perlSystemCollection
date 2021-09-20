package fox.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class ListItem {
    private BufferedImage icon = null;
    private String text;

    public ListItem(File iconFile, String text) {
        try {icon = ImageIO.read(iconFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.text = text;
    }

    public BufferedImage getIcon() {
        return icon;
    }


    public String getText() {
        return text;
    }
}
