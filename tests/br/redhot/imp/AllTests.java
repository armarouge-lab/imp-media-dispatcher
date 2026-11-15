package br.redhot.imp;

public class AllTests {
    public static void main(String[] args) throws Exception {
        TestRunner r = new TestRunner();

        CliTest.run(r);
        ConfigLoaderTest.run(r);
        LoggerTest.run(r);
        MimeMapTest.run(r);
        HandlerTest.run(r);
        PreconditionsTest.run(r);
        DetectorTest.run(r);
        DispatcherTest.run(r);
        EditorVarTest.run(r);

        int code = r.summary();
        System.exit(code);
    }
}
