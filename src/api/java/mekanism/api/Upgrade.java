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
import net.minecraftforge.common.util.Constants.NBT;

public enum Upgrade implements IHasTranslationKey {
    SPEED("speed", APILang.UPGRADE_SPEED, APILang.UPGRADE_SPEED_DESCRIPTION, 8, EnumColor.RED),
    ENERGY("energy", APILang.UPGRADE_ENERGY, APILang.UPGRADE_ENERGY_DESCRIPTION, 8, EnumColor.BRIGHT_GREEN),
    FILTER("filter", APILang.UPGRADE_FILTER, APILang.UPGRADE_FILTER_DESCRIPTION, 1, EnumColor.DARK_AQUA),
    GAS("gas", APILang.UPGRADE_GAS, APILang.UPGRADE_GAS_DESCRIPTION, 8, EnumColor.YELLOW),
    MUFFLING("muffling", APILang.UPGRADE_MUFFLING, APILang.UPGRADE_MUFFLING_DESCRIPTION, 4, EnumColor.INDIGO),
    ANCHOR("anchor", APILang.UPGRADE_ANCHOR, APILang.UPGRADE_ANCHOR_DESCRIPTION, 1, EnumColor.DARK_GREEN),
    STONE_GENERATOR("stone_generator", APILang.UPGRADE_STONE_GENERATOR, APILang.UPGRADE_STONE_GENERATOR_DESCRIPTION, 1, EnumColor.ORANGE);

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

    /**
     * Reads and builds a map of upgrades to their amounts from NBT.
     *
     * @param nbtTags Stored upgrades.
     *
     * @return Installed upgrade map.
     */
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

    /**
     * Writes a map of upgrades to their amounts to NBT.
     *
     * @param upgrades Upgrades to store.
     * @param nbtTags  Tag to write to.
     */
    public static void saveMap(Map<Upgrade, Integer> upgrades, CompoundNBT nbtTags) {
        ListNBT list = new ListNBT();
        for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            list.add(entry.getKey().getTag(entry.getValue()));
        }
        nbtTags.put(NBTConstants.UPGRADES, list);
    }

    /**
     * Writes an upgrade with given amount to NBT.
     *
     * @param upgrade Upgrade.
     * @param amount  Amount.
     *
     * @return NBT.
     *
     * @deprecated use {@link #getTag(int)} instead.
     */
    @Deprecated//TODO - 1.18: Remove this
    public static CompoundNBT getTagFor(Upgrade upgrade, int amount) {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt(NBTConstants.TYPE, upgrade.ordinal());
        compound.putInt(NBTConstants.AMOUNT, amount);
        return compound;
    }

    /**
     * Writes this upgrade with given amount to NBT.
     *
     * @param amount Amount.
     *
     * @return NBT.
     */
    public CompoundNBT getTag(int amount) {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt(NBTConstants.TYPE, ordinal());
        compound.putInt(NBTConstants.AMOUNT, amount);
        return compound;
    }

    /**
     * Gets the "raw" name of this upgrade for use in registry names.
     */
    public String getRawName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return langKey.getTranslationKey();
    }

    /**
     * Gets the description for this upgrade.
     */
    public ITextComponent getDescription() {
        return descLangKey.translate();
    }

    /**
     * Gets the max number of upgrades of this type that can be installed.
     */
    public int getMax() {
        return maxStack;
    }

    /**
     * Gets the color to use when rendering various information related to this upgrade.
     */
    public EnumColor getColor() {
        return color;
    }

    /**
     * Gets whether this upgrade has multiple levels
     *
     * @return {@code true} if the upgrade has multiple levels.
     */
    @Deprecated//TODO - 1.18: Remove this
    public boolean canMultiply() {
        return getMax() > 1;
    }

    /**
     * Gets an upgrade by index.
     *
     * @param index Index of the upgrade.
     */
    public static Upgrade byIndexStatic(int index) {
        return MathUtils.getByIndexMod(UPGRADES, index);
    }

    public interface IUpgradeInfoHandler {

        List<ITextComponent> getInfo(Upgrade upgrade);
    }
}