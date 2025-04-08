package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Upgrade;
import mekanism.api.Upgrade.IUpgradeInfoHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpgradeUtils {

    private UpgradeUtils() {
    }

    public static ItemStack getStack(Upgrade upgrade) {
        return getStack(upgrade, 1);
    }

    public static ItemStack getStack(Upgrade upgrade, int count) {
        return new ItemStack(getItem(upgrade), count);
    }

    public static Holder<Item> getItem(Upgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> MekanismItems.SPEED_UPGRADE;
            case ENERGY -> MekanismItems.ENERGY_UPGRADE;
            case FILTER -> MekanismItems.FILTER_UPGRADE;
            case MUFFLING -> MekanismItems.MUFFLING_UPGRADE;
            case CHEMICAL -> MekanismItems.CHEMICAL_UPGRADE;
            case ANCHOR -> MekanismItems.ANCHOR_UPGRADE;
            case STONE_GENERATOR -> MekanismItems.STONE_GENERATOR_UPGRADE;
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
            double effect = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), tile.getComponent().getUpgrades(upgrade) / (float) upgrade.getMax());
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