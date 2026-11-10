package ua.kpi.b15.application.ports;

import java.util.List;

public interface ResultsStore {
    void save(String fileName, List<String> header, List<List<Object>> rows);
}
