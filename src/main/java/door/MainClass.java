package door;

import fox.iom.IOM;
import fox.iom.IOMs;
import fox.out.Out;
import gui.BackVocalFrame;
import registry.Codes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainClass {

    private static Path[] impotrantDirs;

    public static void main(String[] args) {

        try {
            checkImportantDirectoriesExists();

            buildIOM();

            Out.setEnabled(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.OUTLOG_ENABLED));
            Out.setLogsCoutAllow(3);

            Out.Print("Start!");

            new BackVocalFrame();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void buildIOM() {
        IOM.setConsoleOutOn(false);
        IOM.add(IOM.HEADERS.CONFIG, Paths.get("./resources/config.prop").toFile());


    }

    private static void checkImportantDirectoriesExists() throws IOException {

        impotrantDirs = new Path[] {
                Paths.get("./resources/audio/music"),
                Paths.get("./resources/audio/sound"),
                Paths.get("./resources/audio/meta"),
                Paths.get("./resources/scheduler/")
        };

        for (Path impotrantDir : impotrantDirs) {
            if (Files.notExists(impotrantDir)) {
                Files.createDirectories(impotrantDir);
            }
        }

    }

    public static void exit(Codes code) {
        IOM.saveAll();

        Out.Print("Finish with code: " + code);
        System.exit(code.code());
    }
}
