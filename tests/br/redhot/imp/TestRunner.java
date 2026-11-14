package br.redhot.imp;

import java.util.Objects;

public class TestRunner {

    @FunctionalInterface
    public interface Thrower { void run() throws Exception; }

    private int passed = 0;
    private int failed = 0;
    private String currentTest = "";

    public void test(String name) { this.currentTest = name; }

    public void eq(Object expected, Object actual, String what) {
        if (Objects.equals(expected, actual)) passed++;
        else {
            failed++;
            System.err.println("FAIL [" + currentTest + "] " + what
                    + ": expected=" + format(expected) + " actual=" + format(actual));
        }
    }

    public void ok(boolean condition, String what) {
        if (condition) passed++;
        else { failed++; System.err.println("FAIL [" + currentTest + "] " + what); }
    }

    public void throwsEx(Class<? extends Throwable> type, Thrower t, String what) {
        try {
            t.run();
            failed++;
            System.err.println("FAIL [" + currentTest + "] " + what + ": no exception thrown");
        } catch (Throwable th) {
            if (type.isInstance(th)) passed++;
            else {
                failed++;
                System.err.println("FAIL [" + currentTest + "] " + what
                        + ": expected " + type.getSimpleName()
                        + " got " + th.getClass().getSimpleName());
            }
        }
    }

    private static String format(Object o) {
        if (o == null) return "null";
        if (o instanceof String s) return "\"" + s + "\"";
        return o.toString();
    }

    public int summary() {
        System.out.println();
        System.out.println("passed: " + passed);
        System.out.println("failed: " + failed);
        return failed == 0 ? 0 : 1;
    }
}
