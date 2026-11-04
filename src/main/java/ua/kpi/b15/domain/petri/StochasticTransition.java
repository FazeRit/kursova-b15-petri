package ua.kpi.b15.domain.petri;

import ua.kpi.b15.domain.ports.RandomSource;
import ua.stetsenkoinna.petriobj.PetriT;

public final class StochasticTransition extends PetriT {
    private static final String UNIFORM = "unif";

    private final RandomSource random;

    public StochasticTransition(String name, double mean, double spread, RandomSource random) {
        super(name, mean);
        if (spread < 0) throw new IllegalArgumentException("spread must not be negative");
        this.random = random;
        if (spread > 0) {
            setDistribution(UNIFORM, mean);
            setParamDeviation(spread);
        }
    }

    @Override
    public double generateTimeServ() {
        return getParameter() - getParamDeviation() + 2 * getParamDeviation() * random.next();
    }
}
