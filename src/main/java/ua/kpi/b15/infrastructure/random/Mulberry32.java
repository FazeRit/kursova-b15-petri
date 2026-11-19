package ua.kpi.b15.infrastructure.random;

import ua.kpi.b15.domain.ports.RandomSource;

public final class Mulberry32 implements RandomSource {
    private static final double TWO_POW_32 = 4294967296.0;

    private int state;

    public Mulberry32(long seed) {
        this.state = (int) seed;
    }

    @Override
    public double next() {
        state += 0x6d2b79f5;
        int value = state;
        value = (value ^ (value >>> 15)) * (value | 1);
        value ^= value + (value ^ (value >>> 7)) * (value | 61);
        return Integer.toUnsignedLong(value ^ (value >>> 14)) / TWO_POW_32;
    }
}
