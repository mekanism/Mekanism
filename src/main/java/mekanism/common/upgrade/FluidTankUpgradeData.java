package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter.PathElement;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidTankUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final FluidInventorySlot inputSlot;
    public final OutputInventorySlot outputSlot;
    public final ContainerEditMode editMode;
    public final LargeResourceStack<FluidResource> stored;
    public final CompoundTag components;

    public FluidTankUpgradeData(Provider provider, boolean redstone, FluidInventorySlot inputSlot, OutputInventorySlot outputSlot,
          ContainerEditMode editMode, LargeResourceStack<FluidResource> stored, List<ITileComponent> components, PathElement problemPath) {
        this.redstone = redstone;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.editMode = editMode;
        this.stored = stored;
        this.components = IUpgradeData.readComponents(provider, components, problemPath);
    }
}