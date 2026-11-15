package ua.kpi.b15.application.experiments;

import static ua.kpi.b15.application.Format.fixed;
import static ua.kpi.b15.application.Format.plain;

import ua.kpi.b15.application.Experiment;
import ua.kpi.b15.application.ExperimentContext;
import ua.kpi.b15.application.Replications.Options;
import ua.kpi.b15.domain.statistics.Estimate;
import ua.kpi.b15.domain.switching.RunMetrics;
import ua.kpi.b15.domain.switching.SwitchingNodeConfig;

import java.util.List;
import java.util.function.ToDoubleFunction;

public final class EstimateRunCount implements Experiment {
    private static final int PILOT_RUNS = 100;
    private static final long FIRST_SEED = 7000;

    private record Response(String name, ToDoubleFunction<RunMetrics> select, double accuracy) {}

    private static final List<Response> RESPONSES =
            List.of(
                    new Response("Pвідм", RunMetrics::refusalProbability, 0.002),
                    new Response("Uproc", RunMetrics::processorUtilization, 0.002),
                    new Response("U1", run -> run.direction(0).lineUtilization(), 0.002),
                    new Response("W", RunMetrics::meanDeliveryTime, 0.1));

    @Override
    public void run(ExperimentContext context) {
        context.output().section("Кількість повторних прогонів");
        List<RunMetrics> runs =
                context.replications()
                        .run(
                                SwitchingNodeConfig.BASE,
                                PILOT_RUNS,
                                Options.defaults().withFirstSeed(FIRST_SEED));
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
                                    context.output()
                                            .line(
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
        context.store()
                .save("runs_count.csv", List.of("response", "mean", "sigma", "eps", "p"), rows);
    }
}
