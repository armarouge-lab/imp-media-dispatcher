package br.redhot.imp;

import java.util.Map;

public record ImpConfig(
        int version,
        String fallback,
        boolean detectMime,
        String detectFileCmd,
        String logLevel,
        String logFile,
        Map<String, String> rules
) {}
