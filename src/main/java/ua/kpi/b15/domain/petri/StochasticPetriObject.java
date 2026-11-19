package ua.kpi.b15.domain.petri;

import ua.kpi.b15.domain.ports.RandomSource;
import ua.stetsenkoinna.petriobj.PetriNet;
import ua.stetsenkoinna.petriobj.PetriObjModel;
import ua.stetsenkoinna.petriobj.PetriSim;
import ua.stetsenkoinna.petriobj.PetriT;

import java.util.ArrayList;
import java.util.List;

public final class StochasticPetriObject extends PetriSim {
    private final RandomSource random;

    public StochasticPetriObject(PetriNet net, RandomSource random) {
        super(net);
        this.random = random;
    }

    public void simulate(double duration, SimulationObserver observer) {
        PetriObjModel model = new PetriObjModel(new ArrayList<>(List.of(this)));
        model.setIsProtokol(false);
        model.setIsStatistics(true);
        observer.observe(this);
        model.setStatisticCollector(observer);
        model.go(duration);
    }

    @Override
    public PetriT doConflikt(ArrayList<PetriT> transitions) {
        int topPriority = transitions.getFirst().getPriority();
        List<PetriT> leaders =
                transitions.stream()
                        .takeWhile(transition -> transition.getPriority() == topPriority)
                        .toList();
        if (leaders.size() == 1) return leaders.getFirst();
        return chooseByProbability(leaders);
    }

    private PetriT chooseByProbability(List<PetriT> leaders) {
        double total = 0;
        for (PetriT transition : leaders) total += transition.getProbability();
        double point = random.next() * total;
        double accumulated = 0;
        for (PetriT transition : leaders) {
            accumulated += transition.getProbability();
            if (point < accumulated) return transition;
        }
        return leaders.getLast();
    }
}
