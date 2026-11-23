package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.Rng;
import ua.kpi.b15.model.StochasticTransition;
import ua.kpi.b15.stats.ChiSquareTest;
import ua.kpi.b15.stats.Descriptive;

import java.util.ArrayList;
import java.util.List;

public final class CheckRandomGenerators {
    private static final int SAMPLE_SIZE = 10_000;
    private static final int BINS = 20;

    private record Generator(
            String name, String transition, double mean, double spread, long seed) {}

    private static final List<Generator> GENERATORS =
            List.of(
                    new Generator("Надходження unif(15, 7)", "Arrival", 15, 7, 12345),
                    new Generator("Передача unif(15, 5)", "Transmit", 15, 5, 54321));

    private static final List<String> HEADER =
            List.of(
                    "generator",
                    "mean",
                    "mean_th",
                    "std",
                    "std_th",
                    "min",
                    "max",
                    "chi2",
                    "df",
                    "chi2crit",
                    "accepted");

    public static void run(Output out) {
        out.section("Перевірка генераторів випадкових величин");
        List<List<Object>> rows = new ArrayList<>();
        for (Generator generator : GENERATORS) {
            double mean = generator.mean();
            double spread = generator.spread();
            StochasticTransition transition =
                    new StochasticTransition(
                            generator.transition(), mean, spread, new Rng(generator.seed()));
            double[] sample = new double[SAMPLE_SIZE];
            for (int index = 0; index < SAMPLE_SIZE; index++)
                sample[index] = transition.getTimeServ();
            double low = mean - spread;
            double high = mean + spread;
            ChiSquareTest.Result test =
                    ChiSquareTest.test(
                            sample,
                            x -> Math.min(1, Math.max(0, (x - low) / (2 * spread))),
                            0,
                            BINS,
                            low,
                            high);
            Descriptive stats = Descriptive.of(sample);
            double theoreticalDeviation = (2 * spread) / Math.sqrt(12);
            out.line(
                    generator.name()
                            + ": середнє "
                            + fixed(stats.mean())
                            + " ("
                            + fixed(mean)
                            + "), СКВ "
                            + fixed(stats.deviation())
                            + " ("
                            + fixed(theoreticalDeviation)
                            + "), χ² = "
                            + fixed(test.statistic(), 3)
                            + ", χ²кр = "
                            + fixed(test.critical(), 3)
                            + ", гіпотеза "
                            + (test.accepted() ? "приймається" : "відхиляється"));
            rows.add(
                    List.of(
                            generator.name(),
                            fixed(stats.mean()),
                            fixed(mean),
                            fixed(stats.deviation()),
                            fixed(theoreticalDeviation),
                            fixed(stats.min()),
                            fixed(stats.max()),
                            fixed(test.statistic(), 3),
                            test.degrees(),
                            fixed(test.critical(), 3),
                            test.accepted()));
        }
        out.save("rng_chi2.csv", HEADER, rows);
    }
}
