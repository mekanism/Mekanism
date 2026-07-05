package mekanism.api.upgrade;

import mekanism.api.MekanismAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;

/// Helper class for interacting with upgrades.
///
/// @see IUpgradeHelper#INSTANCE
/// @since 10.8.0
public interface IUpgradeHelper {

    /// Provides access to Mekanism's implementation of [IUpgradeHelper].
    IUpgradeHelper INSTANCE = MekanismAPI.getService(IUpgradeHelper.class);

    /// Creates an [item stack template][ItemStackTemplate] of size `1` representing the upgrade item for the given upgrade.
    ///
    /// @param upgrades Holder getter to look up the upgrade from.
    /// @param upgrade  Upgrade type.
    default ItemStackTemplate asTemplate(HolderGetter<Upgrade> upgrades, ResourceKey<Upgrade> upgrade) {
        return asTemplate(upgrades, upgrade, 1);
    }

    /// Creates an [item stack template][ItemStackTemplate] of the given size representing the upgrade item for the given upgrade.
    ///
    /// @param upgrades Holder getter to look up the upgrade from.
    /// @param upgrade  Upgrade type.
    /// @param amount   Template size.
    default ItemStackTemplate asTemplate(HolderGetter<Upgrade> upgrades, ResourceKey<Upgrade> upgrade, int amount) {
        return asTemplate(upgrades.getOrThrow(upgrade), amount);
    }

    /// Creates an [item stack template][ItemStackTemplate] of the given size representing the upgrade item for the given upgrade.
    ///
    /// @param upgrade Upgrade type.
    /// @param amount  Template size.
    ItemStackTemplate asTemplate(Holder<Upgrade> upgrade, int amount);

    /// Creates an [item stack][ItemStack] of size `1` representing the upgrade item for the given upgrade.
    ///
    /// @param upgrade Upgrade type.
    default ItemStack asStack(Holder<Upgrade> upgrade) {
        return asStack(upgrade, 1);
    }

    /// Creates an [item stack][ItemStack] of the given size representing the upgrade item for the given upgrade.
    ///
    /// @param upgrade Upgrade type.
    /// @param amount  Stack size.
    ItemStack asStack(Holder<Upgrade> upgrade, int amount);

    /// Creates an [item resource][ItemResource] representing the upgrade item for the given upgrade.
    ///
    /// @param upgrade Upgrade type.
    ItemResource asResource(Holder<Upgrade> upgrade);

    /// {@return the data component type for storing upgrade types on items}
    DataComponentType<Holder<Upgrade>> dataComponent();
}