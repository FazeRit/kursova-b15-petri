package ua.kpi.b15.model;

public record DirectionMetrics(
        int arrived,
        int refused,
        int transmitted,
        double lineUtilization,
        double refusalProbability,
        double meanMessages,
        double meanOutputBuffer,
        double meanDeliveryTime) {}
