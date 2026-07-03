package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.component.UpgradeType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.interfaces.ITileUpgradable;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class UpgradeUtils {

    private UpgradeUtils() {
    }

    public static ItemStackTemplate getTemplate(HolderGetter<Upgrade> upgrades, ResourceKey<Upgrade> upgrade, int amount) {
        return getTemplate(upgrades.getOrThrow(upgrade), amount);
    }

    public static ItemStackTemplate getTemplate(Holder<Upgrade> upgrade, int amount) {
        return new ItemStackTemplate(MekanismItems.UPGRADE, amount, DataComponentPatch.builder()
              .set(MekanismDataComponents.UPGRADE_TYPE.get(), new UpgradeType(upgrade))
              .build()
        );
    }

    public static ItemStack getStack(Holder<Upgrade> upgrade) {
        ItemStack stack = MekanismItems.UPGRADE.asStack();
        stack.set(MekanismDataComponents.UPGRADE_TYPE, new UpgradeType(upgrade));
        return stack;
    }

    public static ItemResource getResource(HolderGetter.Provider registries, ResourceKey<Upgrade> upgrade) {
        return getResource(registries.getOrThrow(upgrade));
    }

    public static ItemResource getResource(Holder<Upgrade> upgrade) {
        return MekanismItems.UPGRADE.asResource().with(MekanismDataComponents.UPGRADE_TYPE, new UpgradeType(upgrade));
    }

    public static List<Component> getInfo(BlockEntity tile, Holder<Upgrade> upgrade) {
        List<Component> ret = new ArrayList<>();
        if (tile instanceof IUpgradeTile upgradeTile) {
            if (tile instanceof ITileUpgradable upgradable) {
                return upgradable.getInfo(upgrade);
            } else {
                ret = getMultScaledInfo(upgradeTile, upgrade);
            }
        }
        return ret;
    }

    public static List<Component> getMultScaledInfo(IUpgradeTile tile, Holder<Upgrade> upgrade) {
        List<Component> ret = new ArrayList<>();
        if (tile.supportsUpgrades() && upgrade.value().supportsMultiple()) {
            double effect = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), tile.getUpgrades(upgrade) / (float) upgrade.value().max());
            ret.add(MekanismLang.UPGRADES_EFFECT.translate(Math.round(effect * 100) / 100F));
        }
        return ret;
    }

    public static List<Component> getExpScaledInfo(IUpgradeTile tile, Holder<Upgrade> upgrade) {
        List<Component> ret = new ArrayList<>();
        if (tile.supportsUpgrades() && upgrade.value().supportsMultiple()) {
            ret.add(MekanismLang.UPGRADES_EFFECT.translate(Math.pow(2, (float) tile.getUpgrades(upgrade))));
        }
        return ret;
    }
}