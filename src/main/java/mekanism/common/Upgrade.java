package mekanism.common;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;

//TODO: Think about moving upgrade to API so that other mods can access our upgrades if they want to
public enum Upgrade implements IHasTranslationKey {
    SPEED("speed", 8, EnumColor.RED),
    ENERGY("energy", 8, EnumColor.BRIGHT_GREEN),
    FILTER("filter", 1, EnumColor.DARK_AQUA),
    GAS("gas", 8, EnumColor.YELLOW),
    MUFFLING("muffling", 4, EnumColor.DARK_GRAY),
    ANCHOR("anchor", 1, EnumColor.DARK_GREEN);

    private String name;
    private int maxStack;
    private EnumColor color;

    Upgrade(String s, int max, EnumColor c) {
        name = s;
        maxStack = max;
        color = c;
    }

    public static Map<Upgrade, Integer> buildMap(@Nullable CompoundNBT nbtTags) {
        Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
        if (nbtTags != null) {
            if (nbtTags.contains("upgrades")) {
                ListNBT list = nbtTags.getList("upgrades", NBT.TAG_COMPOUND);
                for (int tagCount = 0; tagCount < list.size(); tagCount++) {
                    CompoundNBT compound = list.getCompound(tagCount);
                    Upgrade upgrade = Upgrade.values()[compound.getInt("type")];
                    upgrades.put(upgrade, compound.getInt("amount"));
                }
            }
        }
        return upgrades;
    }

    public static void saveMap(Map<Upgrade, Integer> upgrades, CompoundNBT nbtTags) {
        ListNBT list = new ListNBT();
        for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            list.add(getTagFor(entry.getKey(), entry.getValue()));
        }
        nbtTags.put("upgrades", list);
    }

    public static CompoundNBT getTagFor(Upgrade upgrade, int amount) {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("type", upgrade.ordinal());
        compound.putInt("amount", amount);
        return compound;
    }

    public String getRawName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return "mekanism.upgrade." + name + ".name";
    }

    public String getDescription() {
        return "mekanism.upgrade." + name + ".desc";
    }

    public int getMax() {
        return maxStack;
    }

    public EnumColor getColor() {
        return color;
    }

    public boolean canMultiply() {
        return getMax() > 1;
    }

    public ItemStack getStack() {
        switch (this) {
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

    public List<ITextComponent> getInfo(TileEntity tile) {
        List<ITextComponent> ret = new ArrayList<>();
        if (tile instanceof IUpgradeTile) {
            if (tile instanceof IUpgradeInfoHandler) {
                return ((IUpgradeInfoHandler) tile).getInfo(this);
            } else {
                ret = getMultScaledInfo((IUpgradeTile) tile);
            }
        }
        return ret;
    }

    public List<ITextComponent> getMultScaledInfo(IUpgradeTile tile) {
        List<ITextComponent> ret = new ArrayList<>();
        if (canMultiply()) {
            double effect = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), (float) tile.getComponent().getUpgrades(this) / (float) getMax());
            ret.add(TextComponentUtil.build(Translation.of("gui.upgrades.effect"), ": " + (Math.round(effect * 100) / 100F) + "x"));
        }
        return ret;
    }

    public List<ITextComponent> getExpScaledInfo(IUpgradeTile tile) {
        List<ITextComponent> ret = new ArrayList<>();
        if (canMultiply()) {
            double effect = Math.pow(2, (float) tile.getComponent().getUpgrades(this));
            ret.add(TextComponentUtil.build(Translation.of("gui.upgrades.effect"), ": " + effect + "x"));
        }
        return ret;
    }

    public interface IUpgradeInfoHandler {

        List<ITextComponent> getInfo(Upgrade upgrade);
    }
}