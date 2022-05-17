package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import mekanism.api.NBTConstants;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public interface IQIODriveItem {

    default boolean hasStoredItemMap(ItemStack stack) {
        return ItemDataUtils.hasData(stack, NBTConstants.QIO_ITEM_MAP, Tag.TAG_LIST);
    }

    default void loadItemMap(ItemStack stack, QIODriveData data) {
        if (hasStoredItemMap(stack)) {
            ListTag list = ItemDataUtils.getList(stack, NBTConstants.QIO_ITEM_MAP);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                ItemStack itemType = ItemStack.of(tag.getCompound(NBTConstants.ITEM));
                if (!itemType.isEmpty()) {
                    //Only add the item if the item could be read. If it can't that means the mod adding the item was probably removed
                    //TODO: Eventually we may want to keep the NBT so that if the mod gets added back it exists again
                    long count = tag.getLong(NBTConstants.AMOUNT);
                    data.getItemMap().put(HashedItem.create(itemType), count);
                }
            }
        }
    }

    default void writeItemMap(ItemStack stack, QIODriveData map) {
        ListTag list = new ListTag();
        for (Entry<HashedItem> entry : map.getItemMap().object2LongEntrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.put(NBTConstants.ITEM, entry.getKey().getStack().save(new CompoundTag()));
            tag.putLong(NBTConstants.AMOUNT, entry.getLongValue());
            list.add(tag);
        }
        ItemDataUtils.setListOrRemove(stack, NBTConstants.QIO_ITEM_MAP, list);
    }

    long getCountCapacity(ItemStack stack);

    int getTypeCapacity(ItemStack stack);

    record DriveMetadata(long count, int types) {

        public void write(ItemStack stack) {
            ItemDataUtils.setLongOrRemove(stack, NBTConstants.QIO_META_COUNT, count);
            ItemDataUtils.setIntOrRemove(stack, NBTConstants.QIO_META_TYPES, types);
        }

        public static DriveMetadata load(ItemStack stack) {
            return new DriveMetadata(ItemDataUtils.getLong(stack, NBTConstants.QIO_META_COUNT), ItemDataUtils.getInt(stack, NBTConstants.QIO_META_TYPES));
        }
    }
}
