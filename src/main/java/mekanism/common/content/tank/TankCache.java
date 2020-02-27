package mekanism.common.content.tank;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.multiblock.InventoryMultiblockCache;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class TankCache extends InventoryMultiblockCache<SynchronizedTankData> {

    @Nonnull
    public FluidStack fluid = FluidStack.EMPTY;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void apply(SynchronizedTankData data) {
        data.setInventoryData(inventorySlots);
        data.fluidTank.setFluid(fluid);
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
        ListNBT tagList = nbtTags.getList("Items", NBT.TAG_COMPOUND);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < 2) {
                inventorySlots.get(slotID).deserializeNBT(tagCompound);
            }
        }
        if (nbtTags.contains("cachedFluid")) {
            fluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("cachedFluid"));
        }
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.putInt("editMode", editMode.ordinal());
        ListNBT tagList = new ListNBT();
        for (int slotCount = 0; slotCount < 2; slotCount++) {
            CompoundNBT tagCompound = inventorySlots.get(slotCount).serializeNBT();
            if (!tagCompound.isEmpty()) {
                tagCompound.putByte("Slot", (byte) slotCount);
                tagList.add(tagCompound);
            }
        }
        nbtTags.put("Items", tagList);
        if (!fluid.isEmpty()) {
            nbtTags.put("cachedFluid", fluid.writeToNBT(new CompoundNBT()));
        }
    }
}