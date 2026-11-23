package ua.kpi.b15;

import ua.kpi.b15.experiments.CheckRandomGenerators;
import ua.kpi.b15.experiments.CompareImprovements;
import ua.kpi.b15.experiments.EstimateBaseModel;
import ua.kpi.b15.experiments.EstimateRunCount;
import ua.kpi.b15.experiments.FindTransientPeriod;
import ua.kpi.b15.experiments.StudyFlowLimit;
import ua.kpi.b15.experiments.StudyUnlimitedNode;
import ua.kpi.b15.experiments.TraceEvents;
import ua.kpi.b15.experiments.VerifyModel;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class Main {
    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "base";
        Output out = new Output(Path.of(args.length > 1 ? args[1] : "results"));

        Map<String, Consumer<Output>> experiments = new LinkedHashMap<>();
        experiments.put("protocol", TraceEvents::run);
        experiments.put("verify", VerifyModel::run);
        experiments.put("rng", CheckRandomGenerators::run);
        experiments.put("transient", FindTransientPeriod::run);
        experiments.put("runs", EstimateRunCount::run);
        experiments.put("base", EstimateBaseModel::run);
        experiments.put("limit", StudyFlowLimit::run);
        experiments.put("unlimited", StudyUnlimitedNode::run);
        experiments.put("proposals", CompareImprovements::run);

        if (mode.equals("all")) {
            experiments.values().forEach(experiment -> experiment.accept(out));
        } else if (experiments.containsKey(mode)) {
            experiments.get(mode).accept(out);
        } else {
            System.out.println("Режими: " + String.join(" | ", experiments.keySet()) + " | all");
            System.exit(1);
        }
    }
}
