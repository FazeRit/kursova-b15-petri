package ua.kpi.b15.infrastructure.output;

import ua.kpi.b15.application.ports.ResultsStore;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CsvResultsStore implements ResultsStore {
    private static final String SEPARATOR = ";";

    private final Path directory;

    public CsvResultsStore(Path directory) {
        this.directory = directory;
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public void save(String fileName, List<String> header, List<List<Object>> rows) {
        String text =
                Stream.concat(Stream.of(List.<Object>copyOf(header)), rows.stream())
                        .map(
                                row ->
                                        row.stream()
                                                .map(String::valueOf)
                                                .collect(Collectors.joining(SEPARATOR)))
                        .collect(Collectors.joining("\n", "", "\n"));
        try {
            Files.writeString(directory.resolve(fileName), text, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
