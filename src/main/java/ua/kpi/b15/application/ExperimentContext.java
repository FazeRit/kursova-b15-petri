package ua.kpi.b15.application;

import ua.kpi.b15.application.ports.ResultsOutput;
import ua.kpi.b15.application.ports.ResultsStore;
import ua.kpi.b15.domain.ports.RandomSourceFactory;

public record ExperimentContext(
        RandomSourceFactory createRandom,
        Replications replications,
        ResultsOutput output,
        ResultsStore store) {}
