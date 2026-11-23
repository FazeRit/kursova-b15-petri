package ua.kpi.b15.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public final class ChiSquareTest {
    private static final double MIN_EXPECTED = 5;
    private static final int DEFAULT_BINS = 20;

    public record Result(
            double statistic, int degrees, double critical, boolean accepted, int intervals) {}

    private record Intervals(List<Double> observed, List<Double> expected) {}

    private ChiSquareTest() {}

    public static Result test(double[] sample, DoubleUnaryOperator cdf, int estimatedParameters) {
        return test(
                sample,
                cdf,
                estimatedParameters,
                DEFAULT_BINS,
                Descriptive.minimum(sample),
                Descriptive.maximum(sample));
    }

    public static Result test(
            double[] sample,
            DoubleUnaryOperator cdf,
            int estimatedParameters,
            int bins,
            double low,
            double high) {
        double width = (high - low) / bins;
        double[] observed = new double[bins];
        for (double value : sample) {
            int bin = (int) Math.min(bins - 1, Math.max(0, Math.floor((value - low) / width)));
            observed[bin] += 1;
        }
        double[] expected = new double[bins];
        for (int bin = 0; bin < bins; bin++) {
            double left = bin == 0 ? 0 : cdf.applyAsDouble(low + bin * width);
            double right = bin == bins - 1 ? 1 : cdf.applyAsDouble(low + (bin + 1) * width);
            expected[bin] = sample.length * (right - left);
        }

        Intervals merged = mergeSmallIntervals(observed, expected);
        double statistic = 0;
        for (int index = 0; index < merged.observed().size(); index++) {
            double difference = merged.observed().get(index) - merged.expected().get(index);
            statistic += StrictMath.pow(difference, 2) / merged.expected().get(index);
        }
        int degrees = merged.observed().size() - 1 - estimatedParameters;
        double critical = Probability.chiSquareCritical(Math.max(degrees, 1));
        return new Result(
                statistic, degrees, critical, statistic < critical, merged.observed().size());
    }

    private static Intervals mergeSmallIntervals(double[] observed, double[] expected) {
        List<Double> mergedObserved = new ArrayList<>();
        List<Double> mergedExpected = new ArrayList<>();
        double pendingObserved = 0;
        double pendingExpected = 0;
        for (int bin = 0; bin < observed.length; bin++) {
            pendingObserved += observed[bin];
            pendingExpected += expected[bin];
            if (pendingExpected >= MIN_EXPECTED) {
                mergedObserved.add(pendingObserved);
                mergedExpected.add(pendingExpected);
                pendingObserved = 0;
                pendingExpected = 0;
            }
        }
        if (pendingObserved > 0 || pendingExpected > 0) {
            if (!mergedExpected.isEmpty()) {
                int last = mergedExpected.size() - 1;
                mergedObserved.set(last, mergedObserved.get(last) + pendingObserved);
                mergedExpected.set(last, mergedExpected.get(last) + pendingExpected);
            } else {
                mergedObserved.add(pendingObserved);
                mergedExpected.add(pendingExpected);
            }
        }
        return new Intervals(mergedObserved, mergedExpected);
    }
}
