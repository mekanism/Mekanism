package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.gas.GasStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ITileComponent;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;

public class AdvancedMachineUpgradeData extends MachineUpgradeData {

    public final GasStack stored;
    public final GasInventorySlot gasSlot;

    //Advanced Machine Constructor
    public AdvancedMachineUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int operatingTicks, GasStack stored,
          GasInventorySlot gasSlot, EnergyInventorySlot energySlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        super(redstone, controlType, electricityStored, operatingTicks, energySlot, inputSlot, outputSlot, components);
        this.stored = stored;
        this.gasSlot = gasSlot;
    }

    //Advanced Machine Factory Constructor
    public AdvancedMachineUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int[] progress, GasStack stored,
          GasInventorySlot gasSlot, EnergyInventorySlot energySlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting,
          List<ITileComponent> components) {
        super(redstone, controlType, electricityStored, progress, energySlot, inputSlots, outputSlots, sorting, components);
        this.stored = stored;
        this.gasSlot = gasSlot;
    }
}