package mekanism.common.content.matrix;

import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;

public class MatrixCache extends MultiblockCache<SynchronizedMatrixData> {

    public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    @Override
    public void apply(SynchronizedMatrixData data) {
        data.setInventory(inventory);
    }

    @Override
    public void sync(SynchronizedMatrixData data) {
        inventory = data.getInventory();
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        NBTTagList tagList = nbtTags.getTagList("Items", NBT.TAG_COMPOUND);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
        for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompoundTagAt(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < 2) {
                inventory.set(slotID, new ItemStack(tagCompound));
            }
        }
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        NBTTagList tagList = new NBTTagList();
        for (int slotCount = 0; slotCount < 2; slotCount++) {
            if (!inventory.get(slotCount).isEmpty()) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.setByte("Slot", (byte) slotCount);
                inventory.get(slotCount).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        nbtTags.setTag("Items", tagList);
    }
}