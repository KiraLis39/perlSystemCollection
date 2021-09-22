package fox.utils;

import java.nio.file.Path;

public interface iPlayList {
    int getRowsCount();
    void selectRow(int rowIndex);
    boolean moveSelectedUp();
    boolean moveSelectedDown();
    void removeSelected();
    void add(Path fileName);
    Path getTrack(int index);
    boolean isEmpty();
}
