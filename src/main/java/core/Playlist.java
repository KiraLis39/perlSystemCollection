package core;

import fox.out.Out;
import gui.BackVocalFrame;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;


public class Playlist extends JPanel {
    private List<Path> musicFilesList = new LinkedList<>();
    private DefaultListModel<String> dlm = new DefaultListModel<>();

    public Playlist(PlayDateItem player, List<Path> musicFilesList) {
        this.musicFilesList = musicFilesList;

        setName(player.getName());
        setBackground(Color.MAGENTA);
        setLayout(new BorderLayout(3,3));

        reload();
    }

    public boolean isTracksExists() {
        return !dlm.isEmpty();
    }

    private void reload() {
        if (musicFilesList == null || musicFilesList.size() == 0) {return;}
        Out.Print("Reloading playlist...");

        removeAll();
        dlm.clear();

        System.out.println("IN DIR HAS " + musicFilesList.size() + " files mp3.");
        for (Path file : musicFilesList) {
            System.out.println("Adding to pl: " + file.toFile().getName());
            dlm.addElement(file.toFile().getName());
        }

        add(new JList<>(dlm) {{
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }});

        BackVocalFrame.showPlayList(this);
    }

    public Path getTrack(int index) {
        return musicFilesList.get(index);
    }

    public int getTracksCount() {return dlm.size();}
}
