package door;

import fox.iom.IOM;
import fox.iom.IOMs;
import fox.out.Out;
import gui.BackVocalFrame;
import registry.Codes;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class MainClass {
    private static Path[] importantDirs;

    public static Long getStartTime() {
        return startTime;
    }

    private static Long startTime;

    public static void main(String[] args) {
        startTime = System.currentTimeMillis();

        try {
            checkImportantDirectoriesExists();

            buildIOM();

            Out.setEnabled(IOM.getBoolean(IOM.HEADERS.CONFIG, IOMs.CONFIG.OUTLOG_ENABLED));
            Out.setLogsCoutAllow(3);

            Out.Print("Start!");

            loadUIM();

            new BackVocalFrame();

        } catch (Exception e) {
            Out.Print("Has error in main: " + e.getMessage(), Out.LEVEL.ERROR);
            e.printStackTrace();
        }
    }


    private static void loadUIM() {
        Out.Print("Set the UIManagers view.");

        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
                Out.Print("Has a some problem with a loading UI manager..", Out.LEVEL.WARN);
            }
        }
    }


    private static void buildIOM() {
        IOM.setConsoleOutOn(false);
        IOM.add(IOM.HEADERS.CONFIG, Paths.get("./resources/config.prop").toFile());


    }

    private static void checkImportantDirectoriesExists() throws IOException {
        Out.Print("Check the important directories...");
        importantDirs = new Path[] {
                Paths.get("./resources/audio/music"),
                Paths.get("./resources/audio/sound"),
                Paths.get("./resources/scheduler/"),
                Paths.get("./resources/icons/")
        };

        for (Path importantDir : importantDirs) {
            if (Files.notExists(importantDir)) {
                Files.createDirectories(importantDir);
            }
        }

    }

    public static void exit(Codes code) {
        IOM.saveAll();

        Out.Print("Finish with code: " + code);
        System.exit(code.code());
    }
}
