package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Upgrade;
import mekanism.api.Upgrade.IUpgradeInfoHandler;
import mekanism.common.MekanismItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class UpgradeUtils {

    public static ItemStack getStack(Upgrade upgrade) {
        switch (upgrade) {
            case SPEED:
                return MekanismItem.SPEED_UPGRADE.getItemStack();
            case ENERGY:
                return MekanismItem.ENERGY_UPGRADE.getItemStack();
            case FILTER:
                return MekanismItem.FILTER_UPGRADE.getItemStack();
            case MUFFLING:
                return MekanismItem.MUFFLING_UPGRADE.getItemStack();
            case GAS:
                return MekanismItem.GAS_UPGRADE.getItemStack();
            case ANCHOR:
                return MekanismItem.ANCHOR_UPGRADE.getItemStack();
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
        if (upgrade.canMultiply()) {
            double effect = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), (float) tile.getComponent().getUpgrades(upgrade) / (float) upgrade.getMax());
            ret.add(TextComponentUtil.build(Translation.of("gui.mekanism.upgrades.effect"), ": " + (Math.round(effect * 100) / 100F) + "x"));
        }
        return ret;
    }

    public static List<ITextComponent> getExpScaledInfo(IUpgradeTile tile, Upgrade upgrade) {
        List<ITextComponent> ret = new ArrayList<>();
        if (upgrade.canMultiply()) {
            double effect = Math.pow(2, (float) tile.getComponent().getUpgrades(upgrade));
            ret.add(TextComponentUtil.build(Translation.of("gui.mekanism.upgrades.effect"), ": " + effect + "x"));
        }
        return ret;
    }
}