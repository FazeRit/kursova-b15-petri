package ua.kpi.b15.domain.petri;

import ua.stetsenkoinna.petriobj.FiringPhase;
import ua.stetsenkoinna.petriobj.PetriNet;
import ua.stetsenkoinna.petriobj.PetriSim;
import ua.stetsenkoinna.petriobj.PetriT;
import ua.stetsenkoinna.petriobj.SimulationStatisticCollector;

public abstract class SimulationObserver implements SimulationStatisticCollector {
    private PetriSim object;

    public void observe(PetriSim object) {
        this.object = object;
    }

    @Override
    public boolean shouldCollect(double currentTime) {
        return true;
    }

    @Override
    public void onTimeStep(double currentTime, PetriNet net, int petriObjId) {
        double end = Math.min(object.getTimeMin(), object.getSimulationTime());
        onInterval(currentTime, end - currentTime);
    }

    @Override
    public void onFiringPhase(
            double time, FiringPhase phase, PetriT transition, Iterable<PetriSim> objects) {
        if (phase == FiringPhase.AFTER_ACT_IN) onEnter(transition, time);
        if (phase == FiringPhase.AFTER_ACT_OUT) onExit(transition, time);
    }

    @Override
    public void flush(double currentTime) {}

    @Override
    public void onSimulationEnd(double simulationEndTime, Iterable<PetriSim> objects) {}

    @Override
    public void shutdown() {}

    protected void onInterval(double start, double duration) {}

    protected void onEnter(PetriT transition, double time) {}

    protected void onExit(PetriT transition, double time) {}
}
