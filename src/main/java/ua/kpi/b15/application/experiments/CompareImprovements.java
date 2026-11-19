package ua.kpi.b15.application.experiments;

import static ua.kpi.b15.application.Format.fixed;
import static ua.kpi.b15.application.experiments.Summaries.averageOf;

import ua.kpi.b15.application.Experiment;
import ua.kpi.b15.application.ExperimentContext;
import ua.kpi.b15.application.ExperimentPlan;
import ua.kpi.b15.application.Replications.Options;
import ua.kpi.b15.domain.statistics.Estimate;
import ua.kpi.b15.domain.switching.RunMetrics;
import ua.kpi.b15.domain.switching.SwitchingNodeConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CompareImprovements implements Experiment {
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

    @Override
    public void run(ExperimentContext context) {
        context.output().section("Порівняння модифікацій системи");
        List<double[]> refusalGroups = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        for (Map.Entry<String, SwitchingNodeConfig> variant : VARIANTS) {
            List<RunMetrics> runs =
                    context.replications()
                            .run(
                                    variant.getValue(),
                                    ExperimentPlan.RUN_COUNT,
                                    Options.defaults()
                                            .collectingDeliveryTimes()
                                            .withFirstSeed(FIRST_SEED));
            double[] refusals = Summaries.valuesOf(runs, RunMetrics::refusalProbability);
            Estimate refusal = Estimate.of(refusals);
            Estimate delivery = Estimate.of(Summaries.valuesOf(runs, RunMetrics::meanDeliveryTime));
            Summaries.DeliverySpread spread = Summaries.deliverySpread(runs);
            double throughput = averageOf(runs, RunMetrics::throughputPerSecond);
            refusalGroups.add(refusals);
            context.output()
                    .line(
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

        context.store().save("proposals.csv", HEADER, rows);
        Summaries.reportAnova(
                "модифікація → Pвідм",
                refusalGroups.subList(0, ANOVA_VARIANTS),
                "anova_proposals",
                context);
    }
}
