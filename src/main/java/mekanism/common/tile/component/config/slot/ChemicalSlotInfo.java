package mekanism.common.tile.component.config.slot;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;

public class ChemicalSlotInfo extends BaseSlotInfo {

    private final List<IChemicalTank> tanks;

    public ChemicalSlotInfo(boolean canInput, boolean canOutput, IChemicalTank... tanks) {
        this(canInput, canOutput, List.of(tanks));
    }

    public ChemicalSlotInfo(boolean canInput, boolean canOutput, List<IChemicalTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    @Override
    public boolean isEmpty() {
        for (IChemicalTank tank : getTanks()) {
            if (!tank.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public List<IChemicalTank> getTanks() {
        return tanks;
    }
}