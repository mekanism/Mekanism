package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.gas.GasTank;

public class GasSlotInfo extends BaseSlotInfo {

    private final List<GasTank> tanks;

    public GasSlotInfo(boolean canInput, boolean canOutput, GasTank... tanks) {
        this(canInput, canOutput, Arrays.asList(tanks));
    }

    public GasSlotInfo(boolean canInput, boolean canOutput, List<GasTank> tanks) {
        super(canInput, canOutput);
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