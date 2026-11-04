package ua.kpi.b15.domain.petri;

import ua.stetsenkoinna.petriobj.ArcIn;
import ua.stetsenkoinna.petriobj.ArcOut;
import ua.stetsenkoinna.petriobj.ExceptionInvalidTimeDelay;
import ua.stetsenkoinna.petriobj.PetriNet;
import ua.stetsenkoinna.petriobj.PetriP;
import ua.stetsenkoinna.petriobj.PetriT;

import java.util.ArrayList;
import java.util.List;

public final class PetriNetBuilder {
    private record InputArc(PetriP place, PetriT transition, int weight) {}

    private record OutputArc(PetriT transition, PetriP place, int weight) {}

    private final List<PetriP> places = new ArrayList<>();
    private final List<PetriT> transitions = new ArrayList<>();
    private final List<InputArc> inputs = new ArrayList<>();
    private final List<OutputArc> outputs = new ArrayList<>();

    public PetriP place(String name, int marks) {
        if (marks < 0)
            throw new IllegalArgumentException(
                    "Place " + name + " must not start with negative tokens");
        PetriP place = new PetriP(name, marks);
        places.add(place);
        return place;
    }

    public PetriP place(String name) {
        return place(name, 0);
    }

    public <T extends PetriT> T transition(T transition) {
        transitions.add(transition);
        return transition;
    }

    public void input(PetriP place, PetriT transition) {
        inputs.add(new InputArc(place, transition, 1));
    }

    public void output(PetriT transition, PetriP place) {
        outputs.add(new OutputArc(transition, place, 1));
    }

    public PetriNet build(String name) {
        for (int index = 0; index < places.size(); index++) places.get(index).setNumber(index);
        for (int index = 0; index < transitions.size(); index++)
            transitions.get(index).setNumber(index);
        ArcIn[] arcsIn =
                inputs.stream()
                        .map(arc -> new ArcIn(arc.place(), arc.transition(), arc.weight()))
                        .toArray(ArcIn[]::new);
        ArcOut[] arcsOut =
                outputs.stream()
                        .map(arc -> new ArcOut(arc.transition(), arc.place(), arc.weight()))
                        .toArray(ArcOut[]::new);
        PetriNet net =
                new PetriNet(
                        name,
                        places.toArray(PetriP[]::new),
                        transitions.toArray(PetriT[]::new),
                        arcsIn,
                        arcsOut);
        validate(net);
        return net;
    }

    private void validate(PetriNet net) {
        try {
            net.validateStructure();
        } catch (ExceptionInvalidTimeDelay exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
        for (PetriT transition : net.getListT()) {
            if (transition.isEmptyOutputPlacesList()) {
                throw new IllegalStateException(
                        "Transition " + transition.getName() + " has no output places");
            }
        }
        if (places.stream().allMatch(place -> place.getMark() == 0)) {
            throw new IllegalStateException("Initial marking must contain at least one token");
        }
    }
}
