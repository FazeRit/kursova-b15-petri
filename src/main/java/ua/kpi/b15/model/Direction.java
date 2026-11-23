package ua.kpi.b15.model;

import ua.stetsenkoinna.petriobj.PetriP;
import ua.stetsenkoinna.petriobj.PetriT;

public record Direction(
        PetriP generator,
        PetriP arrived,
        PetriP permits,
        PetriP inputBuffer,
        PetriP outputBuffer,
        PetriP lineFree,
        PetriP delivered,
        PetriP refused,
        PetriT arrival,
        PetriT accept,
        PetriT refuse,
        PetriT process,
        PetriT transmit) {
    public int messages() {
        return inputBuffer.getMark()
                + process.getBuffer()
                + outputBuffer.getMark()
                + transmit.getBuffer();
    }
}
