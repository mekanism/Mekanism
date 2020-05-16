package mekanism.common.upgrade;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.nbt.CompoundNBT;

public class MachineUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final IEnergyContainer energyContainer;
    public final int[] progress;
    public final boolean sorting;
    public final EnergyInventorySlot energySlot;
    public final List<IInventorySlot> inputSlots;
    public final List<IInventorySlot> outputSlots;
    public final CompoundNBT components;

    //Machine Constructor
    public MachineUpgradeData(boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int operatingTicks, EnergyInventorySlot energySlot,
          InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        this(redstone, controlType, energyContainer, new int[]{operatingTicks}, energySlot, Collections.singletonList(inputSlot), Collections.singletonList(outputSlot),
              false, components);
    }

    //Machine Factory Constructor
    public MachineUpgradeData(boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress, EnergyInventorySlot energySlot,
          List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting, List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.energyContainer = energyContainer;
        this.progress = progress;
        this.energySlot = energySlot;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.sorting = sorting;
        this.components = new CompoundNBT();
        for (ITileComponent component : components) {
            component.write(this.components);
        }
    }
}