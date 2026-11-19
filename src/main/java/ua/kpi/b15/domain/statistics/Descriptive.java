package ua.kpi.b15.domain.statistics;

public record Descriptive(double mean, double deviation, double min, double max) {
    public static Descriptive of(double[] values) {
        return new Descriptive(
                mean(values), standardDeviation(values), minimum(values), maximum(values));
    }

    public static double mean(double[] values) {
        double sum = 0;
        for (double value : values) sum += value;
        return sum / values.length;
    }

    public static double standardDeviation(double[] values) {
        if (values.length < 2) return 0;
        double center = mean(values);
        double squares = 0;
        for (double value : values) squares += (value - center) * (value - center);
        return Math.sqrt(squares / (values.length - 1));
    }

    public static double minimum(double[] values) {
        double least = Double.POSITIVE_INFINITY;
        for (double value : values) if (value < least) least = value;
        return least;
    }

    public static double maximum(double[] values) {
        double most = Double.NEGATIVE_INFINITY;
        for (double value : values) if (value > most) most = value;
        return most;
    }
}
