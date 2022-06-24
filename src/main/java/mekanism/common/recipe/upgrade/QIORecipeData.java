package mekanism.common.recipe.upgrade;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.IQIODriveItem.DriveMetadata;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * QIO Drive merging data helper. Duplicates a fair bit of code from {@link QIODriveData}, but without requiring a {@link QIODriveKey}, and not validating the total size
 * until writing to the output item
 */
@NothingNullByDefault
public class QIORecipeData implements RecipeUpgradeData<QIORecipeData> {

    //Note: We just keep track of the UUID as we know it is unique by type so there is no reason to look up the stacks for merging purposes
    private final Object2LongMap<UUID> itemMap;
    private final long itemCount;

    QIORecipeData(DriveMetadata data, long[] serializedMap) {
        itemCount = data.count();
        itemMap = new Object2LongOpenHashMap<>(data.types());
        for (int i = 0; i < serializedMap.length; i++) {
            itemMap.put(new UUID(serializedMap[i++], serializedMap[i++]), serializedMap[i]);
        }
    }

    private QIORecipeData(Object2LongMap<UUID> itemMap, long itemCount) {
        this.itemMap = itemMap;
        this.itemCount = itemCount;
    }

    @Nullable
    @Override
    public QIORecipeData merge(QIORecipeData other) {
        if (itemCount <= Long.MAX_VALUE - other.itemCount) {
            //Protect against overflow
            Object2LongMap<UUID> smallerMap = other.itemMap;
            Object2LongMap<UUID> largerMap = itemMap;
            if (largerMap.size() < smallerMap.size()) {
                smallerMap = itemMap;
                largerMap = other.itemMap;
            }
            //Add smaller to larger, so we have to iterate fewer elements
            Object2LongMap<UUID> fullItemMap = new Object2LongOpenHashMap<>(largerMap);
            for (Entry<UUID> entry : smallerMap.object2LongEntrySet()) {
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
        int i = 0;
        long[] serializedMap = new long[3 * itemMap.size()];
        for (Entry<UUID> entry : itemMap.object2LongEntrySet()) {
            UUID uuid = entry.getKey();
            serializedMap[i++] = uuid.getMostSignificantBits();
            serializedMap[i++] = uuid.getLeastSignificantBits();
            serializedMap[i++] = entry.getLongValue();
        }
        ItemDataUtils.setLongArrayOrRemove(stack, NBTConstants.QIO_ITEM_MAP, serializedMap);
        DriveMetadata meta = new DriveMetadata(itemCount, itemMap.size());
        meta.write(stack);
        return true;
    }
}