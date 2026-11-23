package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;
import static ua.kpi.b15.Format.plain;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.Rng;
import ua.kpi.b15.model.RunMetrics;
import ua.kpi.b15.model.SwitchingNodeConfig;
import ua.kpi.b15.model.SwitchingNodeSimulation;
import ua.kpi.b15.stats.Descriptive;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class FindTransientPeriod {
    private static final int PILOT_RUNS = 5;
    private static final long FIRST_SEED = 500;

    private record TransientCase(
            String label, SwitchingNodeConfig config, double horizon, double step) {}

    private record Response(String name, ToDoubleFunction<RunMetrics> select, double accuracy) {}

    private record Snapshot(String label, int run, double time, RunMetrics metrics) {}

    private static final List<TransientCase> CASES =
            List.of(
                    new TransientCase("L=3", SwitchingNodeConfig.BASE, 100_000, 1_000),
                    new TransientCase(
                            "L=inf",
                            SwitchingNodeConfig.BASE.withoutFlowControl(),
                            1_000_000,
                            10_000));

    private static final List<Response> RESPONSES =
            List.of(
                    new Response("Pref", RunMetrics::refusalProbability, 0.005),
                    new Response("Uproc", RunMetrics::processorUtilization, 0.005),
                    new Response("W", RunMetrics::meanDeliveryTime, 1.0));

    private static final List<String> HEADER =
            List.of("case", "run", "T", "Pref", "Uproc", "U1", "W", "n1");

    public static void run(Output out) {
        out.section("Тривалість перехідного періоду");
        List<Snapshot> snapshots = new ArrayList<>();
        for (TransientCase item : CASES) snapshots.addAll(observeCase(item));

        out.save(
                "transient.csv",
                HEADER,
                snapshots.stream()
                        .map(
                                snapshot ->
                                        List.<Object>of(
                                                snapshot.label(),
                                                snapshot.run(),
                                                fixed(snapshot.time(), 0),
                                                fixed(snapshot.metrics().refusalProbability(), 5),
                                                fixed(snapshot.metrics().processorUtilization(), 5),
                                                fixed(
                                                        snapshot.metrics()
                                                                .direction(0)
                                                                .lineUtilization(),
                                                        5),
                                                fixed(snapshot.metrics().meanDeliveryTime(), 4),
                                                fixed(
                                                        snapshot.metrics()
                                                                .direction(0)
                                                                .meanMessages(),
                                                        4)))
                        .toList());

        List<Snapshot> limited =
                snapshots.stream()
                        .filter(snapshot -> snapshot.label().equals(CASES.getFirst().label()))
                        .toList();
        for (Response response : RESPONSES) {
            double settled = settlingTime(limited, response.select(), response.accuracy());
            out.line(
                    "Перехідний період за "
                            + response.name()
                            + " (ε = "
                            + plain(response.accuracy())
                            + "): T = "
                            + (Double.isNaN(settled) ? "NaN" : plain(settled))
                            + " мс");
        }
    }

    private static List<Snapshot> observeCase(TransientCase item) {
        List<Snapshot> snapshots = new ArrayList<>();
        for (int run = 0; run < PILOT_RUNS; run++) {
            for (double time = item.step(); time <= item.horizon() + 1e-9; time += item.step()) {
                RunMetrics metrics =
                        SwitchingNodeSimulation.simulate(
                                item.config(), time, 0, false, new Rng(FIRST_SEED + run));
                snapshots.add(new Snapshot(item.label(), run + 1, time, metrics));
            }
        }
        return snapshots;
    }

    private static double settlingTime(
            List<Snapshot> snapshots, ToDoubleFunction<RunMetrics> select, double accuracy) {
        List<Double> times = snapshots.stream().map(Snapshot::time).distinct().sorted().toList();
        for (int index = 0; index < times.size(); index++) {
            boolean settled = true;
            for (double time : times.subList(index, times.size())) {
                double[] values =
                        snapshots.stream()
                                .filter(snapshot -> snapshot.time() == time)
                                .mapToDouble(snapshot -> select.applyAsDouble(snapshot.metrics()))
                                .toArray();
                if (!(Descriptive.maximum(values) - Descriptive.minimum(values) < accuracy)) {
                    settled = false;
                    break;
                }
            }
            if (settled) return times.get(index);
        }
        return Double.NaN;
    }
}
