package fox.utils;

import fox.components.PlayPane;

import javax.swing.*;


public class CustomList<AlarmItem> extends JList<AlarmItem> {

    private PlayPane owner;

    public CustomList(ListModel<AlarmItem> dlm, PlayPane owner) {
        super(dlm);
        this.owner = owner;
    }

    public int getPlayedRowIndex() {
        return owner.getPlayedIndex();
    }
}
