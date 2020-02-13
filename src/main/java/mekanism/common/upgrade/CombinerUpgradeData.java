package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ITileComponent;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;

public class CombinerUpgradeData extends MachineUpgradeData {

    public final InputInventorySlot extraSlot;

    //Combiner Constructor
    public CombinerUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int operatingTicks, EnergyInventorySlot energySlot,
          InputInventorySlot extraSlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        super(redstone, controlType, electricityStored, operatingTicks, energySlot, inputSlot, outputSlot, components);
        this.extraSlot = extraSlot;
    }

    //Combining Factory Constructor
    public CombinerUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int[] progress, EnergyInventorySlot energySlot,
          InputInventorySlot extraSlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting, List<ITileComponent> components) {
        super(redstone, controlType, electricityStored, progress, energySlot, inputSlots, outputSlots, sorting, components);
        this.extraSlot = extraSlot;
    }
}