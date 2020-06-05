package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;

public abstract class ChemicalSlotInfo<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends BaseSlotInfo {

    private final List<TANK> tanks;

    @SafeVarargs
    protected ChemicalSlotInfo(boolean canInput, boolean canOutput, TANK... tanks) {
        this(canInput, canOutput, Arrays.asList(tanks));
    }

    protected ChemicalSlotInfo(boolean canInput, boolean canOutput, List<TANK> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public List<TANK> getTanks() {
        return tanks;
    }

    public static class GasSlotInfo extends ChemicalSlotInfo<Gas, GasStack, IGasTank> {

        public GasSlotInfo(boolean canInput, boolean canOutput, IGasTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public GasSlotInfo(boolean canInput, boolean canOutput, List<IGasTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }

    public static class InfusionSlotInfo extends ChemicalSlotInfo<InfuseType, InfusionStack, IInfusionTank> {

        public InfusionSlotInfo(boolean canInput, boolean canOutput, IInfusionTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public InfusionSlotInfo(boolean canInput, boolean canOutput, List<IInfusionTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }

    public static class PigmentSlotInfo extends ChemicalSlotInfo<Pigment, PigmentStack, IPigmentTank> {

        public PigmentSlotInfo(boolean canInput, boolean canOutput, IPigmentTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public PigmentSlotInfo(boolean canInput, boolean canOutput, List<IPigmentTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }

    public static class SlurrySlotInfo extends ChemicalSlotInfo<Slurry, SlurryStack, ISlurryTank> {

        public SlurrySlotInfo(boolean canInput, boolean canOutput, ISlurryTank... tanks) {
            super(canInput, canOutput, tanks);
        }

        public SlurrySlotInfo(boolean canInput, boolean canOutput, List<ISlurryTank> tanks) {
            super(canInput, canOutput, tanks);
        }
    }
}