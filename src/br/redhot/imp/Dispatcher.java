package br.redhot.imp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Dispatcher {

    private Dispatcher() {}

    public static int dispatch(ImpConfig cfg,
                               Path file,
                               Detector.ResolvedRule rule,
                               boolean dryRun,
                               Logger log) {
        List<Handler> chain = new ArrayList<>();
        if (rule != null) {
            chain.add(new Handler(rule.handler(), file));
        }
        String fallback = cfg.fallback();
        if (fallback != null && !fallback.isEmpty()) {
            boolean duplicate = rule != null && fallback.equals(rule.handler());
            if (!duplicate) {
                chain.add(new Handler(fallback, file));
            }
        }
        if (chain.isEmpty()) {
            log.warn("dispatch", "no rule matched and no fallback configured");
            return ExitCode.EX_NO_HANDLER.code();
        }

        for (Handler h : chain) {
            if (!h.isAvailable()) {
                log.warn("dispatch", "handler not on PATH: " + h.command());
                continue;
            }
            if (dryRun) {
                System.out.println("would dispatch: " + h.command() + " " + file);
                return ExitCode.EX_OK.code();
            }
            ProcessBuilder pb = new ProcessBuilder(h.toArgv()).inheritIO();
            try {
                Process p = pb.start();
                int rc = p.waitFor();
                if (rc == 0) {
                    log.info("dispatch",
                            "file=" + file + " handler=" + h.command() + " rc=0");
                    return ExitCode.EX_OK.code();
                }
                log.warn("dispatch",
                        "handler " + h.command() + " exited " + rc + ", trying next");
            } catch (IOException e) {
                log.warn("dispatch",
                        "cannot start " + h.command() + ": " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("dispatch", "interrupted while running " + h.command());
                return ExitCode.EX_CANCELED.code();
            }
        }
        log.error("dispatch", "all handlers failed for " + file);
        return ExitCode.EX_EXEC_FAILED.code();
    }
}