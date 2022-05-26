package mekanism.common.recipe.upgrade;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.IQIODriveItem.DriveMetadata;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/**
 * QIO Drive merging data helper. Duplicates a fair bit of code from {@link QIODriveData}, but without requiring a {@link QIODriveKey}, and not validating the total size
 * until writing to the output item
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class QIORecipeData implements RecipeUpgradeData<QIORecipeData> {

    private final Object2LongMap<HashedItem> itemMap;
    private final long itemCount;

    QIORecipeData(DriveMetadata data, ListTag nbtItemMap) {
        itemCount = data.count();
        itemMap = new Object2LongOpenHashMap<>(data.types());
        for (int i = 0; i < nbtItemMap.size(); i++) {
            CompoundTag tag = nbtItemMap.getCompound(i);
            ItemStack itemType = ItemStack.of(tag.getCompound(NBTConstants.ITEM));
            //Note: We can use raw as we just created the stack, so we know it isn't referenced anywhere else
            itemMap.put(HashedItem.raw(itemType), tag.getLong(NBTConstants.AMOUNT));
        }
    }

    private QIORecipeData(Object2LongMap<HashedItem> itemMap, long itemCount) {
        this.itemMap = itemMap;
        this.itemCount = itemCount;
    }

    @Nullable
    @Override
    public QIORecipeData merge(QIORecipeData other) {
        if (itemCount <= Long.MAX_VALUE - other.itemCount) {
            //Protect against overflow
            Object2LongMap<HashedItem> smallerMap = other.itemMap;
            Object2LongMap<HashedItem> largerMap = itemMap;
            if (largerMap.size() < smallerMap.size()) {
                smallerMap = itemMap;
                largerMap = other.itemMap;
            }
            //Add smaller to larger, so we have to iterate fewer elements
            Object2LongMap<HashedItem> fullItemMap = new Object2LongOpenHashMap<>(largerMap);
            for (Entry<HashedItem> entry : smallerMap.object2LongEntrySet()) {
                fullItemMap.mergeLong(entry.getKey(), entry.getLongValue(), Long::sum);
            }
            return new QIORecipeData(fullItemMap, itemCount + other.itemCount);
        }
        return null;
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (itemMap.isEmpty()) {
            //If we have nothing present then it is a success, but if we have data that says we should
            // have items, but we don't then fail
            return itemCount == 0;
        }
        IQIODriveItem driveItem = (IQIODriveItem) stack.getItem();
        if (itemCount == 0 || itemCount > driveItem.getCountCapacity(stack) || itemMap.size() > driveItem.getTypeCapacity(stack)) {
            //If we have items stored but no types, have more items stored than the output item supports, or have more types stored
            // then return that we are not able to actually apply them to the stack
            return false;
        }
        ListTag list = new ListTag();
        for (Entry<HashedItem> entry : itemMap.object2LongEntrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.put(NBTConstants.ITEM, entry.getKey().getStack().save(new CompoundTag()));
            tag.putLong(NBTConstants.AMOUNT, entry.getLongValue());
            list.add(tag);
        }
        ItemDataUtils.setListOrRemove(stack, NBTConstants.QIO_ITEM_MAP, list);
        DriveMetadata meta = new DriveMetadata(itemCount, itemMap.size());
        meta.write(stack);
        return true;
    }
}