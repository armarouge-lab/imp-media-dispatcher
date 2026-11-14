package br.redhot.imp;

public class MimeMapTest {

    public static void run(TestRunner r) throws Exception {
        Logger log = new Logger();

        r.test("mime.load-builtin");
        MimeMap map = MimeMap.load(log);
        r.ok(map.size() > 0, "mime.types carregado");

        r.test("mime.lookup");
        r.eq("application/pdf", map.mimeFor(".pdf"), "pdf → application/pdf");
        r.eq("application/pdf", map.mimeFor("pdf"), "pdf sem ponto");
        r.eq("image/png", map.mimeFor(".png"), "png → image/png");
        r.eq("text/markdown", map.mimeFor(".md"), "md → text/markdown");
        r.eq(null, map.mimeFor(".naoexiste"), "inexistente → null");
    }
}
