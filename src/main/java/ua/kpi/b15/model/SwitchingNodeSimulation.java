package ua.kpi.b15.model;

import java.util.ArrayList;
import java.util.List;

public final class SwitchingNodeSimulation {
    public static final double SIMULATION_TIME = 100_000;
    public static final double WARM_UP_TIME = 2_000;
    public static final int RUN_COUNT = 45;
    public static final long DEFAULT_SEED = 1000;

    private SwitchingNodeSimulation() {}

    public static RunMetrics simulate(
            SwitchingNodeConfig config,
            double duration,
            double warmUp,
            boolean collectDeliveryTimes,
            Rng random) {
        SwitchingNodeNet node = new SwitchingNodeNet(config, random);
        SwitchingNodeStatistics statistics =
                new SwitchingNodeStatistics(node, warmUp, collectDeliveryTimes);
        node.object().simulate(duration, statistics);
        return statistics.metrics();
    }

    public static List<RunMetrics> runs(
            SwitchingNodeConfig config,
            int count,
            long firstSeed,
            double duration,
            boolean collectDeliveryTimes) {
        List<RunMetrics> runs = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            runs.add(
                    simulate(
                            config,
                            duration,
                            WARM_UP_TIME,
                            collectDeliveryTimes,
                            new Rng(firstSeed + index)));
        }
        return runs;
    }

    public static List<RunMetrics> runs(
            SwitchingNodeConfig config, int count, long firstSeed, boolean collectDeliveryTimes) {
        return runs(config, count, firstSeed, SIMULATION_TIME, collectDeliveryTimes);
    }
}
