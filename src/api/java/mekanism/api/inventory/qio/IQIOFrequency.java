package mekanism.api.inventory.qio;

import java.util.function.ObjLongConsumer;
import mekanism.api.Action;
import mekanism.api.IFrequency;
import mekanism.api.inventory.IHashedItem;
import net.minecraft.world.item.ItemStack;

/**
 * Basic definition of a QIO Frequency for use in exposing pieces of them to the API.
 *
 * @since 10.2.1
 */
public interface IQIOFrequency extends IFrequency {

    /**
     * Gets the amount of a given item type that is stored in this QIO Frequency.
     *
     * @param type Type of {@link ItemStack} to look up.
     *
     * @return Amount stored.
     */
    long getStored(ItemStack type);

    /**
     * Performs the given action for every item type stored in this QIO Frequency. Each action will be provided with a new {@link ItemStack} with a size of {@code 1}
     * representing the type, and a long representing the amount of that item type that is stored.
     *
     * @param consumer Action to be performed.
     */
    void forAllStored(ObjLongConsumer<ItemStack> consumer);

    /**
     * Performs the given action for every item type stored in this QIO Frequency. Each action will be provided with the stored {@link IHashedItem} representing the type,
     * and a long representing the amount of that item type that is stored.
     *
     * @param consumer Action to be performed.
     *
     * @since 10.3.6
     */
    void forAllHashedStored(ObjLongConsumer<IHashedItem> consumer);

    /**
     * Attempts to insert a given item type into this QIO Frequency.
     *
     * @param type   Type of {@link ItemStack} to insert; this stack will not be modified and the count is ignored.
     * @param amount Amount to insert.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Amount actually inserted.
     *
     * @apiNote This method behaves <em>subtly</em> different from other insertion methods Mekanism exposes as when working with number based returns having a pointer to
     * the remainder is not nearly as useful.
     * @implNote Negative amounts will lead to nothing being inserted rather than causing the item to be extracted.
     */
    long massInsert(ItemStack type, long amount, Action action);

    /**
     * Attempts to extract a given item type from this QIO Frequency.
     *
     * @param type   Type of {@link ItemStack} to extract; this stack will not be modified and the count is ignored.
     * @param amount Amount to extract.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Amount actually extracted.
     *
     * @implNote Negative amounts will lead to nothing being extracted rather than causing the item to be inserted.
     */
    long massExtract(ItemStack type, long amount, Action action);
}