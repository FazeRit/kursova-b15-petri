package ua.kpi.b15.model;

public record SwitchingNodeConfig(
        double arrivalMean,
        double arrivalSpread,
        double processingTime,
        double transmissionMean,
        double transmissionSpread,
        int flowLimit,
        boolean flowControl,
        double firstDirectionShare) {
    public static final SwitchingNodeConfig BASE =
            new SwitchingNodeConfig(15, 7, 7, 15, 5, 3, true, 0.5);

    private static final int UNLIMITED_PERMITS = 1_000_000_000;

    public int permitsPerDirection() {
        return flowControl ? flowLimit : UNLIMITED_PERMITS;
    }

    public SwitchingNodeConfig withArrival(double mean, double spread) {
        return new SwitchingNodeConfig(
                mean,
                spread,
                processingTime,
                transmissionMean,
                transmissionSpread,
                flowLimit,
                flowControl,
                firstDirectionShare);
    }

    public SwitchingNodeConfig withProcessingTime(double time) {
        return new SwitchingNodeConfig(
                arrivalMean,
                arrivalSpread,
                time,
                transmissionMean,
                transmissionSpread,
                flowLimit,
                flowControl,
                firstDirectionShare);
    }

    public SwitchingNodeConfig withTransmission(double mean, double spread) {
        return new SwitchingNodeConfig(
                arrivalMean,
                arrivalSpread,
                processingTime,
                mean,
                spread,
                flowLimit,
                flowControl,
                firstDirectionShare);
    }

    public SwitchingNodeConfig withFlowLimit(int limit) {
        return new SwitchingNodeConfig(
                arrivalMean,
                arrivalSpread,
                processingTime,
                transmissionMean,
                transmissionSpread,
                limit,
                flowControl,
                firstDirectionShare);
    }

    public SwitchingNodeConfig withoutFlowControl() {
        return new SwitchingNodeConfig(
                arrivalMean,
                arrivalSpread,
                processingTime,
                transmissionMean,
                transmissionSpread,
                flowLimit,
                false,
                firstDirectionShare);
    }

    public SwitchingNodeConfig withFirstDirectionShare(double share) {
        return new SwitchingNodeConfig(
                arrivalMean,
                arrivalSpread,
                processingTime,
                transmissionMean,
                transmissionSpread,
                flowLimit,
                flowControl,
                share);
    }
}
