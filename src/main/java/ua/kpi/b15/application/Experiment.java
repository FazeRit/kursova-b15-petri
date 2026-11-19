package ua.kpi.b15.application;

@FunctionalInterface
public interface Experiment {
    void run(ExperimentContext context);
}
