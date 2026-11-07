package ua.kpi.b15.domain.switching;

import ua.kpi.b15.domain.petri.SimulationObserver;
import ua.kpi.b15.domain.ports.RandomSource;

import java.util.function.Function;

public final class SwitchingNodeSimulation {
    public record RunSettings(double duration, double warmUp, boolean collectDeliveryTimes) {}

    private SwitchingNodeSimulation() {}

    public static RunMetrics simulate(
            SwitchingNodeConfig config, RunSettings settings, RandomSource random) {
        SwitchingNodeNet node = new SwitchingNodeNet(config, random);
        SwitchingNodeStatistics statistics =
                new SwitchingNodeStatistics(
                        node, settings.warmUp(), settings.collectDeliveryTimes());
        node.object().simulate(settings.duration(), statistics);
        return statistics.metrics();
    }

    public static <T extends SimulationObserver> T trace(
            SwitchingNodeConfig config,
            double duration,
            RandomSource random,
            Function<SwitchingNodeNet, T> createObserver) {
        SwitchingNodeNet node = new SwitchingNodeNet(config, random);
        T observer = createObserver.apply(node);
        node.object().simulate(duration, observer);
        return observer;
    }
}
