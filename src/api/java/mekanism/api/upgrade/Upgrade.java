package mekanism.api.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/// @param displayName Translated display name of this upgrade.
/// @param description Translated description of what this upgrade does.
/// @param color       The color to use when rendering various information related to this upgrade.
/// @param max         The max number of upgrades of this type that can be installed.
///
/// @since 10.8.0
public record Upgrade(Component displayName, Component description, EnumColor color, int max) implements IHasTextComponent {//TODO - 26.2: Docs

    public static final Codec<Upgrade> DIRECT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
          ComponentSerialization.CODEC.fieldOf(SerializationConstants.DISPLAY_NAME).forGetter(Upgrade::displayName),
          ComponentSerialization.CODEC.fieldOf(SerializationConstants.DESCRIPTION).forGetter(Upgrade::description),
          EnumColor.CODEC.fieldOf(SerializationConstants.COLOR).forGetter(Upgrade::color),
          NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, SerializationConstants.MAX, 1).forGetter(Upgrade::max)
    ).apply(builder, Upgrade::new));
    public static final Codec<Holder<Upgrade>> CODEC = RegistryFixedCodec.create(MekanismRegistries.Keys.UPGRADES);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Upgrade>> STREAM_CODEC = ByteBufCodecs.holderRegistry(MekanismRegistries.Keys.UPGRADES);

    public static Upgrade create(ResourceKey<? extends Upgrade> key, EnumColor color) {
        return create(key, color, 1);
    }

    public static Upgrade create(ResourceKey<? extends Upgrade> key, EnumColor color, int max) {
        return new Upgrade(getDefaultTranslationKey(key), getDefaultDescriptionKey(key), color, max);
    }

    public Upgrade(Component displayName, Component description, EnumColor color) {
        this(displayName, description, color, 1);
    }

    public Upgrade(String nameTranslationKey, String descriptionTranslationKey, EnumColor color) {
        this(nameTranslationKey, descriptionTranslationKey, color, 1);
    }

    public Upgrade(String nameTranslationKey, String descriptionTranslationKey, EnumColor color, int max) {
        this(TextComponentUtil.translate(nameTranslationKey), TextComponentUtil.translate(descriptionTranslationKey), color, max);
    }

    public Upgrade {
        //TODO - 26.2: Do we want to switch the color to an int?
        Objects.requireNonNull(color);
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(description);
        if (max <= 0) {
            throw new IllegalArgumentException("Maximum number of upgrades must be a positive integer");
        }
    }

    public boolean supportsMultiple() {
        return max() > 1;
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

    @Override
    public Component getTextComponent() {
        return displayName;
    }
}