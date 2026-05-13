package mekanism.api.inventory.qio;

import java.util.function.ObjLongConsumer;
import mekanism.api.IFrequency;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Basic definition of a QIO Frequency for use in exposing pieces of them to the API.
 *
 * @since 10.2.1
 */
public interface IQIOFrequency extends IFrequency {//TODO - 26.1: Update docs

    /**
     * Gets the amount of a given item type that is stored in this QIO Frequency.
     *
     * @param type Type of {@link ItemStack} to look up.
     *
     * @return Amount stored.
     */
    default long getStored(ItemStack type) {
        return type.isEmpty() ? 0 : getStored(ItemResource.of(type));
    }

    long getStored(ItemResource type);

    /**
     * Performs the given action for every item type stored in this QIO Frequency. Each action will be provided with a new {@link ItemStack} with a size of {@code 1}
     * representing the type, and a long representing the amount of that item type that is stored.
     *
     * @param consumer Action to be performed.
     */
    void forAllStored(ObjLongConsumer<ItemStack> consumer);

    /**
     * Performs the given action for every item type stored in this QIO Frequency. Each action will be provided with the stored {@link ItemResource} representing the type,
     * and a long representing the amount of that item type that is stored.
     *
     * @param consumer Action to be performed.
     *
     * @since 10.8.0
     */
    void forAllStoredTypes(ObjLongConsumer<ItemResource> consumer);

    /**
     * Attempts to insert a given item type into this QIO Frequency.
     *
     * @param type   Type of {@link ItemStack} to insert; this stack will not be modified and the count is ignored.
     * @param amount Amount to insert.
     *
     * @return Amount actually inserted.

     * @implNote Negative amounts will lead to nothing being inserted rather than causing the item to be extracted.
     */
    long massInsert(ItemResource type, long amount, TransactionContext transaction);//TODO - 26.1: Do we want this and massExtract to throw for empty item type and negative amounts?

    /**
     * Attempts to extract a given item type from this QIO Frequency.
     *
     * @param type   Type of {@link ItemStack} to extract; this stack will not be modified and the count is ignored.
     * @param amount Amount to extract.
     *
     * @return Amount actually extracted.
     *
     * @implNote Negative amounts will lead to nothing being extracted rather than causing the item to be inserted.
     */
    long massExtract(ItemResource type, long amount, TransactionContext transaction);
}