package ua.kpi.b15.model;

import ua.stetsenkoinna.petriobj.PetriT;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class SwitchingNodeStatistics extends SimulationObserver {
    private static final double MILLISECONDS_PER_SECOND = 1000;

    private final SwitchingNodeNet node;
    private final double warmUp;
    private final boolean collectDeliveryTimes;
    private final List<DirectionStatistics> perDirection;
    private final List<Double> deliveryTimes = new ArrayList<>();
    private double observedTime;
    private double processorBusyTime;
    private double inputBufferArea;

    public SwitchingNodeStatistics(
            SwitchingNodeNet node, double warmUp, boolean collectDeliveryTimes) {
        this.node = node;
        this.warmUp = warmUp;
        this.collectDeliveryTimes = collectDeliveryTimes;
        this.perDirection = node.directions().stream().map(DirectionStatistics::new).toList();
    }

    @Override
    protected void onInterval(double start, double duration) {
        double end = start + duration;
        if (end <= warmUp || duration <= 0) return;
        double observed = end - Math.max(start, warmUp);
        observedTime += observed;
        if (node.processorFree().getMark() == 0) processorBusyTime += observed;
        inputBufferArea += totalInputBuffer() * observed;
        for (DirectionStatistics statistics : perDirection) statistics.observe(observed);
    }

    @Override
    protected void onEnter(PetriT transition, double time) {
        boolean afterWarmUp = time >= warmUp;
        for (DirectionStatistics statistics : perDirection) {
            if (transition == statistics.direction.accept()) {
                if (collectDeliveryTimes) statistics.acceptTimes.add(time);
                if (afterWarmUp) statistics.arrived += 1;
            } else if (transition == statistics.direction.refuse() && afterWarmUp) {
                statistics.arrived += 1;
                statistics.refused += 1;
            }
        }
    }

    @Override
    protected void onExit(PetriT transition, double time) {
        for (DirectionStatistics statistics : perDirection) {
            if (transition != statistics.direction.transmit()) continue;
            if (time >= warmUp) statistics.transmitted += 1;
            if (!collectDeliveryTimes) continue;
            Double acceptTime = statistics.acceptTimes.poll();
            if (acceptTime == null)
                throw new IllegalStateException("Departure without a matching accept");
            if (acceptTime >= warmUp) deliveryTimes.add(time - acceptTime);
        }
    }

    public RunMetrics metrics() {
        double observed = observedTime;
        List<DirectionMetrics> directions =
                perDirection.stream().map(statistics -> statistics.finish(observed)).toList();
        double transmitted = sum(statistics -> statistics.transmitted);
        return new RunMetrics(
                observed,
                processorBusyTime / observed,
                inputBufferArea / observed,
                ratio(sum(statistics -> statistics.refused), sum(statistics -> statistics.arrived)),
                ratio(sum(statistics -> statistics.messageArea), transmitted),
                (transmitted / observed) * MILLISECONDS_PER_SECOND,
                directions,
                deliveryTimes.stream().mapToDouble(Double::doubleValue).toArray());
    }

    private double sum(ToDoubleFunction<DirectionStatistics> select) {
        double total = 0;
        for (DirectionStatistics statistics : perDirection)
            total += select.applyAsDouble(statistics);
        return total;
    }

    private int totalInputBuffer() {
        return node.directions().stream()
                .mapToInt(direction -> direction.inputBuffer().getMark())
                .sum();
    }

    private static double ratio(double part, double whole) {
        return whole == 0 ? 0 : part / whole;
    }

    private static final class DirectionStatistics {
        private final Direction direction;
        private final ArrayDeque<Double> acceptTimes = new ArrayDeque<>();
        private int arrived;
        private int refused;
        private int transmitted;
        private double messageArea;
        private double lineBusyTime;
        private double outputBufferArea;

        private DirectionStatistics(Direction direction) {
            this.direction = direction;
        }

        private void observe(double duration) {
            if (direction.lineFree().getMark() == 0) lineBusyTime += duration;
            messageArea += direction.messages() * duration;
            outputBufferArea += direction.outputBuffer().getMark() * duration;
        }

        private DirectionMetrics finish(double observedTime) {
            return new DirectionMetrics(
                    arrived,
                    refused,
                    transmitted,
                    lineBusyTime / observedTime,
                    ratio(refused, arrived),
                    messageArea / observedTime,
                    outputBufferArea / observedTime,
                    ratio(messageArea, transmitted));
        }
    }
}
