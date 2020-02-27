package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.gas.BasicGasTank;

public class GasSlotInfo extends BaseSlotInfo {

    private final List<BasicGasTank> tanks;

    public GasSlotInfo(boolean canInput, boolean canOutput, BasicGasTank... tanks) {
        this(canInput, canOutput, Arrays.asList(tanks));
    }

    public GasSlotInfo(boolean canInput, boolean canOutput, List<BasicGasTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public boolean hasTank(BasicGasTank tank) {
        //TODO: Does this even work
        return getTanks().contains(tank);
    }

    public List<BasicGasTank> getTanks() {
        return tanks;
    }
}