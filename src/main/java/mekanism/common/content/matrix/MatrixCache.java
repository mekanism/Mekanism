package mekanism.common.content.matrix;

import java.util.List;
import mekanism.api.DataHandlerUtils;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.NBTConstants;
import mekanism.common.multiblock.InventoryMultiblockCache;
import net.minecraft.nbt.CompoundNBT;
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
        DataHandlerUtils.readSlots(getInventorySlots(null), nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND));
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.put(NBTConstants.ITEMS, DataHandlerUtils.writeSlots(getInventorySlots(null)));
    }
}