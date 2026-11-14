package br.redhot.imp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

public final class Detector {

    public record ResolvedRule(String ruleKey, String handler) {}

    private Detector() {}

    public static String extension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot < name.length() - 1) {
            return name.substring(dot + 1).toLowerCase();
        }
        return name.toLowerCase();
    }

    public static Optional<String> mime(Path file, String fileCmd, Logger log) {
        if (!isExecutableOnPath(fileCmd)) return Optional.empty();
        try {
            ProcessBuilder pb = new ProcessBuilder(fileCmd, "--brief", "--mime-type", file.toString());
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            String out;
            try (InputStream in = p.getInputStream()) {
                out = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
            int rc = p.waitFor();
            if (rc != 0 || out.isEmpty()) {
                log.warn("detect", "file command failed for " + file);
                return Optional.empty();
            }
            int nl = out.indexOf('\n');
            if (nl >= 0) out = out.substring(0, nl).trim();
            return out.isEmpty() ? Optional.empty() : Optional.of(out);
        } catch (IOException | InterruptedException e) {
            log.warn("detect", "file command error: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static ResolvedRule resolveRule(ImpConfig cfg, Path file, MimeMap mimeMap, Logger log) {
        String ext = extension(file);
        String mimeFromFile = null;
        boolean fileDetectionOk = false;

        if (cfg.detectMime()) {
            Optional<String> m = mime(file, cfg.detectFileCmd(), log);
            if (m.isPresent()) {
                mimeFromFile = m.get();
                fileDetectionOk = true;
            }
        }

        if (fileDetectionOk) {
            String key = "mime." + mimeFromFile.toLowerCase();
            String handler = cfg.rules().get(key);
            log.debug("detect", "mime=" + mimeFromFile + " key=" + key + " -> "
                    + (handler == null ? "(no rule)" : handler));
            if (handler != null) {
                return new ResolvedRule(key, handler);
            }
        } else {
            String mimeFromTypes = mimeMap.mimeFor(ext);
            if (mimeFromTypes != null) {
                String key = "mime." + mimeFromTypes.toLowerCase();
                String handler = cfg.rules().get(key);
                log.debug("detect", "ext=" + ext + " via mime.types=" + mimeFromTypes
                        + " key=" + key + " -> " + (handler == null ? "(no rule)" : handler));
                if (handler != null) {
                    return new ResolvedRule(key, handler);
                }
            }
        }

        String key = "ext." + ext;
        String handler = cfg.rules().get(key);
        log.debug("detect", "ext=" + ext + " key=" + key + " -> " + (handler == null ? "(no rule)" : handler));
        if (handler != null) {
            return new ResolvedRule(key, handler);
        }
        log.debug("detect", "no rule matched for " + file);
        return null;
    }

    private static boolean isExecutableOnPath(String command) {
        if (command == null || command.isEmpty()) return false;
        if (command.contains("/")) return java.nio.file.Files.isExecutable(Path.of(command));
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return false;
        for (String dir : pathEnv.split(":")) {
            if (dir.isEmpty()) continue;
            if (java.nio.file.Files.isExecutable(Path.of(dir, command))) return true;
        }
        return false;
    }
}
