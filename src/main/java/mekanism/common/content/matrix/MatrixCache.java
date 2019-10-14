package mekanism.common.content.matrix;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;

public class MatrixCache extends MultiblockCache<SynchronizedMatrixData> {

    //TODO: REMOVE USAGES OF THIS, and use inventory slots instead
    @Deprecated
    public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    @Nonnull
    private List<IInventorySlot> inventorySlots = Collections.emptyList();

    @Override
    public void apply(SynchronizedMatrixData data) {
        data.setInventoryData(inventorySlots);
    }

    @Override
    public void sync(SynchronizedMatrixData data) {
        inventorySlots = data.getInventorySlots();
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        ListNBT tagList = nbtTags.getList("Items", NBT.TAG_COMPOUND);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < 2) {
                inventory.set(slotID, ItemStack.read(tagCompound));
            }
        }
    }

    @Override
    public void save(CompoundNBT nbtTags) {
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
    }
}