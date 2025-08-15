package mekanism.api;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum Upgrade implements IHasEnumNameTranslationKey, StringRepresentable {
    SPEED("speed", APILang.UPGRADE_SPEED, APILang.UPGRADE_SPEED_DESCRIPTION, 8, EnumColor.RED),
    ENERGY("energy", APILang.UPGRADE_ENERGY, APILang.UPGRADE_ENERGY_DESCRIPTION, 8, EnumColor.BRIGHT_GREEN),
    FILTER("filter", APILang.UPGRADE_FILTER, APILang.UPGRADE_FILTER_DESCRIPTION, 1, EnumColor.DARK_AQUA),
    CHEMICAL("chemical", APILang.UPGRADE_CHEMICAL, APILang.UPGRADE_CHEMICAL_DESCRIPTION, 8, EnumColor.YELLOW),
    MUFFLING("muffling", APILang.UPGRADE_MUFFLING, APILang.UPGRADE_MUFFLING_DESCRIPTION, 1, EnumColor.INDIGO),
    ANCHOR("anchor", APILang.UPGRADE_ANCHOR, APILang.UPGRADE_ANCHOR_DESCRIPTION, 1, EnumColor.DARK_GREEN),
    STONE_GENERATOR("stone_generator", APILang.UPGRADE_STONE_GENERATOR, APILang.UPGRADE_STONE_GENERATOR_DESCRIPTION, 1, EnumColor.ORANGE);

    /**
     * Codec for serializing upgrades based on their name.
     *
     * @since 10.6.0
     */
    public static final Codec<Upgrade> CODEC;

    //TODO - 1.22 remove backcompat and inline back to StringRepresentable.fromEnum
    static {
        Upgrade[] values = values();
        Function<String, Upgrade> nameLookup = StringRepresentable.createNameLookup(values, Function.identity());
        Function<String, Upgrade> remapper = it -> "gas".equals(it) ? CHEMICAL : nameLookup.apply(it);
        CODEC = new EnumCodec<>(values, remapper);
    }

    /**
     * Gets an upgrade by index, wrapping for out of bounds indices.
     *
     * @since 10.6.0
     */
    public static final IntFunction<Upgrade> BY_ID = ByIdMap.continuous(Upgrade::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    /**
     * Stream codec for syncing upgrades by index.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<ByteBuf, Upgrade> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Upgrade::ordinal);

    private final String name;
    private final ILangEntry langKey;
    private final ILangEntry descLangKey;
    private final int maxStack;
    private final EnumColor color;

    Upgrade(String name, ILangEntry langKey, ILangEntry descLangKey, int maxStack, EnumColor color) {
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
     *
     * @implNote Unmodifiable if empty.
     */
    public static Map<Upgrade, Integer> buildMap(@Nullable CompoundTag nbtTags) {
        Map<Upgrade, Integer> upgrades = null;
        if (nbtTags != null && nbtTags.contains(SerializationConstants.UPGRADES, Tag.TAG_LIST)) {
            ListTag list = nbtTags.getList(SerializationConstants.UPGRADES, Tag.TAG_COMPOUND);
            for (int tagCount = 0; tagCount < list.size(); tagCount++) {
                CompoundTag compound = list.getCompound(tagCount);
                Upgrade upgrade = BY_ID.apply(compound.getInt(SerializationConstants.TYPE));
                //Validate the nbt isn't malformed with a negative or zero amount
                int installed = Math.max(compound.getInt(SerializationConstants.AMOUNT), 0);
                if (installed > 0) {
                    if (upgrades == null) {
                        upgrades = new EnumMap<>(Upgrade.class);
                    }
                    upgrades.put(upgrade, installed);
                }
            }
        }
        return upgrades == null ? Collections.emptyMap() : upgrades;
    }

    /**
     * Writes a map of upgrades to their amounts to NBT.
     *
     * @param upgrades Upgrades to store.
     * @param nbtTags  Tag to write to.
     */
    public static void saveMap(Map<Upgrade, Integer> upgrades, CompoundTag nbtTags) {
        ListTag list = new ListTag(upgrades.size());
        for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            list.add(entry.getKey().getTag(entry.getValue()));
        }
        nbtTags.put(SerializationConstants.UPGRADES, list);
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
        compound.putInt(SerializationConstants.TYPE, ordinal());
        compound.putInt(SerializationConstants.AMOUNT, amount);
        return compound;
    }

    /**
     * Gets the "raw" name of this upgrade for use in registry names.
     */
    @Override
    public String getSerializedName() {
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

    public interface IUpgradeInfoHandler {

        List<Component> getInfo(Upgrade upgrade);
    }
}