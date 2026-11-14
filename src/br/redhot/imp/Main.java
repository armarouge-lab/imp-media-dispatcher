package br.redhot.imp;

public class Main {

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--version")) {
            System.out.println("imp 0.1.0a");
            return;
        }
        if (args.length == 0 || (args.length == 1 && args[0].equals("--help"))) {
            printHelp();
            return;
        }
        System.err.println("imp: not implemented yet");
        System.exit(ExitCode.EX_USAGE.code());
    }

    private static void printHelp() {
        System.out.println("Usage: imp <file>");
        System.out.println("       imp --version");
        System.out.println("       imp --help");
    }
}
