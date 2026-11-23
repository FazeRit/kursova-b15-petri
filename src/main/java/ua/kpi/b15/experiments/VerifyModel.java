package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;
import static ua.kpi.b15.experiments.Summaries.averageOf;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.RunMetrics;
import ua.kpi.b15.model.SwitchingNodeConfig;
import ua.kpi.b15.model.SwitchingNodeSimulation;
import ua.kpi.b15.stats.Descriptive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class VerifyModel {
    private static final SwitchingNodeConfig BASE = SwitchingNodeConfig.BASE;
    private static final SwitchingNodeConfig MODIFIED =
            BASE.withTransmission(13, 5).withFlowLimit(5);

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

    private static final List<Map.Entry<String, SwitchingNodeConfig>> MODIFIED_VARIANTS =
            List.of(
                    Map.entry("Модифіковані значення", MODIFIED),
                    Map.entry("a = unif(12, 7)", MODIFIED.withArrival(12, 7)),
                    Map.entry("a = unif(20, 7)", MODIFIED.withArrival(20, 7)),
                    Map.entry("a = unif(1000, 7)", MODIFIED.withArrival(1000, 7)),
                    Map.entry("tp = 3", MODIFIED.withProcessingTime(3)),
                    Map.entry("tp = 9", MODIFIED.withProcessingTime(9)),
                    Map.entry("tl = unif(15, 5)", MODIFIED.withTransmission(15, 5)),
                    Map.entry("tl = unif(20, 5)", MODIFIED.withTransmission(20, 5)),
                    Map.entry("L = 1", MODIFIED.withFlowLimit(1)),
                    Map.entry("L = 3", MODIFIED.withFlowLimit(3)),
                    Map.entry("b1 = 0,9", MODIFIED.withFirstDirectionShare(0.9)),
                    Map.entry(
                            "a = 15, tl = 13 (детерм.)",
                            MODIFIED.withArrival(15, 0).withTransmission(13, 0)));

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

    public static void run(Output out) {
        verify(out, "Верифікація базової моделі", "verification.csv", BASE_VARIANTS);
        verify(
                out,
                "Верифікація модифікованої моделі",
                "verification_modified.csv",
                MODIFIED_VARIANTS);
    }

    private static void verify(
            Output out,
            String title,
            String file,
            List<Map.Entry<String, SwitchingNodeConfig>> variants) {
        out.section(title);
        List<List<Object>> rows = new ArrayList<>();
        for (int index = 0; index < variants.size(); index++) {
            Map.Entry<String, SwitchingNodeConfig> variant = variants.get(index);
            List<RunMetrics> runs =
                    SwitchingNodeSimulation.runs(
                            variant.getValue(),
                            SwitchingNodeSimulation.RUN_COUNT,
                            SwitchingNodeSimulation.DEFAULT_SEED,
                            true);
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
            out.line(row.stream().map(String::valueOf).collect(Collectors.joining(";")));
            rows.add(row);
        }
        out.save(file, HEADER, rows);
    }
}
