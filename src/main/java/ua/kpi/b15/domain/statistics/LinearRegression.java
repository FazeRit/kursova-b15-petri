package ua.kpi.b15.domain.statistics;

import java.util.List;

public record LinearRegression(double intercept, double slope, double correlation) {
    public record Point(double x, double y) {}

    public static LinearRegression fit(List<Point> points) {
        int n = points.size();
        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        for (Point point : points) sumX += point.x();
        for (Point point : points) sumY += point.y();
        for (Point point : points) sumXX += point.x() * point.x();
        for (Point point : points) sumXY += point.x() * point.y();
        double determinant = n * sumXX - sumX * sumX;
        double intercept = (sumXX * sumY - sumX * sumXY) / determinant;
        double slope = (n * sumXY - sumX * sumY) / determinant;
        double meanY = sumY / n;
        double residual = 0;
        double total = 0;
        for (Point point : points)
            residual += StrictMath.pow(point.y() - (intercept + slope * point.x()), 2);
        for (Point point : points) total += StrictMath.pow(point.y() - meanY, 2);
        return new LinearRegression(intercept, slope, Math.sqrt(1 - residual / total));
    }
}
