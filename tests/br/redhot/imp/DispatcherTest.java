package br.redhot.imp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DispatcherTest {

    private static ImpConfig cfg(Map<String, String> rules, String fallback) {
        return new ImpConfig(1, fallback, false, "file", "error", "",
                Collections.unmodifiableMap(new LinkedHashMap<>(rules)));
    }

    private static Detector.ResolvedRule rule(String cmd) {
        return new Detector.ResolvedRule("ext.test", cmd);
    }

    public static void run(TestRunner r) throws Exception {
        Logger log = new Logger();
        Path file = Files.createTempFile("disp-", ".test");
        Files.writeString(file, "x");
        file.toFile().deleteOnExit();

        r.test("dispatch.dry-run");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream orig = System.out;
        int code;
        try {
            System.setOut(new PrintStream(buf));
            code = Dispatcher.dispatch(cfg(Map.of("ext.test", "cat"), "cat"),
                    file, rule("cat"), true, log);
        } finally {
            System.setOut(orig);
        }
        r.eq(ExitCode.EX_OK.code(), code, "dry-run exit 0");
        r.ok(buf.toString().contains("would dispatch: cat"), "printed plan");

        r.test("dispatch.handler-true");
        code = Dispatcher.dispatch(cfg(Map.of("ext.test", "true"), "true"),
                file, rule("true"), false, log);
        r.eq(ExitCode.EX_OK.code(), code, "true → EX_OK");

        r.test("dispatch.handler-false-with-fallback");
        code = Dispatcher.dispatch(cfg(Map.of("ext.test", "false"), "true"),
                file, rule("false"), false, log);
        r.eq(ExitCode.EX_OK.code(), code, "false then true → EX_OK");

        r.test("dispatch.all-fail");
        code = Dispatcher.dispatch(cfg(Map.of("ext.test", "false"), "false"),
                file, rule("false"), false, log);
        r.eq(ExitCode.EX_EXEC_FAILED.code(), code, "both false → EX_EXEC_FAILED");

        r.test("dispatch.no-handler");
        code = Dispatcher.dispatch(cfg(Map.of(), ""), file, null, false, log);
        r.eq(ExitCode.EX_NO_HANDLER.code(), code, "no rule + no fallback → EX_NO_HANDLER");

        r.test("dispatch.not-on-path");
        code = Dispatcher.dispatch(cfg(Map.of("ext.test", "nonexistent_xyz_12345"), "true"),
                file, rule("nonexistent_xyz_12345"), false, log);
        r.eq(ExitCode.EX_OK.code(), code, "missing handler → true fallback → EX_OK");
    }
}
