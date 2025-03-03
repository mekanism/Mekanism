package mekanism.common.recipe.upgrade;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * QIO Drive merging data helper. Duplicates a fair bit of code from {@link QIODriveData}, but without requiring a {@link QIODriveKey}, and not validating the total size
 * until writing to the output item
 */
@NothingNullByDefault
public class QIORecipeData implements RecipeUpgradeData<QIORecipeData> {

    //Note: We just keep track of the UUID as we know it is unique by type so there is no reason to look up the stacks for merging purposes
    private final Object2LongSortedMap<UUID> itemMap;
    private final long itemCount;

    QIORecipeData(DriveMetadata data, DriveContents contents) {
        itemCount = data.count();
        itemMap = contents.namedItemMap();
    }

    private QIORecipeData(Object2LongSortedMap<UUID> itemMap, long itemCount) {
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
            Object2LongSortedMap<UUID> fullItemMap = new Object2LongLinkedOpenHashMap<>(largerMap);
            for (ObjectIterator<Object2LongMap.Entry<UUID>> iterator = Object2LongMaps.fastIterator(smallerMap); iterator.hasNext(); ) {
                Object2LongMap.Entry<UUID> entry = iterator.next();
                fullItemMap.mergeLong(entry.getKey(), entry.getLongValue(), Long::sum);
            }
            return new QIORecipeData(fullItemMap, itemCount + other.itemCount);
        }
        return null;
    }

    @Override
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
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
        stack.set(MekanismDataComponents.DRIVE_METADATA, new DriveMetadata(itemCount, itemMap.size()));
        //Note: We just directly pass the item map to it, as we don't need it anymore so the drive contents can take it over
        stack.set(MekanismDataComponents.DRIVE_CONTENTS, new DriveContents(itemMap));
        return true;
    }
}