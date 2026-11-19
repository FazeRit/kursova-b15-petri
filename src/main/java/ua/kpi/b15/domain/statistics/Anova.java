package ua.kpi.b15.domain.statistics;

import java.util.Arrays;
import java.util.List;

public record Anova(
        double factorVariance,
        double residualVariance,
        double statistic,
        double critical,
        int factorDegrees,
        int residualDegrees,
        boolean significant) {
    public static Anova oneWay(List<double[]> groups) {
        int levels = groups.size();
        int runs = groups.getFirst().length;
        double grandMean =
                Descriptive.mean(groups.stream().flatMapToDouble(Arrays::stream).toArray());
        double factorSum = 0;
        for (double[] group : groups)
            factorSum += StrictMath.pow(Descriptive.mean(group) - grandMean, 2);
        double factorSquares = runs * factorSum;
        double residualSquares = 0;
        for (double[] group : groups) {
            double groupMean = Descriptive.mean(group);
            double inner = 0;
            for (double value : group) inner += StrictMath.pow(value - groupMean, 2);
            residualSquares += inner;
        }
        int factorDegrees = levels - 1;
        int residualDegrees = levels * (runs - 1);
        double factorVariance = factorSquares / factorDegrees;
        double residualVariance = residualSquares / residualDegrees;
        double statistic = factorVariance / residualVariance;
        double critical = Probability.fisherCritical(factorDegrees, residualDegrees);
        return new Anova(
                factorVariance,
                residualVariance,
                statistic,
                critical,
                factorDegrees,
                residualDegrees,
                statistic > critical);
    }
}
