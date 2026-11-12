package ua.kpi.b15.application.experiments;

import static ua.kpi.b15.application.Format.fixed;
import static ua.kpi.b15.application.experiments.Summaries.averageOf;

import ua.kpi.b15.application.Experiment;
import ua.kpi.b15.application.ExperimentContext;
import ua.kpi.b15.application.ExperimentPlan;
import ua.kpi.b15.application.Replications.Options;
import ua.kpi.b15.domain.statistics.Descriptive;
import ua.kpi.b15.domain.switching.RunMetrics;
import ua.kpi.b15.domain.switching.SwitchingNodeConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class VerifyModel implements Experiment {
    private static final SwitchingNodeConfig BASE = SwitchingNodeConfig.BASE;

    private static final List<Map.Entry<String, SwitchingNodeConfig>> BASE_VARIANTS =
            List.of(
                    Map.entry("Базові значення", BASE),
                    Map.entry("a = unif(12, 7)", BASE.withArrival(12, 7)),
                    Map.entry("a = unif(20, 7)", BASE.withArrival(20, 7)),
                    Map.entry("a = unif(15, 0)", BASE.withArrival(15, 0)),
                    Map.entry("a = unif(1000, 7)", BASE.withArrival(1000, 7)),
                    Map.entry("tp = 3", BASE.withProcessingTime(3)),
                    Map.entry("tp = 9", BASE.withProcessingTime(9)),
                    Map.entry("tl = unif(10, 5)", BASE.withTransmission(10, 5)),
                    Map.entry("tl = unif(20, 5)", BASE.withTransmission(20, 5)),
                    Map.entry("tl = unif(15, 0)", BASE.withTransmission(15, 0)),
                    Map.entry("L = 1", BASE.withFlowLimit(1)),
                    Map.entry("L = 6", BASE.withFlowLimit(6)),
                    Map.entry("b1 = 0,9", BASE.withFirstDirectionShare(0.9)),
                    Map.entry(
                            "a = 15, tl = 15 (детерм.)",
                            BASE.withArrival(15, 0).withTransmission(15, 0)));

    private static final List<String> HEADER =
            List.of(
                    "n",
                    "variant",
                    "Uproc",
                    "U1",
                    "U2",
                    "Pref1",
                    "Pref2",
                    "Pref",
                    "n1",
                    "W1_little",
                    "W_little",
                    "W_sample",
                    "Lin");

    @Override
    public void run(ExperimentContext context) {
        verify(context, "Верифікація базової моделі", "verification.csv", BASE_VARIANTS);
    }

    private void verify(
            ExperimentContext context,
            String title,
            String file,
            List<Map.Entry<String, SwitchingNodeConfig>> variants) {
        context.output().section(title);
        List<List<Object>> rows = new ArrayList<>();
        for (int index = 0; index < variants.size(); index++) {
            Map.Entry<String, SwitchingNodeConfig> variant = variants.get(index);
            List<RunMetrics> runs =
                    context.replications()
                            .run(
                                    variant.getValue(),
                                    ExperimentPlan.RUN_COUNT,
                                    Options.defaults().collectingDeliveryTimes());
            List<Object> row =
                    List.of(
                            index + 1,
                            variant.getKey(),
                            fixed(averageOf(runs, RunMetrics::processorUtilization)),
                            fixed(averageOf(runs, run -> run.direction(0).lineUtilization())),
                            fixed(averageOf(runs, run -> run.direction(1).lineUtilization())),
                            fixed(averageOf(runs, run -> run.direction(0).refusalProbability())),
                            fixed(averageOf(runs, run -> run.direction(1).refusalProbability())),
                            fixed(averageOf(runs, RunMetrics::refusalProbability)),
                            fixed(averageOf(runs, run -> run.direction(0).meanMessages()), 3),
                            fixed(averageOf(runs, run -> run.direction(0).meanDeliveryTime()), 2),
                            fixed(averageOf(runs, RunMetrics::meanDeliveryTime), 2),
                            fixed(averageOf(runs, run -> Descriptive.mean(run.deliveryTimes())), 2),
                            fixed(averageOf(runs, RunMetrics::meanInputBuffer), 3));
            context.output()
                    .line(row.stream().map(String::valueOf).collect(Collectors.joining(";")));
            rows.add(row);
        }
        context.store().save(file, HEADER, rows);
    }
}
