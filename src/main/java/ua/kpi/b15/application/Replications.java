package ua.kpi.b15.application;

import ua.kpi.b15.domain.ports.RandomSourceFactory;
import ua.kpi.b15.domain.switching.RunMetrics;
import ua.kpi.b15.domain.switching.SwitchingNodeConfig;
import ua.kpi.b15.domain.switching.SwitchingNodeSimulation;
import ua.kpi.b15.domain.switching.SwitchingNodeSimulation.RunSettings;

import java.util.List;
import java.util.stream.IntStream;

public final class Replications {
    public record Options(
            double duration, double warmUp, long firstSeed, boolean collectDeliveryTimes) {
        public static Options defaults() {
            return new Options(
                    ExperimentPlan.SIMULATION_TIME,
                    ExperimentPlan.WARM_UP_TIME,
                    ExperimentPlan.DEFAULT_SEED,
                    false);
        }

        public Options withDuration(double value) {
            return new Options(value, warmUp, firstSeed, collectDeliveryTimes);
        }

        public Options withFirstSeed(long value) {
            return new Options(duration, warmUp, value, collectDeliveryTimes);
        }

        public Options collectingDeliveryTimes() {
            return new Options(duration, warmUp, firstSeed, true);
        }
    }

    private final RandomSourceFactory createRandom;

    public Replications(RandomSourceFactory createRandom) {
        this.createRandom = createRandom;
    }

    public List<RunMetrics> run(SwitchingNodeConfig config, int count, Options options) {
        RunSettings settings =
                new RunSettings(
                        options.duration(), options.warmUp(), options.collectDeliveryTimes());
        return IntStream.range(0, count)
                .mapToObj(
                        index ->
                                SwitchingNodeSimulation.simulate(
                                        config,
                                        settings,
                                        createRandom.create(options.firstSeed() + index)))
                .toList();
    }
}
