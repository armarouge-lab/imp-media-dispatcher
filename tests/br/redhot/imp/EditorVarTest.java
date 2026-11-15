package br.redhot.imp;

import java.nio.file.Files;
import java.nio.file.Path;

public class EditorVarTest {

    private static Path tmp(String content) throws Exception {
        Path p = Files.createTempFile("imp-editor-", ".conf");
        Files.writeString(p, content);
        p.toFile().deleteOnExit();
        return p;
    }

    public static void run(TestRunner r) throws Exception {
        Logger log = new Logger();

        r.test("editor.expand");
        Path p = tmp("version=1\neditor=cat\next.txt=$editor\n");
        ImpConfig cfg = ConfigLoader.load(p.toString(), log);
        r.eq("cat", cfg.rules().get("ext.txt"), "ext.txt expande para cat");

        r.test("editor.missing");
        p = tmp("version=1\next.txt=$editor\n");
        cfg = ConfigLoader.load(p.toString(), log);
        r.eq("$editor", cfg.rules().get("ext.txt"), "fica literal sem editor=");

        r.test("editor.ordering");
        p = tmp("version=1\next.txt=$editor\neditor=cat\n");
        cfg = ConfigLoader.load(p.toString(), log);
        r.eq("cat", cfg.rules().get("ext.txt"), "expande com editor= depois");

        r.test("editor.unused");
        p = tmp("version=1\neditor=cat\next.txt=devil\n");
        cfg = ConfigLoader.load(p.toString(), log);
        r.eq("devil", cfg.rules().get("ext.txt"), "sem $editor, sem alteracao");

        r.test("editor.mime-expand");
        p = tmp("version=1\neditor=cat\nmime.text/plain=$editor\n");
        cfg = ConfigLoader.load(p.toString(), log);
        r.eq("cat", cfg.rules().get("mime.text/plain"), "mime.* tambem expande");
    }
}
