package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.chemical.gas.IGasTank;

public class GasSlotInfo extends BaseSlotInfo {

    private final List<IGasTank> tanks;

    public GasSlotInfo(boolean canInput, boolean canOutput, IGasTank... tanks) {
        this(canInput, canOutput, Arrays.asList(tanks));
    }

    public GasSlotInfo(boolean canInput, boolean canOutput, List<IGasTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public List<IGasTank> getTanks() {
        return tanks;
    }
}