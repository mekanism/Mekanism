package mekanism.common.content.tank;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class TankCache extends MultiblockCache<SynchronizedTankData> {

    //TODO: REMOVE USAGES OF THIS, and use inventory slots instead
    @Deprecated
    public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    @Nonnull
    private List<IInventorySlot> inventorySlots = Collections.emptyList();

    @Nonnull
    public FluidStack fluid = FluidStack.EMPTY;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void apply(SynchronizedTankData data) {
        data.setInventoryData(inventorySlots);
        data.fluidStored = fluid;
        data.editMode = editMode;
    }

    @Override
    public void sync(SynchronizedTankData data) {
        inventorySlots = data.getInventorySlots();
        fluid = data.fluidStored;
        editMode = data.editMode;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        editMode = EnumUtils.CONTAINER_EDIT_MODES[nbtTags.getInt("editMode")];
        ListNBT tagList = nbtTags.getList("Items", NBT.TAG_COMPOUND);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);

        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < 2) {
                inventory.set(slotID, ItemStack.read(tagCompound));
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
            if (!inventory.get(slotCount).isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putByte("Slot", (byte) slotCount);
                inventory.get(slotCount).write(tagCompound);
                tagList.add(tagCompound);
            }
        }
        nbtTags.put("Items", tagList);
        if (!fluid.isEmpty()) {
            nbtTags.put("cachedFluid", fluid.writeToNBT(new CompoundNBT()));
        }
    }
}