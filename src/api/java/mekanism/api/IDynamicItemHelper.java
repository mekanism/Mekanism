package mekanism.api;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;

/// Helper class for interacting with dynamically created items based on a corresponding component.
///
/// @since 10.8.0
public interface IDynamicItemHelper<TYPE> {

    /// Creates an [item stack template][ItemStackTemplate] of size `1` representing the item for the given component.
    ///
    /// @param holderGetter Holder getter to look up the component from.
    /// @param type         Type.
    default ItemStackTemplate asTemplate(HolderGetter<TYPE> holderGetter, ResourceKey<TYPE> type) {
        return asTemplate(holderGetter, type, 1);
    }

    /// Creates an [item stack template][ItemStackTemplate] of the given size representing the item for the given component.
    ///
    /// @param holderGetter Holder getter to look up the component from.
    /// @param type         Type.
    /// @param amount       Template size.
    default ItemStackTemplate asTemplate(HolderGetter<TYPE> holderGetter, ResourceKey<TYPE> type, int amount) {
        return asTemplate(holderGetter.getOrThrow(type), amount);
    }

    /// Creates an [item stack template][ItemStackTemplate] of size `1` representing the item for the given component.
    ///
    /// @param type Type.
    default ItemStackTemplate asTemplate(Holder<TYPE> type) {
        return asTemplate(type, 1);
    }

    /// Creates an [item stack template][ItemStackTemplate] of the given size representing the item for the given component.
    ///
    /// @param type   Type.
    /// @param amount Template size.
    ItemStackTemplate asTemplate(Holder<TYPE> type, int amount);

    /// Creates an [item stack][ItemStack] of size `1` representing the item for the given component.
    ///
    /// @param type Type.
    default ItemStack asStack(Holder<TYPE> type) {
        return asStack(type, 1);
    }

    /// Creates an [item stack][ItemStack] of the given size representing the item for the given component.
    ///
    /// @param type   Type.
    /// @param amount Stack size.
    ItemStack asStack(Holder<TYPE> type, int amount);

    /// Creates an [item resource][ItemResource] representing the item for the given component.
    ///
    /// @param type Type.
    ItemResource asResource(Holder<TYPE> type);

    /// {@return the data component type for storing the component type on items}
    DataComponentType<Holder<TYPE>> dataComponent();
}