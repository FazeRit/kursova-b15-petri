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

public final class StudyFlowLimit {
    private static final long FIRST_SEED = 3000;
    private static final List<Integer> LIMITS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    private static final List<String> HEADER =
            List.of(
                    "L", "Pref", "Pref_ci", "Uproc", "U1", "W", "W_ci", "W_std", "W_min", "W_max",
                    "Lin");

    public static void run(Output out) {
        out.section("Вплив обмеження методу контролю потоків");
        List<Point> refusalPoints = new ArrayList<>();
        List<Point> delayPoints = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        for (int limit : LIMITS) {
            List<RunMetrics> runs =
                    SwitchingNodeSimulation.runs(
                            SwitchingNodeConfig.BASE.withFlowLimit(limit),
                            SwitchingNodeSimulation.RUN_COUNT,
                            FIRST_SEED,
                            true);
            Estimate refusal =
                    Estimate.of(Summaries.valuesOf(runs, RunMetrics::refusalProbability));
            Estimate delivery = Estimate.of(Summaries.valuesOf(runs, RunMetrics::meanDeliveryTime));
            Summaries.DeliverySpread spread = Summaries.deliverySpread(runs);

            if (refusal.mean() > 0)
                refusalPoints.add(new Point(StrictMath.log(limit), StrictMath.log(refusal.mean())));
            delayPoints.add(new Point(limit, delivery.mean()));
            out.line(
                    "L = "
                            + limit
                            + ": Pвідм = "
                            + fixed(refusal.mean(), 5)
                            + " ± "
                            + fixed(refusal.halfWidth(), 5)
                            + ", W = "
                            + fixed(delivery.mean(), 3)
                            + " ± "
                            + fixed(delivery.halfWidth(), 3)
                            + " мс");
            rows.add(
                    List.of(
                            limit,
                            fixed(refusal.mean(), 6),
                            fixed(refusal.halfWidth(), 6),
                            fixed(averageOf(runs, RunMetrics::processorUtilization), 5),
                            fixed(averageOf(runs, run -> run.direction(0).lineUtilization()), 5),
                            fixed(delivery.mean(), 4),
                            fixed(delivery.halfWidth(), 4),
                            fixed(spread.meanDeviation(), 4),
                            fixed(spread.overallMin(), 3),
                            fixed(spread.overallMax(), 3),
                            fixed(averageOf(runs, RunMetrics::meanInputBuffer), 4)));
        }

        out.save("limit_study.csv", HEADER, rows);
        Summaries.reportRegression("ln Pвідм(ln L)", refusalPoints, "reg_pref", out);
        Summaries.reportRegression("W(L)", delayPoints, "reg_w", out);
    }
}
