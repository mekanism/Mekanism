package mekanism.common.upgrade;

import java.util.List;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final FluidInventorySlot inputSlot;
    public final OutputInventorySlot outputSlot;
    public final ContainerEditMode editMode;
    public final FluidStack stored;
    public final CompoundNBT components;

    public FluidTankUpgradeData(boolean redstone, FluidInventorySlot inputSlot, OutputInventorySlot outputSlot, ContainerEditMode editMode, FluidStack stored,
          List<ITileComponent> components) {
        this.redstone = redstone;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.editMode = editMode;
        this.stored = stored;
        this.components = new CompoundNBT();
        for (ITileComponent component : components) {
            component.write(this.components);
        }
    }
}