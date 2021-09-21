package fox.components;

import java.nio.file.Path;


public class AlarmItem {
    private String time;
    private Path track;
    private boolean wasPlayed = false;

    public AlarmItem(String alarmInitTime, Path alarmFilePath) {
        this.time = alarmInitTime;
        this.track = alarmFilePath;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Path getTrack() {
        return track;
    }

    public void setTrack(Path track) {
        this.track = track;
    }

    @Override
    public String toString() {
        return time + " >> '" + track.toFile().getName() + "'";
    }

    public void wasPlayed(boolean wasPlayed) {
        this.wasPlayed = wasPlayed;
    }

    public boolean isWasPlayed() {return this.wasPlayed;
    }
}
