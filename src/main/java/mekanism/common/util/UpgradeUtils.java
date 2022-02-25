package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Upgrade;
import mekanism.api.Upgrade.IUpgradeInfoHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpgradeUtils {

    private UpgradeUtils() {
    }

    public static ItemStack getStack(Upgrade upgrade) {
        return getStack(upgrade, 1);
    }

    public static ItemStack getStack(Upgrade upgrade, int count) {
        return switch (upgrade) {
            case SPEED -> MekanismItems.SPEED_UPGRADE.getItemStack(count);
            case ENERGY -> MekanismItems.ENERGY_UPGRADE.getItemStack(count);
            case FILTER -> MekanismItems.FILTER_UPGRADE.getItemStack(count);
            case MUFFLING -> MekanismItems.MUFFLING_UPGRADE.getItemStack(count);
            case GAS -> MekanismItems.GAS_UPGRADE.getItemStack(count);
            case ANCHOR -> MekanismItems.ANCHOR_UPGRADE.getItemStack(count);
            case STONE_GENERATOR -> MekanismItems.STONE_GENERATOR_UPGRADE.getItemStack(count);
        };
    }

    public static List<Component> getInfo(BlockEntity tile, Upgrade upgrade) {
        List<Component> ret = new ArrayList<>();
        if (tile instanceof IUpgradeTile upgradeTile) {
            if (tile instanceof IUpgradeInfoHandler upgradeInfoHandler) {
                return upgradeInfoHandler.getInfo(upgrade);
            } else {
                ret = getMultScaledInfo(upgradeTile, upgrade);
            }
        }
        return ret;
    }

    public static List<Component> getMultScaledInfo(IUpgradeTile tile, Upgrade upgrade) {
        List<Component> ret = new ArrayList<>();
        if (tile.supportsUpgrades() && upgrade.getMax() > 1) {
            double effect = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), (float) tile.getComponent().getUpgrades(upgrade) / (float) upgrade.getMax());
            ret.add(MekanismLang.UPGRADES_EFFECT.translate(Math.round(effect * 100) / 100F));
        }
        return ret;
    }

    public static List<Component> getExpScaledInfo(IUpgradeTile tile, Upgrade upgrade) {
        List<Component> ret = new ArrayList<>();
        if (tile.supportsUpgrades() && upgrade.getMax() > 1) {
            ret.add(MekanismLang.UPGRADES_EFFECT.translate(Math.pow(2, (float) tile.getComponent().getUpgrades(upgrade))));
        }
        return ret;
    }
}