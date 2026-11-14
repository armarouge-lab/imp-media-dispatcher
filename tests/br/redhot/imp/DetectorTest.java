package br.redhot.imp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DetectorTest {

    private static ImpConfig cfg(Map<String, String> rules, boolean detectMime, String fallback) {
        return new ImpConfig(1, fallback, detectMime, "file", "warn", "",
                Collections.unmodifiableMap(new LinkedHashMap<>(rules)));
    }

    public static void run(TestRunner r) throws Exception {
        Logger log = new Logger();

        r.test("detector.extension");
        r.eq("pdf", Detector.extension(Path.of("/tmp/x.pdf")), "x.pdf");
        r.eq("md",  Detector.extension(Path.of("/tmp/doc.md")), "doc.md");
        r.eq("gz",  Detector.extension(Path.of("/tmp/a.tar.gz")), "a.tar.gz → gz");
        r.eq("readme", Detector.extension(Path.of("/tmp/README")), "README → readme");
        r.eq("makefile", Detector.extension(Path.of("/tmp/Makefile")), "Makefile → makefile");
        r.eq("pdf", Detector.extension(Path.of("/tmp/x.PDF")), "x.PDF → pdf (lowercase)");

        r.test("detector.mime-with-file");
        Path pdf = Files.createTempFile("det-", ".pdf");
        Files.writeString(pdf, "%PDF-1.4\ntrailer\n");
        pdf.toFile().deleteOnExit();
        Optional<String> m = Detector.mime(pdf, "file", log);
        r.eq(true, m.isPresent(), "file returned mime");
        r.eq("application/pdf", m.orElse(""), "application/pdf");

        r.test("detector.mime-no-file");
        m = Detector.mime(pdf, "nonexistent_file_cmd_xyz", log);
        r.eq(false, m.isPresent(), "no file cmd → empty");

        r.test("detector.resolve-no-rule");
        MimeMap map = MimeMap.load(log);
        var resolved = Detector.resolveRule(cfg(Map.of(), false, "xdg-open"), pdf, map, log);
        r.eq(null, resolved, "no rule → null");

        r.test("detector.resolve-ext");
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("ext.pdf", "zathura");
        resolved = Detector.resolveRule(cfg(rules, false, "xdg-open"), pdf, map, log);
        r.ok(resolved != null, "resolved");
        r.eq("ext.pdf", resolved.ruleKey(), "rule key");
        r.eq("zathura", resolved.handler(), "handler");

        r.test("detector.resolve-mime-over-ext");
        Map<String, String> rules2 = new LinkedHashMap<>();
        rules2.put("ext.bin", "printf");
        rules2.put("mime.text/plain", "echo");
        Path textBin = Files.createTempFile("det-", ".bin");
        Files.writeString(textBin, "hello world\n");
        textBin.toFile().deleteOnExit();
        resolved = Detector.resolveRule(cfg(rules2, true, "xdg-open"), textBin, map, log);
        r.ok(resolved != null, "resolved via mime");
        r.eq("mime.text/plain", resolved.ruleKey(), "mime wins over ext");
        r.eq("echo", resolved.handler(), "echo (not printf)");

        r.test("detector.resolve-mime-types-fallback");
        Map<String, String> rules3 = new LinkedHashMap<>();
        rules3.put("mime.application/pdf", "zathura");
        Path pdf2 = Files.createTempFile("det-", ".pdf");
        Files.writeString(pdf2, "%PDF-1.4\n");
        pdf2.toFile().deleteOnExit();
        resolved = Detector.resolveRule(cfg(rules3, false, "xdg-open"), pdf2, map, log);
        r.ok(resolved != null, "resolved via mime.types");
        r.eq("mime.application/pdf", resolved.ruleKey(), "mime.types provided MIME");
    }
}
