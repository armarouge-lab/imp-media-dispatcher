package br.redhot.imp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LoggerTest {

    public static void run(TestRunner r) throws Exception {
        // captura stderr
        PrintStream originalErr = System.err;

        r.test("logger.warn-default");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buf));
        try {
            Logger log = new Logger();
            log.info("test", "info-msg");     // filtrado (info < warn)
            log.warn("test", "warn-msg");     // passa
        } finally {
            System.setErr(originalErr);
        }
        String out = buf.toString();
        r.ok(!out.contains("INFO"), "INFO filtrado por default WARN");
        r.ok(out.contains("WARN"), "WARN passa");

        r.test("logger.info-threshold");
        buf = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buf));
        try {
            Logger log = new Logger();
            log.setThreshold(Logger.Level.INFO);
            log.info("test", "info-msg");
        } finally {
            System.setErr(originalErr);
        }
        r.ok(buf.toString().contains("INFO"), "INFO passa com threshold=INFO");

        r.test("logger.error-threshold");
        buf = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buf));
        try {
            Logger log = new Logger();
            log.setThreshold(Logger.Level.ERROR);
            log.warn("test", "warn-msg");     // filtrado
            log.error("test", "error-msg");   // passa
        } finally {
            System.setErr(originalErr);
        }
        out = buf.toString();
        r.ok(!out.contains("WARN"), "WARN filtrado com ERROR");
        r.ok(out.contains("ERROR"), "ERROR passa");
    }
}
