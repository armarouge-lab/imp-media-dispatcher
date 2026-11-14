package br.redhot.imp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Logger log = new Logger();
        Cli.ParsedArgs parsed;
        try {
            parsed = Cli.parse(args);
        } catch (UsageException e) {
            System.err.println("imp: " + e.getMessage());
            printHelp();
            System.exit(ExitCode.EX_USAGE.code());
            return;
        }

        boolean debugOverride = parsed.debug();
        if (debugOverride) log.setThreshold(Logger.Level.DEBUG);

        switch (parsed.command()) {
            case VERSION -> { System.out.println("imp 0.9.0"); return; }
            case HELP -> { printHelp(); return; }
            case LIST_MAPPINGS, LIST_HANDLERS, SELFTEST, DISPATCH -> { }
        }

        ImpConfig cfg;
        try {
            cfg = ConfigLoader.load(parsed.configPath(), log);
        } catch (ConfigException e) {
            System.err.println("imp: config error: " + e.getMessage());
            System.exit(ExitCode.EX_CONFIG_ERROR.code());
            return;
        }

        if (!debugOverride) {
            switch (cfg.logLevel()) {
                case "error" -> log.setThreshold(Logger.Level.ERROR);
                case "warn"  -> log.setThreshold(Logger.Level.WARN);
                case "info"  -> log.setThreshold(Logger.Level.INFO);
                case "debug" -> log.setThreshold(Logger.Level.DEBUG);
            }
        }

        if (!cfg.logFile().isEmpty()) {
            try {
                log.setLogFile(Path.of(cfg.logFile()));
            } catch (IOException e) {
                System.err.println("imp: cannot open log_file " + cfg.logFile() + ": " + e.getMessage());
            }
        }

        MimeMap mimeMap;
        try {
            mimeMap = MimeMap.load(log);
        } catch (ConfigException e) {
            System.err.println("imp: mime.types error: " + e.getMessage());
            System.exit(ExitCode.EX_CONFIG_ERROR.code());
            return;
        }

        switch (parsed.command()) {
            case LIST_MAPPINGS -> System.exit(runListMappings(cfg, parsed.json()));
            case LIST_HANDLERS -> System.exit(runListHandlers(cfg, parsed.json()));
            case SELFTEST -> System.exit(runSelftest(cfg, parsed.json()));
            case DISPATCH -> System.exit(runDispatch(cfg, mimeMap, parsed, log));
            case VERSION, HELP -> { }
        }
    }

    private static int runListMappings(ImpConfig cfg, boolean json) {
        if (json) {
            System.out.println("[");
            boolean first = true;
            for (var entry : cfg.rules().entrySet()) {
                if (!first) System.out.println(",");
                first = false;
                String[] parts = splitRuleKey(entry.getKey());
                System.out.print("  {\"type\":" + Json.escape(parts[0])
                        + ",\"pattern\":" + Json.escape(parts[1])
                        + ",\"command\":" + Json.escape(entry.getValue()) + "}");
            }
            System.out.println();
            System.out.println("]");
        } else {
            for (var entry : cfg.rules().entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
        }
        return ExitCode.EX_OK.code();
    }

    private static String[] splitRuleKey(String key) {
        int dot = key.indexOf('.');
        if (dot < 0) return new String[] {key, ""};
        return new String[] {key.substring(0, dot), key.substring(dot)};
    }

    private static int runListHandlers(ImpConfig cfg, boolean json) {
        Map<String, String> roles = new LinkedHashMap<>();
        for (var entry : cfg.rules().entrySet()) {
            roles.putIfAbsent(entry.getValue(), "rule");
        }
        if (cfg.fallback() != null && !cfg.fallback().isEmpty()) {
            roles.put(cfg.fallback(), "fallback");
        }
        if (json) {
            System.out.println("[");
            boolean first = true;
            for (var entry : roles.entrySet()) {
                if (!first) System.out.println(",");
                first = false;
                System.out.print("  {\"command\":" + Json.escape(entry.getKey())
                        + ",\"available\":" + Json.bool(Handler.available(entry.getKey()))
                        + ",\"role\":" + Json.escape(entry.getValue()) + "}");
            }
            System.out.println();
            System.out.println("]");
        } else {
            for (var entry : roles.entrySet()) {
                String status = Handler.available(entry.getKey()) ? "OK" : "MISSING";
                String suffix = "fallback".equals(entry.getValue()) ? "  (fallback)" : "";
                System.out.printf("%-25s %-8s%s%n", entry.getKey(), status, suffix);
            }
        }
        return ExitCode.EX_OK.code();
    }

    private static int runSelftest(ImpConfig cfg, boolean json) {
        int feature = Runtime.version().feature();
        boolean javaOk = feature >= 17;
        String javaVer = Runtime.version().toString();
        boolean xdgOk = Handler.available("xdg-open");
        boolean fileOk = Handler.available("file");
        boolean requiredOk = javaOk;
        if (json) {
            System.out.println("{");
            System.out.println("  \"java\":{\"version\":" + Json.escape(javaVer)
                    + ",\"ok\":" + Json.bool(javaOk) + "},");
            System.out.println("  \"xdg_open\":{\"ok\":" + Json.bool(xdgOk) + "},");
            System.out.println("  \"file\":{\"ok\":" + Json.bool(fileOk) + "},");
            System.out.println("  \"imp_conf\":{\"ok\":true},");
            System.out.println("  \"mime_types\":{\"ok\":true}");
            System.out.println("}");
        } else {
            System.out.println("java:       " + (javaOk ? "OK (" + javaVer + ")" : "FAIL (need 17+)"));
            System.out.println("xdg-open:   " + (xdgOk ? "OK" : "MISSING"));
            System.out.println("file:       " + (fileOk ? "OK" : "MISSING"));
            System.out.println("imp.conf:   OK");
            System.out.println("mime.types: OK");
        }
        return requiredOk ? ExitCode.EX_OK.code() : ExitCode.EX_PRECOND.code();
    }

    private static int runDispatch(ImpConfig cfg, MimeMap mimeMap, Cli.ParsedArgs parsed, Logger log) {
        Path file;
        try {
            file = Preconditions.validateFile(parsed.filePath());
        } catch (PreconditionException e) {
            System.err.println("imp: " + e.getMessage());
            return ExitCode.EX_PRECOND.code();
        }
        Detector.ResolvedRule rule = Detector.resolveRule(cfg, file, mimeMap, log);
        return Dispatcher.dispatch(cfg, file, rule, parsed.dryRun(), log);
    }

    private static void printHelp() {
        System.out.println("Usage: imp <file>");
        System.out.println("       imp --dry-run <file>");
        System.out.println("       imp --list-mappings");
        System.out.println("       imp --list-handlers");
        System.out.println("       imp --selftest");
        System.out.println("       imp --version");
        System.out.println("       imp --help");
        System.out.println();
        System.out.println("Modifiers:");
        System.out.println("       --config <path>   use alternate imp.conf");
        System.out.println("       --debug           verbose logging to stderr");
        System.out.println("       --json            output in JSON (--list-*/--selftest)");
    }
}
