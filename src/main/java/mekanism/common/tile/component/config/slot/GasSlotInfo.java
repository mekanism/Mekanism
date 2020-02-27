package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;

public class GasSlotInfo extends BaseSlotInfo {

    private final List<? extends IChemicalTank<Gas, GasStack>> tanks;

    @SafeVarargs
    public GasSlotInfo(boolean canInput, boolean canOutput, IChemicalTank<Gas, GasStack>... tanks) {
        this(canInput, canOutput, Arrays.asList(tanks));
    }

    public GasSlotInfo(boolean canInput, boolean canOutput, List<? extends IChemicalTank<Gas, GasStack>> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public boolean hasTank(BasicGasTank tank) {
        //TODO: Does this even work
        return getTanks().contains(tank);
    }

    public List<? extends IChemicalTank<Gas, GasStack>> getTanks() {
        return tanks;
    }
}