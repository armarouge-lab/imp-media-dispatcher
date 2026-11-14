package br.redhot.imp;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoaderTest {

    private static Path tmp(String content) throws Exception {
        Path p = Files.createTempFile("imp-test-", ".conf");
        Files.writeString(p, content);
        p.toFile().deleteOnExit();
        return p;
    }

    public static void run(TestRunner r) throws Exception {
        Logger log = new Logger();

        // básico
        r.test("config.basic");
        Path p = tmp("version=1\nfallback=xdg-open\ndetect_mime=true\n");
        ImpConfig cfg = ConfigLoader.load(p.toString(), log);
        r.eq(1, cfg.version(), "version");
        r.eq("xdg-open", cfg.fallback(), "fallback");
        r.eq(true, cfg.detectMime(), "detectMime");

        // regra ext normalizada (com e sem ponto)
        r.test("config.ext-normalize");
        p = tmp("version=1\next.pdf=zathura\next.PNG=imv\next..gif=imv\n");
        cfg = ConfigLoader.load(p.toString(), log);
        r.eq("zathura", cfg.rules().get("ext.pdf"), "ext.pdf");
        r.eq("imv", cfg.rules().get("ext.png"), "ext.png lowercase");
        r.eq("imv", cfg.rules().get("ext.gif"), "ext..gif normaliza para ext.gif");

        // mime
        r.test("config.mime");
        p = tmp("version=1\nmime.application/pdf=zathura\n");
        cfg = ConfigLoader.load(p.toString(), log);
        r.eq("zathura", cfg.rules().get("mime.application/pdf"), "mime.application/pdf");

        // version errada → ConfigException
        r.test("config.version-wrong");
        Path pBad = tmp("version=2\n");
        try {
            ConfigLoader.load(pBad.toString(), log);
            r.ok(false, "should have thrown ConfigException");
        } catch (ConfigException e) {
            r.ok(true, "threw ConfigException");
        }

        // version faltando → ConfigException
        r.test("config.version-missing");
        Path pNoVer = tmp("fallback=xdg-open\n");
        try {
            ConfigLoader.load(pNoVer.toString(), log);
            r.ok(false, "should have thrown");
        } catch (ConfigException e) {
            r.ok(true, "threw");
        }

        // value com '=' → ConfigException
        r.test("config.value-eq");
        Path pEq = tmp("version=1\next.pdf=echo a=b\n");
        try {
            ConfigLoader.load(pEq.toString(), log);
            r.ok(false, "should have thrown");
        } catch (ConfigException e) {
            r.ok(true, "threw");
        }

        // arquivo inexistente → ConfigException
        r.test("config.missing-file");
        try {
            ConfigLoader.load("/proc/naoexiste/naoexiste.conf", log);
            r.ok(false, "should have thrown");
        } catch (ConfigException e) {
            r.ok(true, "threw");
        }

        // builtin: sem path, sem user, sem system → usa builtin
        r.test("config.builtin");
        String origHome = System.getProperty("user.home");
        Path emptyHome = Files.createTempDirectory("imp-empty-home-");
        System.setProperty("user.home", emptyHome.toString());
        try {
            cfg = ConfigLoader.load(null, new Logger());
        } finally {
            System.setProperty("user.home", origHome);
        }
        r.eq(1, cfg.version(), "version builtin");
        r.ok(cfg.rules().size() >= 20, "rules >= 20 (builtin tem 27)");
    }
}
