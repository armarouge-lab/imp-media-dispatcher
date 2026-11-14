package br.redhot.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigLoader {

    private static final String USER_PATH = ".config/redhot/imp/imp.conf";
    private static final String SYSTEM_DEBIAN = "/etc/redhot/imp/imp.conf";
    private static final String SYSTEM_FREEBSD = "/usr/local/etc/redhot/imp/imp.conf";
    private static final String BUILTIN = "/imp.conf.default";

    private ConfigLoader() {}

    public static ImpConfig load(String explicitPath, Logger log) throws ConfigException {
        if (explicitPath != null) {
            Path path = Path.of(explicitPath);
            if (!Files.isReadable(path)) {
                throw new ConfigException("config not readable: " + path);
            }
            try (InputStream in = Files.newInputStream(path)) {
                return parse(in, path.toString(), log);
            } catch (IOException e) {
                throw new ConfigException("cannot read config: " + e.getMessage(), e);
            }
        }

        Path resolved = resolveDefault(log);
        if (resolved != null) {
            try (InputStream in = Files.newInputStream(resolved)) {
                return parse(in, resolved.toString(), log);
            } catch (IOException e) {
                throw new ConfigException("cannot read config: " + e.getMessage(), e);
            }
        }

        InputStream in = ConfigLoader.class.getResourceAsStream(BUILTIN);
        if (in == null) {
            throw new ConfigException("built-in config not found in jar");
        }
        try (in) {
            return parse(in, "<builtin>", log);
        } catch (IOException e) {
            throw new ConfigException("cannot read builtin config: " + e.getMessage(), e);
        }
    }

    private static Path resolveDefault(Logger log) {
        String home = System.getProperty("user.home");
        if (home != null) {
            Path user = Path.of(home, USER_PATH);
            if (Files.isReadable(user)) {
                return user;
            }
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        Path system = Path.of(os.contains("freebsd") ? SYSTEM_FREEBSD : SYSTEM_DEBIAN);
        if (Files.isReadable(system)) {
            return system;
        }
        log.warn("config", "no imp.conf in user or system path, using built-in default");
        return null;
    }

    private static ImpConfig parse(InputStream in, String source, Logger log)
            throws IOException, ConfigException {
        int version = -1;
        boolean sawVersion = false;
        String fallback = "xdg-open";
        boolean detectMime = true;
        String detectFileCmd = "file";
        String logLevel = "warn";
        String logFile = "";
        Map<String, String> rules = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq < 0) {
                    throw new ConfigException(source + ":" + lineNo + ": missing '='");
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();

                switch (key) {
                    case "version" -> {
                        try {
                            version = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            throw new ConfigException(source + ":" + lineNo + ": version not an integer");
                        }
                        sawVersion = true;
                    }
                    case "fallback" -> {
                        if (!isExecutableOnPath(value)) {
                            log.warn("config", "fallback not on PATH: " + value);
                        }
                        fallback = value;
                    }
                    case "detect_mime" -> {
                        if (!value.equals("true") && !value.equals("false")) {
                            throw new ConfigException(
                                    source + ":" + lineNo + ": detect_mime must be true or false");
                        }
                        detectMime = Boolean.parseBoolean(value);
                    }
                    case "detect_file_cmd" -> {
                        if (!isExecutableOnPath(value)) {
                            log.warn("config", "detect_file_cmd not on PATH: " + value);
                        }
                        detectFileCmd = value;
                    }
                    case "log_level" -> {
                        switch (value) {
                            case "error", "warn", "info", "debug" -> logLevel = value;
                            default -> throw new ConfigException(
                                    source + ":" + lineNo + ": invalid log_level: " + value);
                        }
                    }
                    case "log_file" -> logFile = value;
                    default -> {
                        if (key.startsWith("ext.")) {
                            String ext = key.substring(4).toLowerCase();
                            if (!ext.startsWith(".")) {
                                ext = "." + ext;
                            }
                            rules.put("ext" + ext, value);
                        } else if (key.startsWith("mime.")) {
                            String mime = key.substring(5);
                            if (!mime.contains("/")) {
                                throw new ConfigException(
                                        source + ":" + lineNo + ": mime key must be type/subtype: " + mime);
                            }
                            rules.put("mime." + mime.toLowerCase(), value);
                        } else {
                            log.warn("config", source + ":" + lineNo + ": unknown key: " + key);
                        }
                    }
                }
            }
        }

        if (!sawVersion) {
            throw new ConfigException(source + ": missing 'version'");
        }
        if (version != 1) {
            throw new ConfigException(source + ": unsupported version: " + version);
        }

        return new ImpConfig(
                version,
                fallback,
                detectMime,
                detectFileCmd,
                logLevel,
                logFile,
                Collections.unmodifiableMap(new LinkedHashMap<>(rules)));
    }

    private static boolean isExecutableOnPath(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        if (command.contains("/")) {
            return Files.isExecutable(Path.of(command));
        }
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return false;
        }
        for (String dir : pathEnv.split(":")) {
            if (dir.isEmpty()) {
                continue;
            }
            if (Files.isExecutable(Path.of(dir, command))) {
                return true;
            }
        }
        return false;
    }
}
