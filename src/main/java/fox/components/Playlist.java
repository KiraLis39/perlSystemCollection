package fox.components;

import fox.out.Out;
import gui.BackVocalFrame;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class Playlist extends JPanel implements iPlayList {
    private int indexGlobalCounter = 0;

//    private List<Path> musicFilesList;
    private DefaultListModel<ListRow> dlm = new DefaultListModel();
    private JList<ListRow> playList;
//    private PlayDataItem owner;
    private ArrayList<Path> tracks = new ArrayList<>();

    public Playlist(PlayDataItem player) {
//        this.owner = player;

        setName(player.getName());
        setLayout(new BorderLayout(0,0));
        setOpaque(false);

        reload();
    }

    private void reload() {
        if (tracks == null || tracks.size() == 0) {return;}
        Out.Print("Reloading playlist...");

        removeAll();
        dlm.clear();

        System.out.println("IN DIR HAS " + tracks.size() + " files mp3.");
        for (Path path : tracks) {
            System.out.println("Adding to pl: " + path.toFile().getName());
            add(path);
        }

        playList = new JList(dlm) {
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
        indexGlobalCounter++;
        dlm.addElement(new ListRow(this, indexGlobalCounter, new File("./resources/icons/0.png"), trackPath.toString()));
        tracks.add(trackPath);
    }

    @Override
    public Path getTrack(int index) {
        return tracks.get(index);
    }

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
        dlm.getElementAt(getSelectedIndex() + 1).setCount(getSelectedIndex() + 1);
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

    public Iterable<? extends Path> getTracks() {return tracks;}

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

    public void clearTracks() {tracks.clear();}
}
