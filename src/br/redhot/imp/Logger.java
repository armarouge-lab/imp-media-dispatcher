package br.redhot.imp;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Logger {

    public enum Level {
        ERROR, WARN, INFO, DEBUG
    }

    private Level threshold = Level.WARN;
    private PrintWriter fileWriter;

    public void setThreshold(Level level) {
        this.threshold = level;
    }

    public void setLogFile(Path path) throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
        }
        this.fileWriter = new PrintWriter(
                Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND),
                true);
    }

    public void error(String action, String detail) { log(Level.ERROR, action, detail); }
    public void warn(String action, String detail)  { log(Level.WARN,  action, detail); }
    public void info(String action, String detail)  { log(Level.INFO,  action, detail); }
    public void debug(String action, String detail) { log(Level.DEBUG, action, detail); }

    public synchronized void log(Level level, String action, String detail) {
        if (level.ordinal() > threshold.ordinal()) {
            return;
        }
        long epoch = System.currentTimeMillis() / 1000L;
        String line = String.format("%d %s %s %s", epoch, level.name(), action, detail);
        System.err.println(line);
        if (fileWriter != null) {
            fileWriter.println(line);
        }
    }
}
