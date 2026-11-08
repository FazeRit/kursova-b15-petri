package ua.kpi.b15.domain.statistics;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public record DistributionFits(
        double mean,
        double deviation,
        double gammaShape,
        double gammaScale,
        Weibull weibull,
        List<Law> laws) {
    public record Law(String name, DoubleUnaryOperator cdf, int parameters) {}

    public record Weibull(double shape, double scale, double shift) {}

    public static DistributionFits byMoments(double[] sample) {
        double center = Descriptive.mean(sample);
        double deviation = Descriptive.standardDeviation(sample);
        double gammaShape = (center * center) / (deviation * deviation);
        double gammaScale = (deviation * deviation) / center;
        double logDeviation =
                Math.sqrt(StrictMath.log(1 + (deviation * deviation) / (center * center)));
        double logMean = StrictMath.log(center) - (logDeviation * logDeviation) / 2;
        Weibull weibull = shiftedWeibull(sample);

        List<Law> laws =
                List.of(
                        new Law(
                                "нормальний",
                                x -> Probability.normalCdf((x - center) / deviation),
                                2),
                        new Law("гамма", x -> Probability.gammaCdf(x, gammaShape, gammaScale), 2),
                        new Law(
                                "логнормальний",
                                x ->
                                        x <= 0
                                                ? 0
                                                : Probability.normalCdf(
                                                        (StrictMath.log(x) - logMean)
                                                                / logDeviation),
                                2),
                        new Law(
                                "Вейбулла зі зсувом",
                                x ->
                                        x <= weibull.shift()
                                                ? 0
                                                : 1
                                                        - StrictMath.exp(
                                                                -StrictMath.pow(
                                                                        (x - weibull.shift())
                                                                                / weibull.scale(),
                                                                        weibull.shape())),
                                3));
        return new DistributionFits(center, deviation, gammaShape, gammaScale, weibull, laws);
    }

    public static Weibull shiftedWeibull(double[] sample) {
        double center = Descriptive.mean(sample);
        double deviation = Descriptive.standardDeviation(sample);
        double skewness = 0;
        for (double value : sample) skewness += StrictMath.pow((value - center) / deviation, 3);
        skewness /= sample.length;

        double low = 1.05;
        double high = 3.6;
        for (int i = 0; i < 100; i++) {
            double middle = (low + high) / 2;
            if (weibullSkewness(middle) > skewness) low = middle;
            else high = middle;
        }
        double shape = (low + high) / 2;
        double scale =
                deviation
                        / Math.sqrt(
                                gammaMoment(shape, 2) - StrictMath.pow(gammaMoment(shape, 1), 2));
        return new Weibull(shape, scale, center - scale * gammaMoment(shape, 1));
    }

    private static double gammaMoment(double shape, int order) {
        return StrictMath.exp(SpecialFunctions.logGamma(1 + order / shape));
    }

    private static double weibullSkewness(double shape) {
        double first = gammaMoment(shape, 1);
        double second = gammaMoment(shape, 2);
        double third = gammaMoment(shape, 3);
        return (third - 3 * first * second + 2 * StrictMath.pow(first, 3))
                / StrictMath.pow(second - first * first, 1.5);
    }
}
