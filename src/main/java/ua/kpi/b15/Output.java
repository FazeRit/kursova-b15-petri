package ua.kpi.b15;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Output {
    private final PrintStream console = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    private final Path directory;

    public Output(Path directory) {
        this.directory = directory;
    }

    public void section(String title) {
        console.println();
        console.println("===== " + title + " =====");
    }

    public void line(String text) {
        console.println(text);
    }

    public void save(String fileName, List<String> header, List<List<Object>> rows) {
        List<String> lines = new ArrayList<>();
        lines.add(String.join(";", header));
        for (List<Object> row : rows) {
            List<String> cells = new ArrayList<>();
            for (Object cell : row) cells.add(String.valueOf(cell));
            lines.add(String.join(";", cells));
        }
        try {
            Files.createDirectories(directory);
            Files.writeString(
                    directory.resolve(fileName),
                    String.join("\n", lines) + "\n",
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
