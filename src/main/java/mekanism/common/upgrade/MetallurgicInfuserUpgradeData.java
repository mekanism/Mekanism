package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ITileComponent;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InfusionInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;

public class MetallurgicInfuserUpgradeData extends MachineUpgradeData {

    public final InfusionStack stored;
    public final InfusionInventorySlot infusionSlot;

    //Metallurgic Infuser Constructor
    public MetallurgicInfuserUpgradeData(boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int operatingTicks, InfusionStack stored,
          InfusionInventorySlot infusionSlot, EnergyInventorySlot energySlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        super(redstone, controlType, energyContainer, operatingTicks, energySlot, inputSlot, outputSlot, components);
        this.stored = stored;
        this.infusionSlot = infusionSlot;
    }

    //Infusing Factory Constructor
    public MetallurgicInfuserUpgradeData(boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress, InfusionStack stored,
          InfusionInventorySlot infusionSlot, EnergyInventorySlot energySlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting,
          List<ITileComponent> components) {
        super(redstone, controlType, energyContainer, progress, energySlot, inputSlots, outputSlots, sorting, components);
        this.stored = stored;
        this.infusionSlot = infusionSlot;
    }
}