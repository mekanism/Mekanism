package mekanism.common.tile.component.config.slot;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;

public class ChemicalSlotInfo
      extends BaseSlotInfo {

    private final List<IChemicalTank> tanks;

    public ChemicalSlotInfo(boolean canInput, boolean canOutput, IChemicalTank... tanks) {
        this(canInput, canOutput, List.of(tanks));
    }

    public ChemicalSlotInfo(boolean canInput, boolean canOutput, List<IChemicalTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public List<IChemicalTank> getTanks() {
        return tanks;
    }

    public static class GasSlotInfo extends ChemicalSlotInfo {

        public GasSlotInfo(boolean canInput, boolean canOutput, IChemicalTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public GasSlotInfo(boolean canInput, boolean canOutput, List<IChemicalTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }

    public static class InfusionSlotInfo extends ChemicalSlotInfo {

        public InfusionSlotInfo(boolean canInput, boolean canOutput, IChemicalTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public InfusionSlotInfo(boolean canInput, boolean canOutput, List<IChemicalTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }

    public static class PigmentSlotInfo extends ChemicalSlotInfo {

        public PigmentSlotInfo(boolean canInput, boolean canOutput, IChemicalTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public PigmentSlotInfo(boolean canInput, boolean canOutput, List<IChemicalTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }

    public static class SlurrySlotInfo extends ChemicalSlotInfo {

        public SlurrySlotInfo(boolean canInput, boolean canOutput, IChemicalTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public SlurrySlotInfo(boolean canInput, boolean canOutput, List<IChemicalTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }
}