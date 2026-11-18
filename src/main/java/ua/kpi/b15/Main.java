package ua.kpi.b15;

import ua.kpi.b15.application.Experiment;
import ua.kpi.b15.application.ExperimentContext;
import ua.kpi.b15.application.Replications;
import ua.kpi.b15.application.experiments.CheckRandomGenerators;
import ua.kpi.b15.application.experiments.CompareImprovements;
import ua.kpi.b15.application.experiments.EstimateBaseModel;
import ua.kpi.b15.application.experiments.EstimateRunCount;
import ua.kpi.b15.application.experiments.FindTransientPeriod;
import ua.kpi.b15.application.experiments.StudyFlowLimit;
import ua.kpi.b15.application.experiments.StudyUnlimitedNode;
import ua.kpi.b15.application.experiments.TraceEvents;
import ua.kpi.b15.application.experiments.VerifyModel;
import ua.kpi.b15.domain.ports.RandomSourceFactory;
import ua.kpi.b15.infrastructure.output.ConsoleResultsOutput;
import ua.kpi.b15.infrastructure.output.CsvResultsStore;
import ua.kpi.b15.infrastructure.random.Mulberry32;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Main {
    private static final Map<String, Experiment> EXPERIMENTS = new LinkedHashMap<>();

    static {
        EXPERIMENTS.put("protocol", new TraceEvents());
        EXPERIMENTS.put("verify", new VerifyModel());
        EXPERIMENTS.put("rng", new CheckRandomGenerators());
        EXPERIMENTS.put("transient", new FindTransientPeriod());
        EXPERIMENTS.put("runs", new EstimateRunCount());
        EXPERIMENTS.put("base", new EstimateBaseModel());
        EXPERIMENTS.put("limit", new StudyFlowLimit());
        EXPERIMENTS.put("unlimited", new StudyUnlimitedNode());
        EXPERIMENTS.put("proposals", new CompareImprovements());
    }

    private Main() {}

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "base";
        String directory = args.length > 1 ? args[1] : "results";

        RandomSourceFactory createRandom = Mulberry32::new;
        ExperimentContext context =
                new ExperimentContext(
                        createRandom,
                        new Replications(createRandom),
                        new ConsoleResultsOutput(),
                        new CsvResultsStore(Path.of(directory)));

        List<Experiment> selected =
                mode.equals("all")
                        ? List.copyOf(EXPERIMENTS.values())
                        : EXPERIMENTS.containsKey(mode)
                                ? List.of(EXPERIMENTS.get(mode))
                                : List.of();

        if (selected.isEmpty()) {
            System.out.println("Режими: " + String.join(" | ", EXPERIMENTS.keySet()) + " | all");
            System.exit(1);
        }
        selected.forEach(experiment -> experiment.run(context));
    }
}
