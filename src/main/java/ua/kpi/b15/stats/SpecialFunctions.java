package ua.kpi.b15.stats;

public final class SpecialFunctions {
    private static final double[] LANCZOS = {
        76.18009172947146, -86.50532032941677, 24.01409824083091,
        -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5,
    };
    private static final double TINY = 1e-300;
    private static final double PRECISION = 1e-14;
    private static final int MAX_ITERATIONS = 1000;

    private SpecialFunctions() {}

    public static double logGamma(double x) {
        double y = x;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * StrictMath.log(tmp);
        double series = 1.000000000190015;
        for (double coefficient : LANCZOS) series += coefficient / ++y;
        return -tmp + StrictMath.log((2.5066282746310005 * series) / x);
    }

    public static double regularizedGammaP(double a, double x) {
        if (x <= 0) return 0;
        return x < a + 1 ? gammaSeries(a, x) : 1 - gammaContinuedFraction(a, x);
    }

    public static double regularizedBeta(double a, double b, double x) {
        if (x <= 0) return 0;
        if (x >= 1) return 1;
        double front =
                StrictMath.exp(
                        logGamma(a + b)
                                - logGamma(a)
                                - logGamma(b)
                                + a * StrictMath.log(x)
                                + b * StrictMath.log(1 - x));
        return x < (a + 1) / (a + b + 2)
                ? (front * betaContinuedFraction(a, b, x)) / a
                : 1 - (front * betaContinuedFraction(b, a, 1 - x)) / b;
    }

    private static double avoidZero(double value) {
        return Math.abs(value) < TINY ? TINY : value;
    }

    private static double gammaSeries(double a, double x) {
        double denominator = a;
        double sum = 1 / a;
        double term = sum;
        for (int n = 0; n < MAX_ITERATIONS; n++) {
            denominator++;
            term *= x / denominator;
            sum += term;
            if (Math.abs(term) < Math.abs(sum) * PRECISION) break;
        }
        return sum * StrictMath.exp(-x + a * StrictMath.log(x) - logGamma(a));
    }

    private static double gammaContinuedFraction(double a, double x) {
        double b = x + 1 - a;
        double c = 1e300;
        double d = 1 / b;
        double result = d;
        for (int i = 1; i < MAX_ITERATIONS; i++) {
            double an = -i * (i - a);
            b += 2;
            d = 1 / avoidZero(an * d + b);
            c = avoidZero(b + an / c);
            double delta = d * c;
            result *= delta;
            if (Math.abs(delta - 1) < PRECISION) break;
        }
        return StrictMath.exp(-x + a * StrictMath.log(x) - logGamma(a)) * result;
    }

    private static double betaContinuedFraction(double a, double b, double x) {
        double sum = a + b;
        double next = a + 1;
        double previous = a - 1;
        double c = 1;
        double d = 1 / avoidZero(1 - (sum * x) / next);
        double result = d;
        for (int m = 1; m <= MAX_ITERATIONS; m++) {
            int twice = 2 * m;
            double even = (m * (b - m) * x) / ((previous + twice) * (a + twice));
            d = 1 / avoidZero(1 + even * d);
            c = avoidZero(1 + even / c);
            result *= d * c;
            double odd = (-(a + m) * (sum + m) * x) / ((a + twice) * (next + twice));
            d = 1 / avoidZero(1 + odd * d);
            c = avoidZero(1 + odd / c);
            double delta = d * c;
            result *= delta;
            if (Math.abs(delta - 1) < PRECISION) break;
        }
        return result;
    }
}
