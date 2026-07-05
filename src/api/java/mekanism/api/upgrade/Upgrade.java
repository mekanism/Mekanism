package mekanism.api.upgrade;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import mekanism.api.SupportsColorMap;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Range;

/// @param displayName Translated display name of this upgrade.
/// @param description Translated description of what this upgrade does.
/// @param color       The color to use when rendering various information related to this upgrade.
/// @param max         The max number of upgrades of this type that can be installed.
///
/// @since 10.8.0
public record Upgrade(Component displayName, Component description, Either<EnumColor, TextColor> color, @Range(from = 1, to = Integer.MAX_VALUE) int max)
      implements IHasTextComponent {

    /// Direct codec for loading from file and registry sync
    public static final Codec<Upgrade> DIRECT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
          ComponentSerialization.CODEC.fieldOf(SerializationConstants.DISPLAY_NAME).forGetter(Upgrade::displayName),
          ComponentSerialization.CODEC.fieldOf(SerializationConstants.DESCRIPTION).forGetter(Upgrade::description),
          Codec.either(EnumColor.CODEC, TextColor.CODEC).fieldOf(SerializationConstants.COLOR).forGetter(Upgrade::color),
          NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, SerializationConstants.MAX, 1).forGetter(Upgrade::max)
    ).apply(builder, Upgrade::new));
    /// Codec for referencing an upgrade by name
    public static final Codec<Holder<Upgrade>> CODEC = RegistryFixedCodec.create(MekanismRegistries.Keys.UPGRADES);
    /// Stream codec for referencing an upgrade by name
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Upgrade>> STREAM_CODEC = ByteBufCodecs.holderRegistry(MekanismRegistries.Keys.UPGRADES);

    /// Helper to create an upgrade with a max size of `1` for use in datagen with the default name and description translation keys.
    ///
    /// @param key   Name the upgrade is being registered under to get the default translation keys of the display name and the description.
    /// @param color The color to use when rendering various information related to this upgrade.
    public static Upgrade create(ResourceKey<? extends Upgrade> key, EnumColor color) {
        return create(key, color, 1);
    }

    /// Helper to create an upgrade for use in datagen with the default name and description translation keys.
    ///
    /// @param key   Name the upgrade is being registered under to get the default translation keys of the display name and the description.
    /// @param color The color to use when rendering various information related to this upgrade.
    /// @param max   The max number of upgrades of this type that can be installed.
    public static Upgrade create(ResourceKey<? extends Upgrade> key, EnumColor color, int max) {
        return new Upgrade(getDefaultTranslationKey(key), getDefaultDescriptionKey(key), Either.left(color), max);
    }

    /// Helper to create an upgrade with a max size of `1` for use in datagen with the default name and description translation keys.
    ///
    /// @param key   Name the upgrade is being registered under to get the default translation keys of the display name and the description.
    /// @param color The color to use when rendering various information related to this upgrade.
    public static Upgrade create(ResourceKey<? extends Upgrade> key, TextColor color) {
        return create(key, color, 1);
    }

    /// Helper to create an upgrade for use in datagen with the default name and description translation keys.
    ///
    /// @param key   Name the upgrade is being registered under to get the default translation keys of the display name and the description.
    /// @param color The color to use when rendering various information related to this upgrade.
    /// @param max   The max number of upgrades of this type that can be installed.
    public static Upgrade create(ResourceKey<? extends Upgrade> key, TextColor color, int max) {
        return new Upgrade(getDefaultTranslationKey(key), getDefaultDescriptionKey(key), Either.right(color), max);
    }

    /// Creates an upgrade with a max size of `1`
    ///
    /// @param displayName Translated display name of this upgrade.
    /// @param description Translated description of what this upgrade does.
    /// @param color       The color to use when rendering various information related to this upgrade.
    public Upgrade(Component displayName, Component description, Either<EnumColor, TextColor> color) {
        this(displayName, description, color, 1);
    }

    /// Creates an upgrade with a max size of `1`
    ///
    /// @param nameTranslationKey        Translation key to use as the display name of this upgrade.
    /// @param descriptionTranslationKey Translation key to use as the description of what this upgrade does.
    /// @param color                     The color to use when rendering various information related to this upgrade.
    public Upgrade(String nameTranslationKey, String descriptionTranslationKey, Either<EnumColor, TextColor> color) {
        this(nameTranslationKey, descriptionTranslationKey, color, 1);
    }

    /// @param nameTranslationKey        Translation key to use as the display name of this upgrade.
    /// @param descriptionTranslationKey Translation key to use as the description of what this upgrade does.
    /// @param color                     The color to use when rendering various information related to this upgrade.
    /// @param max                       The max number of upgrades of this type that can be installed.
    public Upgrade(String nameTranslationKey, String descriptionTranslationKey, Either<EnumColor, TextColor> color, int max) {
        this(TextComponentUtil.translate(nameTranslationKey), TextComponentUtil.translate(descriptionTranslationKey), color, max);
    }

    public Upgrade {
        Objects.requireNonNull(color);
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(description);
        if (max <= 0) {
            throw new IllegalArgumentException("Maximum number of upgrades must be a positive integer");
        }
    }

    /// {@return the text color for this upgrade}
    public TextColor textColor() {
        return color.map(SupportsColorMap::getTextColor, Function.identity());
    }

    /// {@return the ARGB color representation for this upgrade}
    public int argb() {
        return color.map(SupportsColorMap::getPackedColor, textColor -> ARGB.opaque(textColor.getValue()));
    }

    /// {@return `true` if this upgrade supports being installed multiple times on the same machine}
    public boolean supportsMultiple() {
        return max() > 1;
    }

    @Override
    public Component getTextComponent() {
        return displayName;
    }

    /// Helper to get the default translation key path for a given [Upgrade].
    ///
    /// @param key [Upgrade] name.
    public static String getDefaultTranslationKey(ResourceKey<? extends Upgrade> key) {
        return key.identifier().toLanguageKey("upgrade");
    }

    /// Helper to get the default description translation key path for a given [Upgrade].
    ///
    /// @param key [Upgrade] name.
    public static String getDefaultDescriptionKey(ResourceKey<? extends Upgrade> key) {
        return key.identifier().toLanguageKey("upgrade", "description");
    }
}