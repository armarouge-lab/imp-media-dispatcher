package br.redhot.imp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record Handler(String command, Path file) {

    public boolean isAvailable() {
        if (command == null || command.isEmpty()) return false;
        if (command.contains("/")) {
            return Files.isExecutable(Path.of(command));
        }
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return false;
        for (String dir : pathEnv.split(":")) {
            if (dir.isEmpty()) continue;
            if (Files.isExecutable(Path.of(dir, command))) return true;
        }
        return false;
    }

    public List<String> toArgv() {
        return List.of(command, file.toString());
    }
}
