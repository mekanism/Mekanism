package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import mekanism.api.NBTConstants;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public interface IQIODriveItem {

    default boolean hasStoredItemMap(ItemStack stack) {
        return ItemDataUtils.hasData(stack, NBTConstants.QIO_ITEM_MAP, NBT.TAG_LIST);
    }

    default void loadItemMap(ItemStack stack, QIODriveData data) {
        if (hasStoredItemMap(stack)) {
            ListNBT list = ItemDataUtils.getList(stack, NBTConstants.QIO_ITEM_MAP);
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT tag = list.getCompound(i);
                ItemStack itemType = ItemStack.read(tag.getCompound(NBTConstants.ITEM));
                if (!itemType.isEmpty()) {
                    //Only add the item if the item could be read. If it can't that means the mod adding the item was probaly removed
                    //TODO: Eventually we may want to keep the NBT so that if the mod gets added back it exists again
                    long count = tag.getLong(NBTConstants.AMOUNT);
                    data.getItemMap().put(HashedItem.create(itemType), count);
                }
            }
        }
    }

    default void writeItemMap(ItemStack stack, QIODriveData map) {
        ListNBT list = new ListNBT();
        for (Entry<HashedItem> entry : map.getItemMap().object2LongEntrySet()) {
            CompoundNBT tag = new CompoundNBT();
            tag.put(NBTConstants.ITEM, entry.getKey().getStack().write(new CompoundNBT()));
            tag.putLong(NBTConstants.AMOUNT, entry.getLongValue());
            list.add(tag);
        }
        ItemDataUtils.setList(stack, NBTConstants.QIO_ITEM_MAP, list);
    }

    long getCountCapacity(ItemStack stack);

    int getTypeCapacity(ItemStack stack);

    class DriveMetadata {

        private final long count;
        private final int types;

        public DriveMetadata(long count, int types) {
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
