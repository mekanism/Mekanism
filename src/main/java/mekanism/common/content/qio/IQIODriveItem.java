package mekanism.common.content.qio;

import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public interface IQIODriveItem {

    default void loadItemMap(ItemStack stack, QIODriveData data) {
        if (ItemDataUtils.hasData(stack, NBTConstants.QIO_ITEM_MAP, NBT.TAG_LIST)) {
            ListNBT list = ItemDataUtils.getList(stack, NBTConstants.QIO_ITEM_MAP);
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT tag = list.getCompound(i);
                ItemStack itemType = ItemStack.read(tag.getCompound(NBTConstants.ITEM));
                long count = tag.getLong(NBTConstants.AMOUNT);
                data.getItemMap().put(new HashedItem(itemType), count);
            }
        }
    }

    default void writeItemMap(ItemStack stack, QIODriveData map) {
        ListNBT list = new ListNBT();
        for (Map.Entry<HashedItem, Long> entry : map.getItemMap().entrySet()) {
            CompoundNBT tag = new CompoundNBT();
            tag.put(NBTConstants.ITEM, entry.getKey().getStack().write(new CompoundNBT()));
            tag.putLong(NBTConstants.AMOUNT, entry.getValue());
            list.add(tag);
        }
        ItemDataUtils.setList(stack, NBTConstants.QIO_ITEM_MAP, list);
    }

    long getCountCapacity(ItemStack stack);

    int getTypeCapacity(ItemStack stack);

    class DriveMetadata {

        private long count;
        private int types;

        protected DriveMetadata(long count, int types) {
            this.count = count;
            this.types = types;
        }

        public void write(ItemStack stack) {
            ItemDataUtils.setLong(stack, NBTConstants.QIO_META_COUNT, count);
            ItemDataUtils.setInt(stack, NBTConstants.QIO_META_TYPES, types);
        }

        public static DriveMetadata load(ItemStack stack) {
            return new DriveMetadata(ItemDataUtils.getLong(stack, NBTConstants.QIO_META_COUNT), ItemDataUtils.getInt(stack, NBTConstants.QIO_META_TYPES));
        }

        public long getCount() {
            return count;
        }

        public int getTypes() {
            return types;
        }
    }
}
