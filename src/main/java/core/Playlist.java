package core;

import gui.BackVocalFrame;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class Playlist extends JPanel {
    private PlayDateItem owner;
    private Path musicDir;
    private DefaultListModel<String> dlm = new DefaultListModel<>();

    public Playlist(Path musicDir) {
        this.musicDir = musicDir;

        setBackground(Color.MAGENTA);
        setLayout(new BorderLayout(3,3));

        JList<String> newList = new JList<>(dlm);
        newList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(newList);
    }

    public boolean isTracksExists() {
        return !dlm.isEmpty();
    }

    public void setMusicDir(Path plDir) {
        this.musicDir= plDir;

        try {reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reload() throws IOException {
        removeAll();
        dlm.clear();

        File[] musicArray = Arrays.stream(musicDir.toFile().listFiles()).filter(m -> (m.getName().endsWith(".mp3"))).toArray(File[]::new);
        System.out.println("IN DIR HAS " + musicArray.length + " files mp3.");

        for (File file : musicArray) {
            System.out.println("Adding to pl: " + file.getName());
            dlm.addElement(file.getName());
        }

        JList<String> newList = new JList<>(dlm);
        add(newList);

        BackVocalFrame.showPlayList(this);
    }

    public void setOwner(PlayDateItem owner) {
        this.owner = owner;
        setName(owner.getName());
    }

    public int getTracksCount() {
        return dlm.size();
    }

    public Path getMusicDirectory() {
        return musicDir;
    }
}
