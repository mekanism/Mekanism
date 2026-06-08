package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter.PathElement;

public class FluidTankUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final FluidInventorySlot inputSlot;
    public final OutputInventorySlot outputSlot;
    public final ContainerEditMode editMode;
    public final IFluidTank fluidTank;
    public final CompoundTag components;

    public FluidTankUpgradeData(Provider provider, boolean redstone, FluidInventorySlot inputSlot, OutputInventorySlot outputSlot,
          ContainerEditMode editMode, IFluidTank fluidTank, List<ITileComponent> components, PathElement problemPath) {
        this.redstone = redstone;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.editMode = editMode;
        this.fluidTank = fluidTank;
        this.components = IUpgradeData.readComponents(provider, components, problemPath);
    }
}