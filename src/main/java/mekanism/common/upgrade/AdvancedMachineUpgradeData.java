package mekanism.common.upgrade;

import java.util.Collections;
import java.util.List;
import mekanism.api.gas.GasStack;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ITileComponent;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class AdvancedMachineUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final double electricityStored;
    public final int[] progress;
    public final boolean sorting;
    public final GasStack stored;
    public final GasInventorySlot gasSlot;
    public final EnergyInventorySlot energySlot;
    public final List<IInventorySlot> inputSlots;
    public final List<IInventorySlot> outputSlots;
    public final ItemStack typeInputStack;
    public final ItemStack typeOutputStack;
    public final CompoundNBT components;

    //Advanced Machine Constructor
    public AdvancedMachineUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int operatingTicks, GasStack stored,
          GasInventorySlot gasSlot, EnergyInventorySlot energySlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        this(redstone, controlType, electricityStored, new int[]{operatingTicks}, stored, gasSlot, energySlot, Collections.singletonList(inputSlot),
              Collections.singletonList(outputSlot), false, ItemStack.EMPTY, ItemStack.EMPTY, components);
    }

    //Advanced Machine Factory Constructor
    public AdvancedMachineUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int[] progress, GasStack stored,
          GasInventorySlot gasSlot, EnergyInventorySlot energySlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting,
          ItemStack typeInputStack, ItemStack typeOutputStack, List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.electricityStored = electricityStored;
        this.progress = progress;
        this.stored = stored;
        this.gasSlot = gasSlot;
        this.energySlot = energySlot;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.sorting = sorting;
        this.typeInputStack = typeInputStack;
        this.typeOutputStack = typeOutputStack;
        this.components = new CompoundNBT();
        for (ITileComponent component : components) {
            component.write(this.components);
        }
    }
}