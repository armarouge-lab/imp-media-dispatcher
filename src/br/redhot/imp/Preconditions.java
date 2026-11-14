package br.redhot.imp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Preconditions {

    private Preconditions() {}

    public static Path validateFile(String rawPath) throws PreconditionException {
        Path path = Path.of(rawPath);
        if (!Files.exists(path)) {
            throw new PreconditionException("file not found: " + rawPath);
        }
        if (Files.isDirectory(path)) {
            throw new PreconditionException("directories are handled by nid, not imp");
        }
        if (!Files.isReadable(path)) {
            throw new PreconditionException("file not readable: " + rawPath);
        }
        if (Files.isSymbolicLink(path)) {
            return path;
        }
        try {
            return path.toRealPath();
        } catch (IOException e) {
            return path;
        }
    }
}
