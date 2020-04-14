package mekanism.api;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import mekanism.api.math.MathUtils;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public enum Upgrade implements IHasTranslationKey {
    SPEED("speed", APILang.UPGRADE_SPEED, APILang.UPGRADE_SPEED_DESCRIPTION, 8, EnumColor.RED),
    ENERGY("energy", APILang.UPGRADE_ENERGY, APILang.UPGRADE_ENERGY_DESCRIPTION, 8, EnumColor.BRIGHT_GREEN),
    FILTER("filter", APILang.UPGRADE_FILTER, APILang.UPGRADE_FILTER_DESCRIPTION, 1, EnumColor.DARK_AQUA),
    GAS("gas", APILang.UPGRADE_GAS, APILang.UPGRADE_GAS_DESCRIPTION, 8, EnumColor.YELLOW),
    MUFFLING("muffling", APILang.UPGRADE_MUFFLING, APILang.UPGRADE_MUFFLING_DESCRIPTION, 4, EnumColor.DARK_GRAY),
    ANCHOR("anchor", APILang.UPGRADE_ANCHOR, APILang.UPGRADE_ANCHOR_DESCRIPTION, 1, EnumColor.DARK_GREEN);

    private static final Upgrade[] UPGRADES = values();

    private final String name;
    private final APILang langKey;
    private final APILang descLangKey;
    private final int maxStack;
    private final EnumColor color;

    Upgrade(String name, APILang langKey, APILang descLangKey, int maxStack, EnumColor color) {
        this.name = name;
        this.langKey = langKey;
        this.descLangKey = descLangKey;
        this.maxStack = maxStack;
        this.color = color;
    }

    public static Map<Upgrade, Integer> buildMap(@Nullable CompoundNBT nbtTags) {
        Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
        if (nbtTags != null && nbtTags.contains(NBTConstants.UPGRADES, NBT.TAG_LIST)) {
            ListNBT list = nbtTags.getList(NBTConstants.UPGRADES, NBT.TAG_COMPOUND);
            for (int tagCount = 0; tagCount < list.size(); tagCount++) {
                CompoundNBT compound = list.getCompound(tagCount);
                Upgrade upgrade = byIndexStatic(compound.getInt(NBTConstants.TYPE));
                upgrades.put(upgrade, Math.min(upgrade.maxStack, compound.getInt(NBTConstants.AMOUNT)));
            }
        }
        return upgrades;
    }

    public static void saveMap(Map<Upgrade, Integer> upgrades, CompoundNBT nbtTags) {
        ListNBT list = new ListNBT();
        for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            list.add(getTagFor(entry.getKey(), entry.getValue()));
        }
        nbtTags.put(NBTConstants.UPGRADES, list);
    }

    public static CompoundNBT getTagFor(Upgrade upgrade, int amount) {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt(NBTConstants.TYPE, upgrade.ordinal());
        compound.putInt(NBTConstants.AMOUNT, amount);
        return compound;
    }

    public String getRawName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return langKey.getTranslationKey();
    }

    public ITextComponent getDescription() {
        return new TranslationTextComponent(descLangKey.getTranslationKey());
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

    public static Upgrade byIndexStatic(int index) {
        return MathUtils.getByIndexMod(UPGRADES, index);
    }

    public interface IUpgradeInfoHandler {

        List<ITextComponent> getInfo(Upgrade upgrade);
    }
}