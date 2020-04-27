package mekanism.common.content.qio;

import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.NBTConstants;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public interface IQIODriveItem {

    default QIODriveMap readItemMap(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.QIO_ITEM_MAP, NBT.TAG_LIST)) {
            QIODriveMap map = new QIODriveMap();
            ListNBT list = ItemDataUtils.getList(stack, NBTConstants.QIO_ITEM_MAP);
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT tag = list.getCompound(i);
                ItemStack itemType = ItemStack.read(tag.getCompound(NBTConstants.ITEM));
                int count = tag.getInt(NBTConstants.AMOUNT);
                map.itemMap.put(new HashedItem(itemType), count);
            }
            return map;
        }
        return null;
    }

    default void writeItemMap(ItemStack stack, QIODriveMap map) {
        ListNBT list = new ListNBT();
        for (Map.Entry<HashedItem, Integer> entry : map.itemMap.entrySet()) {
            CompoundNBT tag = new CompoundNBT();
            tag.put(NBTConstants.ITEM, entry.getKey().getStack().write(new CompoundNBT()));
            tag.putInt(NBTConstants.AMOUNT, entry.getValue());
            list.add(tag);
        }
        ItemDataUtils.setList(stack, NBTConstants.QIO_ITEM_MAP, list);
    }

    public static class QIODriveMap {

        private Map<HashedItem, Integer> itemMap = new Object2ObjectOpenHashMap<>();
    }
}
