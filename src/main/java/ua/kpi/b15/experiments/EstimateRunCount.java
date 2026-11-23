package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;
import static ua.kpi.b15.Format.plain;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.RunMetrics;
import ua.kpi.b15.model.SwitchingNodeConfig;
import ua.kpi.b15.model.SwitchingNodeSimulation;
import ua.kpi.b15.stats.Estimate;

import java.util.List;
import java.util.function.ToDoubleFunction;

public final class EstimateRunCount {
    private static final int PILOT_RUNS = 100;
    private static final long FIRST_SEED = 7000;

    private record Response(String name, ToDoubleFunction<RunMetrics> select, double accuracy) {}

    private static final List<Response> RESPONSES =
            List.of(
                    new Response("Pвідм", RunMetrics::refusalProbability, 0.002),
                    new Response("Uproc", RunMetrics::processorUtilization, 0.002),
                    new Response("U1", run -> run.direction(0).lineUtilization(), 0.002),
                    new Response("W", RunMetrics::meanDeliveryTime, 0.1));

    public static void run(Output out) {
        out.section("Кількість повторних прогонів");
        List<RunMetrics> runs =
                SwitchingNodeSimulation.runs(
                        SwitchingNodeConfig.BASE, PILOT_RUNS, FIRST_SEED, false);
        List<List<Object>> rows =
                RESPONSES.stream()
                        .map(
                                response -> {
                                    Estimate estimate =
                                            Estimate.of(
                                                    Summaries.valuesOf(runs, response.select()));
                                    int count =
                                            Estimate.chebyshevRunCount(
                                                    estimate.deviation(), response.accuracy());
                                    out.line(
                                            response.name()
                                                    + ": середнє "
                                                    + fixed(estimate.mean(), 5)
                                                    + ", σ = "
                                                    + fixed(estimate.deviation(), 5)
                                                    + ", ε = "
                                                    + plain(response.accuracy())
                                                    + ", p = "
                                                    + count);
                                    return List.<Object>of(
                                            response.name(),
                                            fixed(estimate.mean(), 5),
                                            fixed(estimate.deviation(), 5),
                                            plain(response.accuracy()),
                                            count);
                                })
                        .toList();
        out.save("runs_count.csv", List.of("response", "mean", "sigma", "eps", "p"), rows);
    }
}
