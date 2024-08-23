package mekanism.common.lib.transmitter;

import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

@NothingNullByDefault
public enum ConnectionType implements IIncrementalEnum<ConnectionType>, StringRepresentable, IHasTranslationKey, IHasEnumNameTextComponent {
    NORMAL(MekanismLang.CONNECTION_NORMAL, EnumColor.ORANGE),
    PUSH(MekanismLang.CONNECTION_PUSH, EnumColor.BRIGHT_GREEN),
    PULL(MekanismLang.CONNECTION_PULL, EnumColor.YELLOW),
    NONE(MekanismLang.CONNECTION_NONE, EnumColor.WHITE);

    public static final IntFunction<ConnectionType> BY_ID = ByIdMap.continuous(ConnectionType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, ConnectionType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ConnectionType::ordinal);

    private final ILangEntry langEntry;
    private final EnumColor color;

    ConnectionType(ILangEntry langEntry, EnumColor color) {
        this.langEntry = langEntry;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public Component getTextComponent() {
        return langEntry.translateColored(color);
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }

    @Override
    public ConnectionType byIndex(int index) {
        return BY_ID.apply(index);
    }

    /**
     * @return {@code true} If this connection type allows other things to insert into it.
     */
    public boolean canAccept() {
        return this == NORMAL || this == PULL;
    }

    /**
     * @return {@code true} If this connection type can send to other things or allow them to extract from it.
     */
    public boolean canSendTo() {
        return this == NORMAL || this == PUSH;
    }
}