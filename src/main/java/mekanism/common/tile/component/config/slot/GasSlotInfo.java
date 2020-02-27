package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;

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

    public boolean hasTank(IChemicalTank<Gas, GasStack> tank) {
        //TODO: Does this even work
        return getTanks().contains(tank);
    }

    public List<? extends IChemicalTank<Gas, GasStack>> getTanks() {
        return tanks;
    }
}