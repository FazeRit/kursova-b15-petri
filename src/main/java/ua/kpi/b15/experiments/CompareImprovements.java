package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;
import static ua.kpi.b15.experiments.Summaries.averageOf;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.RunMetrics;
import ua.kpi.b15.model.SwitchingNodeConfig;
import ua.kpi.b15.model.SwitchingNodeSimulation;
import ua.kpi.b15.stats.Estimate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CompareImprovements {
    private static final long FIRST_SEED = 5000;
    private static final int ANOVA_VARIANTS = 4;
    private static final SwitchingNodeConfig BASE = SwitchingNodeConfig.BASE;

    private static final List<Map.Entry<String, SwitchingNodeConfig>> VARIANTS =
            List.of(
                    Map.entry("Базова система (L = 3)", BASE),
                    Map.entry("L = 5", BASE.withFlowLimit(5)),
                    Map.entry("tl = unif(13, 5), L = 3", BASE.withTransmission(13, 5)),
                    Map.entry(
                            "tl = unif(13, 5), L = 5",
                            BASE.withTransmission(13, 5).withFlowLimit(5)),
                    Map.entry(
                            "tl = unif(13, 5), без обмежень",
                            BASE.withTransmission(13, 5).withoutFlowControl()));

    private static final List<String> HEADER =
            List.of(
                    "variant",
                    "Pref",
                    "Pref_ci",
                    "Uproc",
                    "U1",
                    "W",
                    "W_ci",
                    "W_std",
                    "W_max",
                    "throughput");

    public static void run(Output out) {
        out.section("Порівняння модифікацій системи");
        List<double[]> refusalGroups = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        for (Map.Entry<String, SwitchingNodeConfig> variant : VARIANTS) {
            List<RunMetrics> runs =
                    SwitchingNodeSimulation.runs(
                            variant.getValue(),
                            SwitchingNodeSimulation.RUN_COUNT,
                            FIRST_SEED,
                            true);
            double[] refusals = Summaries.valuesOf(runs, RunMetrics::refusalProbability);
            Estimate refusal = Estimate.of(refusals);
            Estimate delivery = Estimate.of(Summaries.valuesOf(runs, RunMetrics::meanDeliveryTime));
            Summaries.DeliverySpread spread = Summaries.deliverySpread(runs);
            double throughput = averageOf(runs, RunMetrics::throughputPerSecond);
            refusalGroups.add(refusals);
            out.line(
                    variant.getKey()
                            + ": Pвідм = "
                            + fixed(refusal.mean(), 5)
                            + ", W = "
                            + fixed(delivery.mean(), 2)
                            + " мс, пропускна здатність "
                            + fixed(throughput, 2)
                            + " 1/с");
            rows.add(
                    List.of(
                            variant.getKey(),
                            fixed(refusal.mean(), 6),
                            fixed(refusal.halfWidth(), 6),
                            fixed(averageOf(runs, RunMetrics::processorUtilization), 5),
                            fixed(averageOf(runs, run -> run.direction(0).lineUtilization()), 5),
                            fixed(delivery.mean(), 4),
                            fixed(delivery.halfWidth(), 4),
                            fixed(spread.meanDeviation(), 4),
                            fixed(spread.overallMax(), 3),
                            fixed(throughput, 4)));
        }

        out.save("proposals.csv", HEADER, rows);
        Summaries.reportAnova(
                "модифікація → Pвідм",
                refusalGroups.subList(0, ANOVA_VARIANTS),
                "anova_proposals",
                out);
    }
}
