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
            itemMap.put(HashedItem.create(itemType), tag.getLong(NBTConstants.AMOUNT));
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
            Object2LongMap<HashedItem> fullItemMap = new Object2LongOpenHashMap<>();
            fullItemMap.putAll(itemMap);
            for (Entry<HashedItem> entry : other.itemMap.object2LongEntrySet()) {
                HashedItem type = entry.getKey();
                fullItemMap.put(type, fullItemMap.getOrDefault(type, 0L) + entry.getLongValue());
            }
            return new QIORecipeData(fullItemMap, itemCount + other.itemCount);
        }
        return null;
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        IQIODriveItem driveItem = (IQIODriveItem) stack.getItem();
        if (itemCount > driveItem.getCountCapacity(stack) || itemMap.size() > driveItem.getTypeCapacity(stack)) {
            //If we have more items stored than the output item supports or more types stored
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