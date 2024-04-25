package mekanism.common.upgrade;

import java.util.List;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidTankUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final FluidInventorySlot inputSlot;
    public final OutputInventorySlot outputSlot;
    public final ContainerEditMode editMode;
    public final FluidStack stored;
    public final CompoundTag components;

    public FluidTankUpgradeData(HolderLookup.Provider provider, boolean redstone, FluidInventorySlot inputSlot, OutputInventorySlot outputSlot,
          ContainerEditMode editMode, FluidStack stored, List<ITileComponent> components) {
        this.redstone = redstone;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.editMode = editMode;
        this.stored = stored;
        this.components = new CompoundTag();
        for (ITileComponent component : components) {
            component.write(this.components, provider);
        }
    }
}