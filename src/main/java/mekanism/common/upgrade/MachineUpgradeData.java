package mekanism.common.upgrade;

import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ITileComponent;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class MachineUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final double electricityStored;
    public final int[] progress;
    public final boolean sorting;
    public final EnergyInventorySlot energySlot;
    public final List<IInventorySlot> inputSlots;
    public final List<IInventorySlot> outputSlots;
    public final ItemStack typeInputStack;
    public final ItemStack typeOutputStack;
    public final CompoundNBT components;

    //Machine Constructor
    public MachineUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int operatingTicks, EnergyInventorySlot energySlot,
          InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        this(redstone, controlType, electricityStored, new int[]{operatingTicks}, energySlot, Collections.singletonList(inputSlot), Collections.singletonList(outputSlot),
              false, ItemStack.EMPTY, ItemStack.EMPTY, components);
    }

    //Machine Factory Constructor
    public MachineUpgradeData(boolean redstone, RedstoneControl controlType, double electricityStored, int[] progress, EnergyInventorySlot energySlot,
          List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting, ItemStack typeInputStack, ItemStack typeOutputStack,
          List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.electricityStored = electricityStored;
        this.progress = progress;
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