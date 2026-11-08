package ua.kpi.b15.domain.statistics;

public record Estimate(double mean, double deviation, double halfWidth) {
    private static final double CONFIDENCE = 0.95;

    public static Estimate of(double[] values) {
        double center = Descriptive.mean(values);
        double deviation = Descriptive.standardDeviation(values);
        double halfWidth =
                values.length > 1
                        ? (Probability.studentCritical(values.length - 1) * deviation)
                                / Math.sqrt(values.length)
                        : 0;
        return new Estimate(center, deviation, halfWidth);
    }

    public static int chebyshevRunCount(double deviation, double accuracy) {
        return (int) Math.ceil((deviation * deviation) / (accuracy * accuracy * (1 - CONFIDENCE)));
    }
}
