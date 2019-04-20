package mekanism.common.base;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.Coord4D;
import net.minecraft.util.EnumFacing;

public class EnergyAcceptorTarget {

    private Map<EnumFacing, EnergyAcceptorWrapper> wrappers = new HashMap<>();
    private final Coord4D coord;

    public EnergyAcceptorTarget(Coord4D coord) {
        //We don't use it but might as well have it be accessible?
        this.coord = coord;
    }

    public boolean hasAcceptors() {
        return !wrappers.isEmpty();
    }

    public void addSide(EnumFacing side, EnergyAcceptorWrapper acceptor) {
        wrappers.put(side, acceptor);
        //TODO: If the wrapper is sideless keep track of that somehow?
    }

    public Map<EnumFacing, EnergyAcceptorWrapper> getWrappers() {
        return wrappers;
    }
}