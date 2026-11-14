package br.redhot.imp;

public class CliTest {

    public static void run(TestRunner r) throws Exception {
        r.test("cli.version");
        var a = Cli.parse(new String[]{"--version"});
        r.eq(Cli.ParsedArgs.Command.VERSION, a.command(), "command");
        r.eq(false, a.dryRun(), "dryRun");

        r.test("cli.help");
        a = Cli.parse(new String[]{"--help"});
        r.eq(Cli.ParsedArgs.Command.HELP, a.command(), "command");

        r.test("cli.list-mappings");
        a = Cli.parse(new String[]{"--list-mappings"});
        r.eq(Cli.ParsedArgs.Command.LIST_MAPPINGS, a.command(), "command");

        r.test("cli.list-handlers-json");
        a = Cli.parse(new String[]{"--list-handlers", "--json"});
        r.eq(Cli.ParsedArgs.Command.LIST_HANDLERS, a.command(), "command");
        r.eq(true, a.json(), "json");

        r.test("cli.selftest");
        a = Cli.parse(new String[]{"--selftest"});
        r.eq(Cli.ParsedArgs.Command.SELFTEST, a.command(), "command");

        r.test("cli.dispatch");
        a = Cli.parse(new String[]{"/tmp/x.pdf"});
        r.eq(Cli.ParsedArgs.Command.DISPATCH, a.command(), "command");
        r.eq("/tmp/x.pdf", a.filePath(), "filePath");

        r.test("cli.modifiers");
        a = Cli.parse(new String[]{"--dry-run", "--debug", "--config", "/etc/foo", "/tmp/y.pdf"});
        r.eq(true, a.dryRun(), "dryRun");
        r.eq(true, a.debug(), "debug");
        r.eq("/etc/foo", a.configPath(), "configPath");
        r.eq("/tmp/y.pdf", a.filePath(), "filePath");

        r.test("cli.empty");
        r.throwsEx(UsageException.class, () -> Cli.parse(new String[]{}), "empty args");

        r.test("cli.two-files");
        r.throwsEx(UsageException.class, () -> Cli.parse(new String[]{"a", "b"}), "two positional");

        r.test("cli.config-no-arg");
        r.throwsEx(UsageException.class, () -> Cli.parse(new String[]{"--config"}), "--config no arg");

        r.test("cli.unknown-flag");
        r.throwsEx(UsageException.class, () -> Cli.parse(new String[]{"--frobnicate"}), "unknown flag");
    }
}
