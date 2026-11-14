package br.redhot.imp;

public enum ExitCode {
    EX_OK(0),
    EX_NO_HANDLER(1),
    EX_EXEC_FAILED(2),
    EX_PRECOND(3),
    EX_CONFIG_ERROR(4),
    EX_CANCELED(5),
    EX_USAGE(64);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
