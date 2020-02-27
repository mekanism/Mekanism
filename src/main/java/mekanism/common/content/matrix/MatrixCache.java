package mekanism.common.content.matrix;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.multiblock.InventoryMultiblockCache;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class MatrixCache extends InventoryMultiblockCache<SynchronizedMatrixData> {

    @Override
    public void apply(SynchronizedMatrixData data) {
        data.setInventoryData(inventorySlots);
    }

    @Override
    public void sync(SynchronizedMatrixData data) {
        List<IInventorySlot> toCopy = data.getInventorySlots(null);
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < inventorySlots.size()) {
                //Just directly set it as we don't have any restrictions on our slots here
                inventorySlots.get(i).setStack(toCopy.get(i).getStack());
            }
        }
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        ListNBT tagList = nbtTags.getList("Items", NBT.TAG_COMPOUND);
        for (int tagCount = 0; tagCount < tagList.size(); tagCount++) {
            CompoundNBT tagCompound = tagList.getCompound(tagCount);
            byte slotID = tagCompound.getByte("Slot");
            if (slotID >= 0 && slotID < 2) {
                inventorySlots.get(slotID).deserializeNBT(tagCompound);
            }
        }
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        ListNBT tagList = new ListNBT();
        for (int slotCount = 0; slotCount < 2; slotCount++) {
            CompoundNBT tagCompound = inventorySlots.get(slotCount).serializeNBT();
            if (!tagCompound.isEmpty()) {
                tagCompound.putByte("Slot", (byte) slotCount);
                tagList.add(tagCompound);
            }
        }
        nbtTags.put("Items", tagList);
    }
}