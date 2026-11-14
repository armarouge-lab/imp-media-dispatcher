package br.redhot.imp;

public class Logger {

    public enum Level {
        ERROR, WARN, INFO, DEBUG
    }

    private Level threshold = Level.WARN;

    public void setThreshold(Level level) {
        this.threshold = level;
    }

    public void error(String action, String detail) { log(Level.ERROR, action, detail); }
    public void warn(String action, String detail)  { log(Level.WARN,  action, detail); }
    public void info(String action, String detail)  { log(Level.INFO,  action, detail); }
    public void debug(String action, String detail) { log(Level.DEBUG, action, detail); }

    public void log(Level level, String action, String detail) {
        if (level.ordinal() > threshold.ordinal()) {
            return;
        }
        long epoch = System.currentTimeMillis() / 1000L;
        System.err.printf("%d %s %s %s%n", epoch, level.name(), action, detail);
    }
}
