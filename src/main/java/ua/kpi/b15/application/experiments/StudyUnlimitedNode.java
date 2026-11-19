package ua.kpi.b15.application.experiments;

import static ua.kpi.b15.application.Format.fixed;
import static ua.kpi.b15.application.experiments.Summaries.averageOf;

import ua.kpi.b15.application.Experiment;
import ua.kpi.b15.application.ExperimentContext;
import ua.kpi.b15.application.ExperimentPlan;
import ua.kpi.b15.application.Replications.Options;
import ua.kpi.b15.domain.statistics.Estimate;
import ua.kpi.b15.domain.statistics.LinearRegression.Point;
import ua.kpi.b15.domain.switching.RunMetrics;
import ua.kpi.b15.domain.switching.SwitchingNodeConfig;

import java.util.ArrayList;
import java.util.List;

public final class StudyUnlimitedNode implements Experiment {
    private static final long FIRST_SEED = 4000;
    private static final long DISTRIBUTION_SEED = 2000;
    private static final List<Integer> DURATIONS =
            List.of(10_000, 20_000, 50_000, 100_000, 200_000, 500_000, 1_000_000);
    private static final SwitchingNodeConfig UNLIMITED =
            SwitchingNodeConfig.BASE.withoutFlowControl();

    private static final List<String> HEADER =
            List.of(
                    "Tmod",
                    "W",
                    "W_ci",
                    "W_std",
                    "W_min",
                    "W_max_avg",
                    "n1",
                    "Uproc",
                    "U1",
                    "Lin",
                    "Lout1");

    @Override
    public void run(ExperimentContext context) {
        context.output().section("Система без обмежень методу контролю потоків");
        List<Point> growth = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        for (int duration : DURATIONS) {
            List<RunMetrics> runs =
                    context.replications()
                            .run(
                                    UNLIMITED,
                                    ExperimentPlan.RUN_COUNT,
                                    Options.defaults()
                                            .withDuration(duration)
                                            .collectingDeliveryTimes()
                                            .withFirstSeed(FIRST_SEED));
            Estimate delivery = Estimate.of(Summaries.valuesOf(runs, RunMetrics::meanDeliveryTime));
            Summaries.DeliverySpread spread = Summaries.deliverySpread(runs);
            growth.add(new Point(StrictMath.log(duration), StrictMath.log(delivery.mean())));
            context.output()
                    .line(
                            "Tмод = "
                                    + duration
                                    + ": W = "
                                    + fixed(delivery.mean(), 2)
                                    + " ± "
                                    + fixed(delivery.halfWidth(), 2)
                                    + " мс");
            rows.add(
                    List.of(
                            duration,
                            fixed(delivery.mean(), 4),
                            fixed(delivery.halfWidth(), 4),
                            fixed(spread.meanDeviation(), 4),
                            fixed(spread.overallMin(), 3),
                            fixed(spread.meanMax(), 3),
                            fixed(averageOf(runs, run -> run.direction(0).meanMessages()), 4),
                            fixed(averageOf(runs, RunMetrics::processorUtilization), 5),
                            fixed(averageOf(runs, run -> run.direction(0).lineUtilization()), 5),
                            fixed(averageOf(runs, RunMetrics::meanInputBuffer), 4),
                            fixed(averageOf(runs, run -> run.direction(0).meanOutputBuffer()), 4)));
        }

        context.store().save("unlimited_study.csv", HEADER, rows);
        Summaries.reportRegression("ln W(ln Tмод)", growth, "reg_unlimited", context);

        List<RunMetrics> runs =
                context.replications()
                        .run(
                                UNLIMITED,
                                ExperimentPlan.RUN_COUNT,
                                Options.defaults()
                                        .collectingDeliveryTimes()
                                        .withFirstSeed(DISTRIBUTION_SEED));
        DeliveryTimeAnalysis.analyse(
                "unlimited", DeliveryTimeAnalysis.thinnedDeliveryTimes(runs), context);
    }
}
