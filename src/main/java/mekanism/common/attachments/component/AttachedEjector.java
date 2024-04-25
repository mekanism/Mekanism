package mekanism.common.attachments.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.common.util.EnumUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedEjector(List<EnumColor> inputColors, boolean strictInput, Optional<EnumColor> outputColor) {

    public static final Codec<AttachedEjector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          EnumColor.CODEC.listOf(EnumUtils.SIDES.length, EnumUtils.SIDES.length).fieldOf(NBTConstants.INPUT_COLOR).forGetter(AttachedEjector::inputColors),
          Codec.BOOL.fieldOf(NBTConstants.STRICT_INPUT).forGetter(AttachedEjector::strictInput),
          EnumColor.CODEC.optionalFieldOf(NBTConstants.QIO_META_TYPES).forGetter(AttachedEjector::outputColor)
    ).apply(instance, AttachedEjector::new));
    public static final StreamCodec<ByteBuf, AttachedEjector> STREAM_CODEC = StreamCodec.composite(
          EnumColor.STREAM_CODEC.apply(ByteBufCodecs.list(EnumUtils.SIDES.length)), AttachedEjector::inputColors,
          ByteBufCodecs.BOOL, AttachedEjector::strictInput,
          ByteBufCodecs.optional(EnumColor.STREAM_CODEC), AttachedEjector::outputColor,
          AttachedEjector::new
    );

    public AttachedEjector {
        if (inputColors.size() != EnumUtils.SIDES.length) {
            throw new IllegalArgumentException("Expected there to be an input color for each side");
        }
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        inputColors = Collections.unmodifiableList(inputColors);
    }
}