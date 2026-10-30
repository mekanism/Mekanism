package mekanism.common.upgrade;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.lookup.monitor.ProgressRecipeCacheLookupMonitor;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter.PathElement;

public class MachineUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final IEnergyContainer energyContainer;
    public final ProgressRecipeCacheLookupMonitor<?>[] cacheLookupMonitors;
    public final boolean sorting;
    public final EnergyInventorySlot energySlot;
    public final List<IInventorySlot> inputSlots;
    public final List<IInventorySlot> outputSlots;
    public final CompoundTag components;

    //Machine Constructor
    public MachineUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, EnergyInventorySlot energySlot,
          ProgressRecipeCacheLookupMonitor<?> cacheLookupMonitor, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components,
          PathElement problemPath) {
        this(provider, redstone, controlType, energyContainer, energySlot, new ProgressRecipeCacheLookupMonitor<?>[]{cacheLookupMonitor}, Collections.singletonList(inputSlot),
              Collections.singletonList(outputSlot), false, components, problemPath);
    }

    //Machine Factory Constructor
    public MachineUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, EnergyInventorySlot energySlot,
          ProgressRecipeCacheLookupMonitor<?>[] cacheLookupMonitors, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting,
          List<ITileComponent> components, PathElement problemPath) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.energyContainer = energyContainer;
        this.cacheLookupMonitors = cacheLookupMonitors;
        this.energySlot = energySlot;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.sorting = sorting;
        this.components = IUpgradeData.readComponents(provider, components, problemPath);
    }
}