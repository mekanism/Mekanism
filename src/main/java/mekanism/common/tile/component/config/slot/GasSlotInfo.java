package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.gas.GasTank;

public class GasSlotInfo implements ISlotInfo {

    private final List<GasTank> tanks;

    public GasSlotInfo() {
        tanks = Collections.emptyList();
    }

    public GasSlotInfo(GasTank... tanks) {
        this(Arrays.asList(tanks));
    }

    public GasSlotInfo(List<GasTank> tanks) {
        this.tanks = tanks;
    }

    public boolean hasTank(GasTank tank) {
        //TODO: Does this even work
        return getTanks().contains(tank);
    }

    public List<GasTank> getTanks() {
        return tanks;
    }
}