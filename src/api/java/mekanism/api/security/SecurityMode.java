package mekanism.api.security;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * Simple security enum for defining different access levels.
 *
 * @since 10.2.1
 */
@NothingNullByDefault
public enum SecurityMode implements IIncrementalEnum<SecurityMode>, IHasEnumNameTextComponent, StringRepresentable {
    /**
     * Public Security: Everyone is allowed access.
     */
    PUBLIC(APILang.PUBLIC, EnumColor.BRIGHT_GREEN),
    /**
     * Private Security: Only the owner is allowed access.
     */
    PRIVATE(APILang.PRIVATE, EnumColor.RED),
    /**
     * Trusted Security: The owner and anyone they mark as trusted in their security desk are allowed access.
     */
    TRUSTED(APILang.TRUSTED, EnumColor.INDIGO);

    /**
     * Codec for serializing security modes based on their name.
     *
     * @since 10.6.0
     */
    public static final Codec<SecurityMode> CODEC = StringRepresentable.fromEnum(SecurityMode::values);
    /**
     * Gets a security mode by index, wrapping for out of bounds indices.
     *
     * @since 10.6.0
     */
    public static final IntFunction<SecurityMode> BY_ID = ByIdMap.continuous(SecurityMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    /**
     * Stream codec for syncing security modes by index.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<ByteBuf, SecurityMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SecurityMode::ordinal);

    private final String serializedName;
    private final ILangEntry langEntry;
    private final EnumColor color;

    SecurityMode(ILangEntry langEntry, EnumColor color) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.langEntry = langEntry;
        this.color = color;
    }

    @Override
    public Component getTextComponent() {
        return langEntry.translateColored(color);
    }

    @Override
    public SecurityMode byIndex(int index) {
        return BY_ID.apply(index);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}