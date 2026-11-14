package br.redhot.imp;

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

        if (parsed.debug()) {
            log.setThreshold(Logger.Level.DEBUG);
        }

        switch (parsed.command()) {
            case VERSION -> System.out.println("imp 0.1.0b");
            case HELP -> printHelp();
            case LIST_MAPPINGS -> System.exit(runListMappings(parsed, log));
            case LIST_HANDLERS -> {
                System.err.println("imp: --list-handlers not implemented yet (V0.3.0)");
                System.exit(ExitCode.EX_USAGE.code());
            }
            case SELFTEST -> {
                System.err.println("imp: --selftest not implemented yet (V0.3.0)");
                System.exit(ExitCode.EX_USAGE.code());
            }
            case DISPATCH -> {
                System.err.println("imp: dispatch not implemented yet (V0.2.0b)");
                System.exit(ExitCode.EX_USAGE.code());
            }
        }
    }

    private static int runListMappings(Cli.ParsedArgs parsed, Logger log) {
        ImpConfig cfg;
        try {
            cfg = ConfigLoader.load(parsed.configPath(), log);
        } catch (ConfigException e) {
            System.err.println("imp: config error: " + e.getMessage());
            return ExitCode.EX_CONFIG_ERROR.code();
        }
        for (var entry : cfg.rules().entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        return ExitCode.EX_OK.code();
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
