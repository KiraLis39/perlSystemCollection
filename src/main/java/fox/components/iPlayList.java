package fox.components;

import java.nio.file.Path;

public interface iPlayList {
    int getRowsCount();
    void selectRow(int rowIndex);
    void moveSelectedUp();
    void moveSelectedDown();
    void removeSelected();
    void add(Path fileName);
    Path getTrack(int index);
    boolean isEmpty();
}
