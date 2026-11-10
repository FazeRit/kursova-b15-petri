package ua.kpi.b15.infrastructure.output;

import ua.kpi.b15.application.ports.ResultsOutput;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class ConsoleResultsOutput implements ResultsOutput {
    private final PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

    @Override
    public void section(String title) {
        out.println();
        out.println("===== " + title + " =====");
    }

    @Override
    public void line(String text) {
        out.println(text);
    }
}
