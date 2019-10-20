package mekanism.common.content.matrix;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class MatrixCache extends MultiblockCache<SynchronizedMatrixData> {

    //TODO: FIX INVENTORY PERSISTENCE??
    @Nonnull
    private List<IInventorySlot> inventorySlots = SynchronizedMatrixData.createBaseInventorySlots();

    @Override
    public void apply(SynchronizedMatrixData data) {
        data.setInventoryData(inventorySlots);
    }

    @Override
    public void sync(SynchronizedMatrixData data) {
        inventorySlots = data.getInventorySlots();
    }

    @Nonnull
    public List<IInventorySlot> getInventorySlots() {
        return inventorySlots;
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