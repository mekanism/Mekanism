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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

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
    public static Map<Upgrade, Integer> buildMap(@Nullable CompoundTag nbtTags) {
        Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);//null;
        if (nbtTags != null && nbtTags.contains(NBTConstants.UPGRADES, Tag.TAG_LIST)) {
            ListTag list = nbtTags.getList(NBTConstants.UPGRADES, Tag.TAG_COMPOUND);
            for (int tagCount = 0; tagCount < list.size(); tagCount++) {
                CompoundTag compound = list.getCompound(tagCount);
                Upgrade upgrade = byIndexStatic(compound.getInt(NBTConstants.TYPE));
                //Validate the nbt isn't malformed with a negative or zero amount
                int installed = Math.max(0, Math.min(upgrade.maxStack, compound.getInt(NBTConstants.AMOUNT)));
                if (installed > 0) {
                    /*if (upgrades == null) {
                        upgrades = new EnumMap<>(Upgrade.class);
                    }*/
                    upgrades.put(upgrade, installed);
                }
            }
        }
        //TODO - 1.19: Return empty map if there are no upgrades rather than an enum map (basically just uncomment everything that is commented in this method)
        // We can't do it yet as it technically is a breaking change if anyone is depending on implementation spec;
        // which for example we used to do in TileComponentUpgrade
        //return upgrades == null ? Collections.emptyMap() : upgrades;
        return upgrades;
    }

    /**
     * Writes a map of upgrades to their amounts to NBT.
     *
     * @param upgrades Upgrades to store.
     * @param nbtTags  Tag to write to.
     */
    public static void saveMap(Map<Upgrade, Integer> upgrades, CompoundTag nbtTags) {
        ListTag list = new ListTag();
        for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            list.add(entry.getKey().getTag(entry.getValue()));
        }
        nbtTags.put(NBTConstants.UPGRADES, list);
    }

    /**
     * Writes this upgrade with given amount to NBT.
     *
     * @param amount Amount.
     *
     * @return NBT.
     */
    public CompoundTag getTag(int amount) {
        CompoundTag compound = new CompoundTag();
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
    public Component getDescription() {
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
     * Gets an upgrade by index.
     *
     * @param index Index of the upgrade.
     */
    public static Upgrade byIndexStatic(int index) {
        return MathUtils.getByIndexMod(UPGRADES, index);
    }

    public interface IUpgradeInfoHandler {

        List<Component> getInfo(Upgrade upgrade);
    }
}