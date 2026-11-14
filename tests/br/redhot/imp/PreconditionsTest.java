package br.redhot.imp;

import java.nio.file.Files;
import java.nio.file.Path;

public class PreconditionsTest {

    public static void run(TestRunner r) throws Exception {
        r.test("precond.file-ok");
        Path file = Files.createTempFile("prec-", ".txt");
        Files.writeString(file, "content");
        file.toFile().deleteOnExit();
        Path result = Preconditions.validateFile(file.toString());
        r.ok(Files.exists(result), "returned path exists");
        r.eq(file.toRealPath(), result, "returns toRealPath for regular file");

        r.test("precond.missing");
        try {
            Preconditions.validateFile("/tmp/naoexiste_" + System.nanoTime());
            r.ok(false, "should throw");
        } catch (PreconditionException e) {
            r.ok(true, "threw");
            r.ok(e.getMessage().contains("not found"), "message contains 'not found'");
        }

        r.test("precond.directory");
        try {
            Preconditions.validateFile("/tmp");
            r.ok(false, "should throw");
        } catch (PreconditionException e) {
            r.ok(true, "threw");
            r.ok(e.getMessage().contains("dgn"), "message mentions 'dgn'");
        }

        r.test("precond.symlink");
        Path target = Files.createTempFile("prec-target-", ".txt");
        Files.writeString(target, "content");
        target.toFile().deleteOnExit();
        Path link = Files.createTempFile("prec-link-", ".lnk");
        Files.delete(link);
        Files.createSymbolicLink(link, target);
        link.toFile().deleteOnExit();
        Path resultLink = Preconditions.validateFile(link.toString());
        r.eq(link, resultLink, "symlink returned as-is (not resolved)");
    }
}
