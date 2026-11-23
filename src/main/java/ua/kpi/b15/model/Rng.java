package ua.kpi.b15.model;

public final class Rng {
    private int state;

    public Rng(long seed) {
        state = (int) seed;
    }

    public double next() {
        state += 0x6d2b79f5;
        int value = state;
        value = (value ^ (value >>> 15)) * (value | 1);
        value ^= value + (value ^ (value >>> 7)) * (value | 61);
        return Integer.toUnsignedLong(value ^ (value >>> 14)) / 4294967296.0;
    }
}
