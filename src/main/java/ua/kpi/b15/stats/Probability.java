package ua.kpi.b15.stats;

import java.util.function.DoubleUnaryOperator;

public final class Probability {
    private static final double SIGNIFICANCE = 0.05;

    private Probability() {}

    public static double chiSquareCdf(double x, double degrees) {
        return SpecialFunctions.regularizedGammaP(degrees / 2, x / 2);
    }

    public static double chiSquareCritical(int degrees) {
        return inverse(x -> chiSquareCdf(x, degrees), 1 - SIGNIFICANCE);
    }

    public static double fisherCdf(double f, double first, double second) {
        return f <= 0
                ? 0
                : SpecialFunctions.regularizedBeta(
                        first / 2, second / 2, (first * f) / (first * f + second));
    }

    public static double fisherCritical(int first, int second) {
        return inverse(x -> fisherCdf(x, first, second), 1 - SIGNIFICANCE);
    }

    public static double studentCritical(int degrees) {
        return Math.sqrt(fisherCritical(1, degrees));
    }

    public static double normalCdf(double z) {
        return 0.5 * (1 + Math.signum(z) * SpecialFunctions.regularizedGammaP(0.5, (z * z) / 2));
    }

    public static double gammaCdf(double x, double shape, double scale) {
        return x <= 0 ? 0 : SpecialFunctions.regularizedGammaP(shape, x / scale);
    }

    private static double inverse(DoubleUnaryOperator cdf, double level) {
        double low = 0;
        double high = 1000;
        for (int i = 0; i < 200; i++) {
            double middle = (low + high) / 2;
            if (cdf.applyAsDouble(middle) < level) low = middle;
            else high = middle;
        }
        return (low + high) / 2;
    }
}
