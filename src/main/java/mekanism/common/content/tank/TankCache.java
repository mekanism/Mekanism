package mekanism.common.content.tank;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.DataHandlerUtils;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.multiblock.InventoryMultiblockCache;
import mekanism.common.base.ContainerEditMode;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class TankCache extends InventoryMultiblockCache<SynchronizedTankData> {

    @Nonnull
    public FluidStack fluid = FluidStack.EMPTY;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void apply(SynchronizedTankData data) {
        data.setInventoryData(inventorySlots);
        data.fluidTank.setStack(fluid);
        data.editMode = editMode;
    }

    @Override
    public void sync(SynchronizedTankData data) {
        List<IInventorySlot> toCopy = data.getInventorySlots(null);
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < inventorySlots.size()) {
                //Just directly set it as we don't have any restrictions on our slots here
                inventorySlots.get(i).setStack(toCopy.get(i).getStack());
            }
        }
        fluid = data.fluidTank.getFluid();
        editMode = data.editMode;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        editMode = ContainerEditMode.byIndexStatic(nbtTags.getInt("editMode"));
        DataHandlerUtils.readSlots(getInventorySlots(null), nbtTags.getList("Items", NBT.TAG_COMPOUND));
        if (nbtTags.contains("cachedFluid")) {
            fluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("cachedFluid"));
        }
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.putInt("editMode", editMode.ordinal());
        nbtTags.put("Items", DataHandlerUtils.writeSlots(getInventorySlots(null)));
        if (!fluid.isEmpty()) {
            nbtTags.put("cachedFluid", fluid.writeToNBT(new CompoundNBT()));
        }
    }
}