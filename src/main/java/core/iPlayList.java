package core;

import java.nio.file.Path;

public interface iPlayList {
    int getRowsCount();
    void selectRow(int rowIndex);
    void moveSelectedUp();
    void moveSelectedDown();
    void removeSelected();
    void add(String fileName);
    Path getTrack(int index);
    boolean isEmpty();
}
