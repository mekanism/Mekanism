package mekanism.api.inventory.qio;

import java.util.function.ObjLongConsumer;
import mekanism.api.IFrequency;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/// Basic definition of a QIO Frequency for use in exposing pieces of them to the API.
///
/// @since 10.2.1
public interface IQIOFrequency extends IFrequency {

    /// {@return the amount of a given item type that is stored in this QIO Frequency}
    ///
    /// @param type Item type to look up.
    default long getStored(ItemStack type) {
        return type.isEmpty() ? 0 : getStored(ItemResource.of(type));
    }

    /// {@return the amount of a given item type that is stored in this QIO Frequency}
    ///
    /// @param type Item type to look up.
    ///
    /// @since 10.8.0
    long getStored(ItemResource type);

    /// Performs the given action for every item type stored in this QIO Frequency. Each action will be provided with the stored [ItemResource] representing the type, and
    /// a long representing the amount of that item type that is stored.
    ///
    /// @param consumer Action to be performed.
    ///
    /// @since 10.8.0
    void forAllStoredTypes(ObjLongConsumer<ItemResource> consumer);

    /// Attempts to insert a given item type into this QIO Frequency.
    ///
    /// @param type   Item type to insert. **Must be non-empty.**
    /// @param amount Amount to insert. **Must be non-negative.**
    ///
    /// @return Amount actually inserted.
    ///
    /// @throws IllegalArgumentException If the resource is empty or the amount is negative.
    /// @since 10.8.0
    long massInsert(ItemResource type, long amount, TransactionContext transaction);

    /// Attempts to extract a given item type from this QIO Frequency.
    ///
    /// @param type   Item type to extract. **Must be non-empty.**
    /// @param amount Amount to extract. **Must be non-negative.**
    ///
    /// @return Amount actually extracted.
    ///
    /// @throws IllegalArgumentException If the resource is empty or the amount is negative.
    /// @since 10.8.0
    long massExtract(ItemResource type, long amount, TransactionContext transaction);
}