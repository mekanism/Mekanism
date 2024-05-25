package mekanism.common.attachments.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.common.util.EnumUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

//TODO - 1.20.5: Try to make the version be what is attached to things that support ejectors so that if nothing is set it doesn't end up in the component patch
@NothingNullByDefault
public record AttachedEjector(List<Optional<EnumColor>> inputColors, boolean strictInput, Optional<EnumColor> outputColor) {

    public static final Codec<AttachedEjector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ExtraCodecs.optionalEmptyMap(EnumColor.CODEC).listOf(EnumUtils.SIDES.length, EnumUtils.SIDES.length).fieldOf(SerializationConstants.INPUT_COLOR).forGetter(AttachedEjector::inputColors),
          Codec.BOOL.fieldOf(SerializationConstants.STRICT_INPUT).forGetter(AttachedEjector::strictInput),
          EnumColor.CODEC.optionalFieldOf(SerializationConstants.QIO_META_TYPES).forGetter(AttachedEjector::outputColor)
    ).apply(instance, AttachedEjector::new));
    public static final StreamCodec<ByteBuf, AttachedEjector> STREAM_CODEC = StreamCodec.composite(
          EnumColor.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(EnumUtils.SIDES.length)), AttachedEjector::inputColors,
          ByteBufCodecs.BOOL, AttachedEjector::strictInput,
          EnumColor.OPTIONAL_STREAM_CODEC, AttachedEjector::outputColor,
          AttachedEjector::new
    );

    public static AttachedEjector create(EnumColor[] inputColors, boolean strictInput, @Nullable EnumColor outputColor) {
        List<Optional<EnumColor>> inputs = new ArrayList<>(inputColors.length);
        for (EnumColor inputColor : inputColors) {
            inputs.add(Optional.ofNullable(inputColor));
        }
        return new AttachedEjector(inputs, strictInput, Optional.ofNullable(outputColor));
    }

    public AttachedEjector {
        if (inputColors.size() != EnumUtils.SIDES.length) {
            throw new IllegalArgumentException("Expected there to be an input color for each side");
        }
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        inputColors = Collections.unmodifiableList(inputColors);
    }
}