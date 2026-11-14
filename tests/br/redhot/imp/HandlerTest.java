package br.redhot.imp;

import java.nio.file.Path;
import java.util.List;

public class HandlerTest {

    public static void run(TestRunner r) throws Exception {
        r.test("handler.available");
        r.eq(true, Handler.available("sh"), "sh available");
        r.eq(true, Handler.available("true"), "true available");
        r.eq(false, Handler.available("nonexistent_cmd_xyz_12345"), "nonexistent not available");
        r.eq(false, Handler.available(null), "null not available");
        r.eq(false, Handler.available(""), "empty not available");
        r.eq(false, Handler.available("/proc/naoexiste"), "missing absolute path");

        r.test("handler.toArgv");
        Handler h = new Handler("cat", Path.of("/tmp/x.txt"));
        r.eq(List.of("cat", "/tmp/x.txt"), h.toArgv(), "argv list");
        r.eq("cat", h.command(), "command field");
        r.eq(Path.of("/tmp/x.txt"), h.file(), "file field");

        r.test("handler.isAvailable");
        r.eq(true, new Handler("sh", Path.of("/tmp/x")).isAvailable(), "instance sh");
        r.eq(false, new Handler("nonexistent_xyz_12345", Path.of("/tmp/x")).isAvailable(),
                "instance nonexistent");
    }
}
