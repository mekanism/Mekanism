package mekanism.common.tile.interfaces;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public interface IRedstoneControl {

    /**
     * Gets the RedstoneControl type from this block.
     *
     * @return this block's RedstoneControl type
     */
    RedstoneControl getControlType();

    /**
     * Sets this block's RedstoneControl type to a new value.
     *
     * @param type - RedstoneControl type to set
     */
    void setControlType(RedstoneControl type);

    /**
     * If the block is getting powered or not by redstone (indirectly).
     *
     * @return if the block is getting powered indirectly
     */
    boolean isPowered();

    /**
     * If the block was getting powered or not by redstone, last tick. Used for PULSE mode.
     */
    boolean wasPowered();

    /**
     * If the machine supports the given redstone control mode.
     */
    boolean supportsMode(RedstoneControl mode);

    @NothingNullByDefault
    enum RedstoneControl implements IIncrementalEnum<RedstoneControl>, IHasEnumNameTextComponent, StringRepresentable {
        DISABLED(MekanismLang.REDSTONE_CONTROL_DISABLED),
        HIGH(MekanismLang.REDSTONE_CONTROL_HIGH),
        LOW(MekanismLang.REDSTONE_CONTROL_LOW),
        PULSE(MekanismLang.REDSTONE_CONTROL_PULSE);

        public static final Codec<RedstoneControl> CODEC = StringRepresentable.fromEnum(RedstoneControl::values);
        public static final IntFunction<RedstoneControl> BY_ID = ByIdMap.continuous(RedstoneControl::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, RedstoneControl> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, RedstoneControl::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;

        RedstoneControl(ILangEntry langEntry) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate();
        }

        @Override
        public RedstoneControl byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}