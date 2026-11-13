package ua.kpi.b15.application.experiments;

import static ua.kpi.b15.application.Format.fixed;

import ua.kpi.b15.application.ExperimentContext;
import ua.kpi.b15.domain.statistics.Anova;
import ua.kpi.b15.domain.statistics.Descriptive;
import ua.kpi.b15.domain.statistics.LinearRegression;
import ua.kpi.b15.domain.switching.RunMetrics;

import java.util.List;
import java.util.function.ToDoubleFunction;

final class Summaries {
    record DeliverySpread(
            double meanDeviation, double overallMin, double overallMax, double meanMax) {}

    private Summaries() {}

    static double[] valuesOf(List<RunMetrics> runs, ToDoubleFunction<RunMetrics> select) {
        return runs.stream().mapToDouble(select).toArray();
    }

    static double averageOf(List<RunMetrics> runs, ToDoubleFunction<RunMetrics> select) {
        return Descriptive.mean(valuesOf(runs, select));
    }

    static DeliverySpread deliverySpread(List<RunMetrics> runs) {
        return new DeliverySpread(
                averageOf(runs, run -> Descriptive.standardDeviation(run.deliveryTimes())),
                Descriptive.minimum(
                        valuesOf(runs, run -> Descriptive.minimum(run.deliveryTimes()))),
                Descriptive.maximum(
                        valuesOf(runs, run -> Descriptive.maximum(run.deliveryTimes()))),
                averageOf(runs, run -> Descriptive.maximum(run.deliveryTimes())));
    }

    static LinearRegression reportRegression(
            String title,
            List<LinearRegression.Point> points,
            String file,
            ExperimentContext context) {
        LinearRegression fit = LinearRegression.fit(points);
        context.output()
                .line(
                        "Регресія "
                                + title
                                + ": b0 = "
                                + fixed(fit.intercept(), 5)
                                + ", b1 = "
                                + fixed(fit.slope(), 5)
                                + ", R = "
                                + fixed(fit.correlation(), 5));
        context.store()
                .save(
                        file + ".csv",
                        List.of("b0", "b1", "R"),
                        List.of(
                                List.of(
                                        fixed(fit.intercept(), 6),
                                        fixed(fit.slope(), 6),
                                        fixed(fit.correlation(), 6))));
        return fit;
    }

    static Anova reportAnova(
            String title, List<double[]> groups, String file, ExperimentContext context) {
        Anova result = Anova.oneWay(groups);
        context.output()
                .line(
                        "ANOVA "
                                + title
                                + ": F = "
                                + fixed(result.statistic(), 3)
                                + ", Fкр = "
                                + fixed(result.critical(), 3)
                                + " - вплив "
                                + (result.significant() ? "суттєвий" : "несуттєвий"));
        context.store()
                .save(
                        file + ".csv",
                        List.of("Dfact", "Dres", "F", "Fcrit", "d1", "d2"),
                        List.of(
                                List.of(
                                        fixed(result.factorVariance(), 8),
                                        fixed(result.residualVariance(), 8),
                                        fixed(result.statistic(), 4),
                                        fixed(result.critical(), 4),
                                        result.factorDegrees(),
                                        result.residualDegrees())));
        return result;
    }
}
