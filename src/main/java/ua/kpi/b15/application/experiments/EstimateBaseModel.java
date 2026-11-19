package ua.kpi.b15.application.experiments;

import static ua.kpi.b15.application.Format.fixed;

import ua.kpi.b15.application.Experiment;
import ua.kpi.b15.application.ExperimentContext;
import ua.kpi.b15.application.ExperimentPlan;
import ua.kpi.b15.application.Replications.Options;
import ua.kpi.b15.domain.statistics.Descriptive;
import ua.kpi.b15.domain.statistics.Estimate;
import ua.kpi.b15.domain.switching.RunMetrics;
import ua.kpi.b15.domain.switching.SwitchingNodeConfig;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

public final class EstimateBaseModel implements Experiment {
    private static final long FIRST_SEED = 2000;

    private record Response(String name, ToDoubleFunction<RunMetrics> select, int digits) {}

    private static final List<Response> RESPONSES =
            List.of(
                    new Response("Uproc", RunMetrics::processorUtilization, 4),
                    new Response("U1", run -> run.direction(0).lineUtilization(), 4),
                    new Response("U2", run -> run.direction(1).lineUtilization(), 4),
                    new Response("Pref1", run -> run.direction(0).refusalProbability(), 4),
                    new Response("Pref2", run -> run.direction(1).refusalProbability(), 4),
                    new Response("Pref", RunMetrics::refusalProbability, 4),
                    new Response("n1", run -> run.direction(0).meanMessages(), 3),
                    new Response("Lin", RunMetrics::meanInputBuffer, 3),
                    new Response("Lout1", run -> run.direction(0).meanOutputBuffer(), 3),
                    new Response("W1", run -> run.direction(0).meanDeliveryTime(), 3),
                    new Response("W2", run -> run.direction(1).meanDeliveryTime(), 3),
                    new Response("W", RunMetrics::meanDeliveryTime, 3));

    @Override
    public void run(ExperimentContext context) {
        context.output().section("Оцінки вихідних характеристик базової моделі");
        List<RunMetrics> runs =
                context.replications()
                        .run(
                                SwitchingNodeConfig.BASE,
                                ExperimentPlan.RUN_COUNT,
                                Options.defaults()
                                        .collectingDeliveryTimes()
                                        .withFirstSeed(FIRST_SEED));

        List<List<Object>> rows =
                RESPONSES.stream()
                        .map(
                                response -> {
                                    double[] values = Summaries.valuesOf(runs, response.select());
                                    Estimate estimate = Estimate.of(values);
                                    int digits = response.digits();
                                    context.output()
                                            .line(
                                                    response.name()
                                                            + ": "
                                                            + fixed(estimate.mean(), digits)
                                                            + " ± "
                                                            + fixed(estimate.halfWidth(), digits)
                                                            + " (σ = "
                                                            + fixed(estimate.deviation(), digits)
                                                            + ")");
                                    return List.<Object>of(
                                            response.name(),
                                            fixed(estimate.mean(), digits + 1),
                                            fixed(estimate.deviation(), digits + 1),
                                            fixed(estimate.halfWidth(), digits + 1),
                                            fixed(Descriptive.minimum(values), digits + 1),
                                            fixed(Descriptive.maximum(values), digits + 1));
                                })
                        .toList();
        context.store()
                .save(
                        "base_estimates.csv",
                        List.of("response", "mean", "std", "ci", "min", "max"),
                        rows);

        DeliveryTimeAnalysis.analyse(
                "base", DeliveryTimeAnalysis.thinnedDeliveryTimes(runs), context);
        double[] all =
                runs.stream()
                        .flatMapToDouble(run -> DoubleStream.of(run.deliveryTimes()))
                        .toArray();
        Descriptive stats = Descriptive.of(all);
        context.output()
                .line(
                        "Час передачі (усі "
                                + all.length
                                + " повідомлень): середнє "
                                + fixed(stats.mean(), 3)
                                + ", σ = "
                                + fixed(stats.deviation(), 3)
                                + ", min = "
                                + fixed(stats.min(), 3)
                                + ", max = "
                                + fixed(stats.max(), 3));
    }
}
