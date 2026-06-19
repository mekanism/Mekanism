package mekanism.api;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

//TODO - 26.2: Move this to a registry so that it is extensible
public enum Upgrade implements IHasEnumNameTranslationKey, StringRepresentable {
    SPEED("speed", APILang.UPGRADE_SPEED, APILang.UPGRADE_SPEED_DESCRIPTION, 8, EnumColor.RED),
    ENERGY("energy", APILang.UPGRADE_ENERGY, APILang.UPGRADE_ENERGY_DESCRIPTION, 8, EnumColor.BRIGHT_GREEN),
    FILTER("filter", APILang.UPGRADE_FILTER, APILang.UPGRADE_FILTER_DESCRIPTION, 1, EnumColor.DARK_AQUA),
    CHEMICAL("chemical", APILang.UPGRADE_CHEMICAL, APILang.UPGRADE_CHEMICAL_DESCRIPTION, 8, EnumColor.YELLOW),
    MUFFLING("muffling", APILang.UPGRADE_MUFFLING, APILang.UPGRADE_MUFFLING_DESCRIPTION, 1, EnumColor.INDIGO),
    ANCHOR("anchor", APILang.UPGRADE_ANCHOR, APILang.UPGRADE_ANCHOR_DESCRIPTION, 1, EnumColor.DARK_GREEN),
    STONE_GENERATOR("stone_generator", APILang.UPGRADE_STONE_GENERATOR, APILang.UPGRADE_STONE_GENERATOR_DESCRIPTION, 1, EnumColor.ORANGE);

    /// Codec for serializing upgrades based on their name.
    ///
    /// @since 10.6.0
    public static final Codec<Upgrade> CODEC = StringRepresentable.fromEnum(Upgrade::values);
    //TODO - 26.2: Validate there are no cases where a zero value is stored in an upgrade map as our positive int will error for that
    //TODO - 26.2: Make sure this is lenient so if there are invalid amounts or unknown upgrades then it skips them. Maybe just LenientUnboundedMapCodec ?
    private static final Codec<Map<Upgrade, Integer>> UPGRADE_MAP_CODEC = Codec.unboundedMap(CODEC, ExtraCodecs.POSITIVE_INT);

    /// Gets an upgrade by index, wrapping for out of bounds indices.
    ///
    /// @since 10.6.0
    public static final IntFunction<Upgrade> BY_ID = ByIdMap.continuous(Upgrade::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    /// Stream codec for syncing upgrades by index.
    ///
    /// @since 10.6.0
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

    /// Reads and builds a map of upgrades to their amounts from the given input.
    ///
    /// @param upgradeInput Stored upgrades.
    ///
    /// @return Unmodifiable map representing the installed upgrades.
    public static Map<Upgrade, Integer> buildMap(ValueInput upgradeInput) {
        return upgradeInput.read(SerializationConstants.UPGRADES, UPGRADE_MAP_CODEC).orElse(Collections.emptyMap());
    }

    /// Writes a map of upgrades to their amounts to NBT.
    ///
    /// @param upgrades      Upgrades to store.
    /// @param upgradeOutput Output to write upgrades to.
    public static void saveMap(Map<Upgrade, Integer> upgrades, ValueOutput upgradeOutput) {
        if (!upgrades.isEmpty()) {
            upgradeOutput.store(SerializationConstants.UPGRADES, UPGRADE_MAP_CODEC, upgrades);
        }
    }

    /// Gets the "raw" name of this upgrade for use in registry names.
    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return langKey.getTranslationKey();
    }

    /// Gets the description for this upgrade.
    public Component getDescription() {
        return descLangKey.translate();
    }

    /// Gets the max number of upgrades of this type that can be installed.
    public int getMax() {
        return maxStack;
    }

    /// Gets the color to use when rendering various information related to this upgrade.
    public EnumColor getColor() {
        return color;
    }

    public interface IUpgradeInfoHandler {

        List<Component> getInfo(Upgrade upgrade);
    }
}