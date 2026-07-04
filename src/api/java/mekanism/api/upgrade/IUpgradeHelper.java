package mekanism.api.upgrade;

import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModuleHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;

/// Helper class for interacting with upgrades.
///
/// @see IModuleHelper#INSTANCE
/// @since 10.8.0
public interface IUpgradeHelper {//TODO - 26.2: Docs

    /// Provides access to Mekanism's implementation of [IUpgradeHelper].
    IUpgradeHelper INSTANCE = MekanismAPI.getService(IUpgradeHelper.class);

    default ItemStackTemplate asTemplate(HolderGetter<Upgrade> upgrades, ResourceKey<Upgrade> upgrade) {
        return asTemplate(upgrades, upgrade, 1);
    }

    default ItemStackTemplate asTemplate(HolderGetter<Upgrade> upgrades, ResourceKey<Upgrade> upgrade, int amount) {
        return asTemplate(upgrades.getOrThrow(upgrade), amount);
    }

    ItemStackTemplate asTemplate(Holder<Upgrade> upgrade, int amount);

    default ItemStack asStack(Holder<Upgrade> upgrade) {
        return asStack(upgrade, 1);
    }

    ItemStack asStack(Holder<Upgrade> upgrade, int amount);

    ItemResource asResource(Holder<Upgrade> upgrade);

    DataComponentType<Holder<Upgrade>> dataComponent();
}