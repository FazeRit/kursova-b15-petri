package ua.kpi.b15.experiments;

import static ua.kpi.b15.Format.fixed;

import ua.kpi.b15.Output;
import ua.kpi.b15.model.Direction;
import ua.kpi.b15.model.Rng;
import ua.kpi.b15.model.SimulationObserver;
import ua.kpi.b15.model.SwitchingNodeConfig;
import ua.kpi.b15.model.SwitchingNodeNet;
import ua.stetsenkoinna.petriobj.PetriT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class TraceEvents {
    private static final double TRACE_DURATION = 160;
    private static final long TRACE_SEED = 1;
    private static final List<String> DIRECTION_COLUMNS =
            List.of("Arr", "Lim", "InBuf", "OutBuf", "LineFree", "Done", "Ref");

    private static final class EventProtocol extends SimulationObserver {
        private final SwitchingNodeNet node;
        private final List<List<Object>> rows = new ArrayList<>();

        private EventProtocol(SwitchingNodeNet node) {
            this.node = node;
        }

        @Override
        protected void onEnter(PetriT transition, double time) {
            record("вхід", transition, time);
        }

        @Override
        protected void onExit(PetriT transition, double time) {
            record("вихід", transition, time);
        }

        private void record(String event, PetriT transition, double time) {
            List<Object> row =
                    new ArrayList<>(List.of(fixed(time, 3), event, transition.getName()));
            row.addAll(marking(node.directions().get(0)));
            row.add(node.processorFree().getMark());
            row.addAll(marking(node.directions().get(1)));
            row.add(transition.getBuffer() > 0 ? fixed(transition.getMinTime(), 3) : "∞");
            rows.add(row);
        }

        private static List<Integer> marking(Direction direction) {
            return Stream.of(
                            direction.arrived(),
                            direction.permits(),
                            direction.inputBuffer(),
                            direction.outputBuffer(),
                            direction.lineFree(),
                            direction.delivered(),
                            direction.refused())
                    .map(place -> place.getMark())
                    .toList();
        }
    }

    public static void run(Output out) {
        out.section("Протокол подій");
        SwitchingNodeNet node = new SwitchingNodeNet(SwitchingNodeConfig.BASE, new Rng(TRACE_SEED));
        EventProtocol protocol = new EventProtocol(node);
        node.object().simulate(TRACE_DURATION, protocol);
        out.line("Записано подій: " + protocol.rows.size());
        List<String> header = new ArrayList<>(List.of("t, мс", "подія", "перехід"));
        DIRECTION_COLUMNS.forEach(column -> header.add(column + "1"));
        header.add("ProcFree");
        DIRECTION_COLUMNS.forEach(column -> header.add(column + "2"));
        header.add("tOut");
        out.save("protocol_0_160.csv", header, protocol.rows);
    }
}
