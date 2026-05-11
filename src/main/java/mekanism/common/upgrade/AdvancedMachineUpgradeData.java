package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.util.ProblemReporter.PathElement;

public class AdvancedMachineUpgradeData extends MachineUpgradeData {

    public final IChemicalTank stored;
    public final ChemicalInventorySlot chemicalSlot;
    public final int[] usedSoFar;

    //Advanced Machine Constructor
    public AdvancedMachineUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int operatingTicks, int usedSoFar, IChemicalTank stored,
          ChemicalInventorySlot chemicalSlot, EnergyInventorySlot energySlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components, PathElement problemPath) {
        super(provider, redstone, controlType, energyContainer, operatingTicks, energySlot, inputSlot, outputSlot, components, problemPath);
        this.stored = stored;
        this.chemicalSlot = chemicalSlot;
        this.usedSoFar = new int[]{usedSoFar};
    }

    //Advanced Machine Factory Constructor
    public AdvancedMachineUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress, int[] usedSoFar, IChemicalTank stored,
          ChemicalInventorySlot chemicalSlot, EnergyInventorySlot energySlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting,
          List<ITileComponent> components, PathElement problemPath) {
        super(provider, redstone, controlType, energyContainer, progress, energySlot, inputSlots, outputSlots, sorting, components, problemPath);
        this.stored = stored;
        this.chemicalSlot = chemicalSlot;
        this.usedSoFar = usedSoFar;
    }
}