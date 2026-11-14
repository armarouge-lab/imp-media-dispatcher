package br.redhot.imp;

public final class Cli {

    public record ParsedArgs(
            Command command,
            boolean dryRun,
            boolean debug,
            boolean json,
            String configPath,
            String filePath
    ) {
        public enum Command {
            DISPATCH, VERSION, HELP, LIST_MAPPINGS, LIST_HANDLERS, SELFTEST
        }
    }

    private Cli() {}

    public static ParsedArgs parse(String[] args) throws UsageException {
        boolean dryRun = false;
        boolean debug = false;
        boolean json = false;
        String configPath = null;
        String filePath = null;
        ParsedArgs.Command command = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--dry-run" -> dryRun = true;
                case "--debug" -> debug = true;
                case "--json" -> json = true;
                case "--config" -> {
                    if (++i >= args.length) {
                        throw new UsageException("--config requires a path");
                    }
                    configPath = args[i];
                }
                case "--version" -> command = setCommand(command, ParsedArgs.Command.VERSION);
                case "--help" -> command = setCommand(command, ParsedArgs.Command.HELP);
                case "--list-mappings" -> command = setCommand(command, ParsedArgs.Command.LIST_MAPPINGS);
                case "--list-handlers" -> command = setCommand(command, ParsedArgs.Command.LIST_HANDLERS);
                case "--selftest" -> command = setCommand(command, ParsedArgs.Command.SELFTEST);
                case "--" -> {
                    for (int j = i + 1; j < args.length; j++) {
                        if (filePath != null) {
                            throw new UsageException("only one file expected");
                        }
                        filePath = args[j];
                    }
                    i = args.length;
                }
                default -> {
                    if (arg.startsWith("-")) {
                        throw new UsageException("unknown option: " + arg);
                    }
                    if (filePath != null) {
                        throw new UsageException("only one file expected, got: " + filePath + " and " + arg);
                    }
                    filePath = arg;
                }
            }
        }

        if (command == null) {
            command = ParsedArgs.Command.DISPATCH;
        }
        if (command == ParsedArgs.Command.DISPATCH && filePath == null) {
            throw new UsageException("missing file argument");
        }
        if (command != ParsedArgs.Command.DISPATCH && filePath != null) {
            throw new UsageException("unexpected file argument: " + filePath);
        }
        return new ParsedArgs(command, dryRun, debug, json, configPath, filePath);
    }

    private static ParsedArgs.Command setCommand(ParsedArgs.Command current, ParsedArgs.Command next)
            throws UsageException {
        if (current != null) {
            throw new UsageException("only one command allowed, got: " + current + " and " + next);
        }
        return next;
    }
}
