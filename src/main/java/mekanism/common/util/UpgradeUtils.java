package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Upgrade;
import mekanism.api.Upgrade.IUpgradeInfoHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class UpgradeUtils {

    private UpgradeUtils() {
    }

    public static ItemStack getStack(Upgrade upgrade) {
        return getStack(upgrade, 1);
    }

    public static ItemStack getStack(Upgrade upgrade, int count) {
        switch (upgrade) {
            case SPEED:
                return MekanismItems.SPEED_UPGRADE.getItemStack(count);
            case ENERGY:
                return MekanismItems.ENERGY_UPGRADE.getItemStack(count);
            case FILTER:
                return MekanismItems.FILTER_UPGRADE.getItemStack(count);
            case MUFFLING:
                return MekanismItems.MUFFLING_UPGRADE.getItemStack(count);
            case GAS:
                return MekanismItems.GAS_UPGRADE.getItemStack(count);
            case ANCHOR:
                return MekanismItems.ANCHOR_UPGRADE.getItemStack(count);
            case STONE_GENERATOR:
                return MekanismItems.STONE_GENERATOR_UPGRADE.getItemStack(count);
        }
        return ItemStack.EMPTY;
    }

    public static List<ITextComponent> getInfo(TileEntity tile, Upgrade upgrade) {
        List<ITextComponent> ret = new ArrayList<>();
        if (tile instanceof IUpgradeTile) {
            if (tile instanceof IUpgradeInfoHandler) {
                return ((IUpgradeInfoHandler) tile).getInfo(upgrade);
            } else {
                ret = getMultScaledInfo((IUpgradeTile) tile, upgrade);
            }
        }
        return ret;
    }

    public static List<ITextComponent> getMultScaledInfo(IUpgradeTile tile, Upgrade upgrade) {
        List<ITextComponent> ret = new ArrayList<>();
        if (tile.supportsUpgrades() && upgrade.getMax() > 1) {
            double effect = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), (float) tile.getComponent().getUpgrades(upgrade) / (float) upgrade.getMax());
            ret.add(MekanismLang.UPGRADES_EFFECT.translate(Math.round(effect * 100) / 100F));
        }
        return ret;
    }

    public static List<ITextComponent> getExpScaledInfo(IUpgradeTile tile, Upgrade upgrade) {
        List<ITextComponent> ret = new ArrayList<>();
        if (tile.supportsUpgrades() && upgrade.getMax() > 1) {
            ret.add(MekanismLang.UPGRADES_EFFECT.translate(Math.pow(2, (float) tile.getComponent().getUpgrades(upgrade))));
        }
        return ret;
    }
}