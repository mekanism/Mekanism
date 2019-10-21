package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.gas.GasTank;

public class GasSlotInfo implements ISlotInfo {

    //TODO: Should this be a list of gas handlers?
    private final List<GasTank> tanks;

    public GasSlotInfo(GasTank... tanks) {
        this(Arrays.asList(tanks));
    }

    public GasSlotInfo(List<GasTank> tanks) {
        this.tanks = tanks;
    }

    public boolean hasTank(GasTank tank) {
        //TODO: Does this even work
        return tanks.contains(tank);
    }
}