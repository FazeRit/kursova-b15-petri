package ua.kpi.b15.model;

import java.util.List;

public record RunMetrics(
        double observedTime,
        double processorUtilization,
        double meanInputBuffer,
        double refusalProbability,
        double meanDeliveryTime,
        double throughputPerSecond,
        List<DirectionMetrics> directions,
        double[] deliveryTimes) {
    public DirectionMetrics direction(int index) {
        return directions.get(index);
    }
}
