package ua.kpi.b15.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Format {
    private static final int DEFAULT_DIGITS = 4;

    private Format() {}

    public static String fixed(double value) {
        return fixed(value, DEFAULT_DIGITS);
    }

    public static String fixed(double value, int digits) {
        if (Double.isNaN(value)) return "NaN";
        if (Double.isInfinite(value)) return value > 0 ? "Infinity" : "-Infinity";
        String text = new BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).toPlainString();
        return value < 0 && !text.startsWith("-") ? "-" + text : text;
    }

    public static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
