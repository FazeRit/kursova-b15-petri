package ua.kpi.b15.domain.ports;

@FunctionalInterface
public interface RandomSourceFactory {
    RandomSource create(long seed);
}
