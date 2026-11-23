package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;
import static ua.kpi.b15.experiments.Summaries.averageOf;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.RunMetrics;
import ua.kpi.b15.model.SwitchingNodeConfig;
import ua.kpi.b15.model.SwitchingNodeSimulation;
import ua.kpi.b15.stats.Estimate;
import ua.kpi.b15.stats.LinearRegression.Point;

import java.util.ArrayList;
import java.util.List;

public final class StudyUnlimitedNode {
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

    public static void run(Output out) {
        out.section("Система без обмежень методу контролю потоків");
        List<Point> growth = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        for (int duration : DURATIONS) {
            List<RunMetrics> runs =
                    SwitchingNodeSimulation.runs(
                            UNLIMITED,
                            SwitchingNodeSimulation.RUN_COUNT,
                            FIRST_SEED,
                            duration,
                            true);
            Estimate delivery = Estimate.of(Summaries.valuesOf(runs, RunMetrics::meanDeliveryTime));
            Summaries.DeliverySpread spread = Summaries.deliverySpread(runs);
            growth.add(new Point(StrictMath.log(duration), StrictMath.log(delivery.mean())));
            out.line(
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

        out.save("unlimited_study.csv", HEADER, rows);
        Summaries.reportRegression("ln W(ln Tмод)", growth, "reg_unlimited", out);

        List<RunMetrics> runs =
                SwitchingNodeSimulation.runs(
                        UNLIMITED, SwitchingNodeSimulation.RUN_COUNT, DISTRIBUTION_SEED, true);
        DeliveryTimeAnalysis.analyse(
                "unlimited", DeliveryTimeAnalysis.thinnedDeliveryTimes(runs), out);
    }
}
