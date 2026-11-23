package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.RunMetrics;
import ua.kpi.b15.stats.ChiSquareTest;
import ua.kpi.b15.stats.Descriptive;
import ua.kpi.b15.stats.DistributionFits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

final class DeliveryTimeAnalysis {
    private static final int THINNING_STEP = 250;

    private DeliveryTimeAnalysis() {}

    static double[] thinnedDeliveryTimes(List<RunMetrics> runs) {
        return runs.stream()
                .flatMapToDouble(
                        run ->
                                IntStream.range(0, run.deliveryTimes().length)
                                        .filter(index -> index % THINNING_STEP == 0)
                                        .mapToDouble(index -> run.deliveryTimes()[index]))
                .toArray();
    }

    static void analyse(String label, double[] sample, Output out) {
        Descriptive stats = Descriptive.of(sample);
        out.line(
                "["
                        + label
                        + "] вибірка n = "
                        + sample.length
                        + ": середнє "
                        + fixed(stats.mean(), 3)
                        + ", σ = "
                        + fixed(stats.deviation(), 3)
                        + ", min = "
                        + fixed(stats.min(), 3)
                        + ", max = "
                        + fixed(stats.max(), 3));
        out.save(
                "delays_" + label + ".csv",
                List.of("delay"),
                Arrays.stream(sample).mapToObj(value -> List.<Object>of(fixed(value, 3))).toList());

        DistributionFits fits = DistributionFits.byMoments(sample);
        List<List<Object>> rows = new ArrayList<>();
        for (DistributionFits.Law law : fits.laws()) {
            ChiSquareTest.Result test = ChiSquareTest.test(sample, law.cdf(), law.parameters());
            out.line(
                    "   "
                            + law.name()
                            + ": χ² = "
                            + fixed(test.statistic(), 2)
                            + ", df = "
                            + test.degrees()
                            + ", χ²кр = "
                            + fixed(test.critical(), 2)
                            + " - "
                            + (test.accepted() ? "приймається" : "відхиляється"));
            rows.add(
                    List.of(
                            law.name(),
                            fixed(test.statistic(), 3),
                            test.degrees(),
                            fixed(test.critical(), 3),
                            test.accepted()));
        }
        DistributionFits.Weibull weibull = fits.weibull();
        rows.add(
                List.of(
                        "params",
                        "m=" + fixed(fits.mean()),
                        "s=" + fixed(fits.deviation()),
                        "k=" + fixed(fits.gammaShape()),
                        "theta=" + fixed(fits.gammaScale()),
                        "weibull_k=" + fixed(weibull.shape()),
                        "weibull_lambda=" + fixed(weibull.scale()),
                        "weibull_c=" + fixed(weibull.shift())));
        out.save("chi2_" + label + ".csv", List.of("law", "chi2", "df", "crit", "accepted"), rows);
    }
}
