package fox.components;

import fox.out.Out;
import fox.utils.CustomList;
import fox.utils.MyCellRenderer;
import fox.utils.iPlayList;
import gui.BackVocalFrame;
import gui.PlayDataItem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class PlayPane extends JPanel implements iPlayList {
    private int indexGlobalCounter = 0;

    private ArrayList<Path> tracks = new ArrayList<>();
    private DefaultListModel<ListRow> dlm = new DefaultListModel();
    private CustomList<ListRow> playList;
    private PlayDataItem owner;


    public PlayPane(PlayDataItem player) {
        this.owner = player;
        setName(player.getName());
        setLayout(new BorderLayout(0,0));
        setOpaque(false);
    }

    private void reload() {
        if (tracks == null || tracks.size() == 0) {
            Out.Print("tracks is empty so return...");
            return;
        }
        Out.Print("Reloading playlist to " + tracks.size() + " files mp3.");

        removeAll();
        dlm.clear();

        indexGlobalCounter = 0;
        for (Path path : tracks) {
            Out.Print("Was added to playlist the track: " + path);
            indexGlobalCounter++;
            dlm.addElement(new ListRow(this, indexGlobalCounter, new File("./resources/icons/0.png"), path));
        }

        playList = new CustomList(dlm, PlayPane.this) {
            {
                setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                setFixedCellHeight(32);
//              setVisibleRowCount(5);
                setBackground(Color.DARK_GRAY);
                setCellRenderer(new MyCellRenderer(32));
            }
        };

        add(playList, BorderLayout.CENTER);

        BackVocalFrame.showPlayList(this);
    }

    @Override
    public void add(Path trackPath) {
        if (Files.exists(trackPath)) {
            tracks.add(trackPath);
            reload();
        }
    }

    @Override
    public Path getTrack(int index) {
        return dlm.getElementAt(index).getPath();
    }

    public int getPlayedIndex() {return owner.getIndexOfPlayed();}

    @Override
    public boolean isEmpty() {
        return dlm.isEmpty();
    }

    @Override
    public int getRowsCount() {
        return dlm.size();
    }

    @Override
    public void selectRow(int rowIndex) {
        playList.setSelectedIndex(rowIndex);
    }


    @Override
    public void moveSelectedUp() {
        int index = getSelectedIndex();
        if (index > 0) {
            ListRow tmp = dlm.getElementAt(index);

            dlm.getElementAt(index - 1).setCount(index + 1);
            tmp.setCount(index);

            dlm.removeElementAt(index);
            dlm.insertElementAt(tmp,index - 1);
            playList.setSelectedIndex(index - 1);
        }
    }

    @Override
    public void moveSelectedDown() {
        int index = getSelectedIndex();
        if (index < dlm.size() - 1) {
            ListRow tmp = dlm.getElementAt(index);

            dlm.getElementAt(index).setCount(index + 2);
            dlm.getElementAt(index + 1).setCount(index + 1);

            dlm.removeElementAt(index);
            dlm.insertElementAt(tmp,index + 1);
            playList.setSelectedIndex(index + 1);
        }
    }

    @Override
    public void removeSelected() {
        tracks.remove(getTrack(getSelectedIndex()));

        for (int i = getSelectedIndex() + 1; i < dlm.size(); i++) {
            dlm.getElementAt(i).setCount(i);
        }

        dlm.removeElementAt(getSelectedIndex());
    }

    public int getSelectedIndex() {
        int si = playList.getSelectedIndex();
        if (si == -1) {
            playList.setSelectedIndex(0);
            si = 0;
        }
        return si;
    }

    public List<Path> getTracks() {
        List<Path> result = new ArrayList<>();
        for (int i = 0; i < dlm.size(); i++) {
            result.add(dlm.get(i).getPath());
        }
        return result;
    }

    public void setTracks(File[] tracks) {
        try {
            for (File file : tracks) {
                if (Files.isRegularFile(file.toPath())) {
                    if (file.getName().endsWith(".mp3")) {
                        System.out.println("Find the track: '" + file + "'...");
                        add(file.toPath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTracks(File tracksDirectory) {
        try {
            for (File file : tracksDirectory.listFiles()) {
                if (Files.isRegularFile(file.toPath())) {
                    if (file.getName().endsWith(".mp3")) {
                        System.out.println("Find the track: '" + file + "'...");
                        add(file.toPath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearTracks() {
        tracks.clear();
        dlm.clear();
    }
}
