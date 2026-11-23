package ua.kpi.b15.model;

import ua.stetsenkoinna.petriobj.PetriNet;
import ua.stetsenkoinna.petriobj.PetriP;
import ua.stetsenkoinna.petriobj.PetriT;

import java.util.List;
import java.util.stream.IntStream;

public final class SwitchingNodeNet {
    private static final int DIRECTION_COUNT = 2;
    private static final int ACCEPT_PRIORITY = 1;
    private static final int REFUSE_PRIORITY = 0;

    private final SwitchingNodeConfig config;
    private final Rng random;
    private final PetriNetBuilder builder = new PetriNetBuilder();
    private final PetriP processorFree;
    private final List<Direction> directions;
    private final StochasticPetriObject object;

    public SwitchingNodeNet(SwitchingNodeConfig config, Rng random) {
        this.config = config;
        this.random = random;
        this.processorFree = builder.place("ProcFree", 1);
        this.directions =
                IntStream.range(0, DIRECTION_COUNT).mapToObj(this::buildDirection).toList();
        PetriNet net = builder.build("SwitchingNode");
        this.object = new StochasticPetriObject(net, random);
    }

    public PetriP processorFree() {
        return processorFree;
    }

    public List<Direction> directions() {
        return directions;
    }

    public StochasticPetriObject object() {
        return object;
    }

    private Direction buildDirection(int index) {
        int number = index + 1;

        PetriP generator = builder.place("Gen" + number, 1);
        PetriP arrived = builder.place("Arr" + number);
        PetriP permits = builder.place("Lim" + number, config.permitsPerDirection());
        PetriP inputBuffer = builder.place("InBuf" + number);
        PetriP outputBuffer = builder.place("OutBuf" + number);
        PetriP lineFree = builder.place("LineFree" + number, 1);
        PetriP delivered = builder.place("Done" + number);
        PetriP refused = builder.place("Ref" + number);

        PetriT arrival =
                builder.transition(
                        new StochasticTransition(
                                "Arrival" + number,
                                config.arrivalMean(),
                                config.arrivalSpread(),
                                random));
        PetriT accept = builder.transition(new PetriT("Accept" + number, 0.0, ACCEPT_PRIORITY));
        PetriT refuse = builder.transition(new PetriT("Refuse" + number, 0.0, REFUSE_PRIORITY));
        PetriT process =
                builder.transition(
                        new PetriT("Process" + number, config.processingTime(), shareOf(index)));
        PetriT transmit =
                builder.transition(
                        new StochasticTransition(
                                "Transmit" + number,
                                config.transmissionMean(),
                                config.transmissionSpread(),
                                random));

        builder.input(generator, arrival);
        builder.output(arrival, generator);
        builder.output(arrival, arrived);

        builder.input(arrived, accept);
        builder.input(permits, accept);
        builder.output(accept, inputBuffer);

        builder.input(arrived, refuse);
        builder.output(refuse, refused);

        builder.input(inputBuffer, process);
        builder.input(processorFree, process);
        builder.output(process, processorFree);
        builder.output(process, outputBuffer);

        builder.input(outputBuffer, transmit);
        builder.input(lineFree, transmit);
        builder.output(transmit, lineFree);
        builder.output(transmit, permits);
        builder.output(transmit, delivered);

        return new Direction(
                generator,
                arrived,
                permits,
                inputBuffer,
                outputBuffer,
                lineFree,
                delivered,
                refused,
                arrival,
                accept,
                refuse,
                process,
                transmit);
    }

    private double shareOf(int index) {
        return index == 0 ? config.firstDirectionShare() : 1 - config.firstDirectionShare();
    }
}
