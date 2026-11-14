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

public final class MimeMap {

    private static final String USER_PATH = ".config/redhot/imp/mime.types";
    private static final String SYSTEM_DEBIAN = "/etc/redhot/imp/mime.types";
    private static final String SYSTEM_FREEBSD = "/usr/local/etc/redhot/imp/mime.types";
    private static final String BUILTIN = "/mime.types.default";

    private final Map<String, String> byMime;
    private final Map<String, String> byExt;

    private MimeMap(Map<String, String> byMime, Map<String, String> byExt) {
        this.byMime = byMime;
        this.byExt = byExt;
    }

    public String mimeFor(String extension) {
        String ext = extension.startsWith(".") ? extension : "." + extension;
        return byExt.get(ext.toLowerCase());
    }

    public String extFor(String mime) {
        return byMime.get(mime);
    }

    public int size() {
        return byExt.size();
    }

    public static MimeMap load(String explicitPath, Logger log) throws ConfigException {
        if (explicitPath != null) {
            Path path = Path.of(explicitPath);
            if (!Files.isReadable(path)) {
                throw new ConfigException("mime.types not readable: " + path);
            }
            try (InputStream in = Files.newInputStream(path)) {
                return parse(in, path.toString());
            } catch (IOException e) {
                throw new ConfigException("cannot read mime.types: " + e.getMessage(), e);
            }
        }

        Path resolved = resolveDefault(log);
        if (resolved != null) {
            try (InputStream in = Files.newInputStream(resolved)) {
                return parse(in, resolved.toString());
            } catch (IOException e) {
                throw new ConfigException("cannot read mime.types: " + e.getMessage(), e);
            }
        }

        InputStream in = MimeMap.class.getResourceAsStream(BUILTIN);
        if (in == null) {
            throw new ConfigException("built-in mime.types not found in jar");
        }
        try (in) {
            return parse(in, "<builtin>");
        } catch (IOException e) {
            throw new ConfigException("cannot read builtin mime.types: " + e.getMessage(), e);
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
        log.warn("mime", "no mime.types in user or system path, using built-in default");
        return null;
    }

    private static MimeMap parse(InputStream in, String source) throws IOException, ConfigException {
        Map<String, String> byMime = new LinkedHashMap<>();
        Map<String, String> byExt = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }
                String mime = parts[0];
                if (!mime.contains("/")) {
                    throw new ConfigException(source + ":" + lineNo + ": invalid mime: " + mime);
                }
                for (int i = 1; i < parts.length; i++) {
                    String ext = "." + parts[i].toLowerCase();
                    byExt.put(ext, mime);
                    byMime.putIfAbsent(mime, parts[i]);
                }
            }
        }

        return new MimeMap(
                Collections.unmodifiableMap(byMime),
                Collections.unmodifiableMap(byExt));
    }
}
