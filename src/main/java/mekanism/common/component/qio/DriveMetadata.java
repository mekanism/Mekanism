package mekanism.common.component.qio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record DriveMetadata(long count, int types, DriveCapacity capacity) implements TooltipProvider {

    public static final Codec<DriveMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ExtraCodecs.NON_NEGATIVE_LONG.fieldOf(SerializationConstants.COUNT).forGetter(DriveMetadata::count),
          ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.TYPES).forGetter(DriveMetadata::types),
          DriveCapacity.CODEC.fieldOf(SerializationConstants.CAPACITY).forGetter(DriveMetadata::capacity)
    ).apply(instance, DriveMetadata::new));
    public static final StreamCodec<ByteBuf, DriveMetadata> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, DriveMetadata::count,
          ByteBufCodecs.VAR_INT, DriveMetadata::types,
          DriveCapacity.STREAM_CODEC, DriveMetadata::capacity,
          DriveMetadata::new
    );

    public boolean isEmpty() {
        return count == 0 && types == 0;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        DriveCapacity capacity = capacity();
        builder.accept(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(count), TextUtils.format(capacity.count())));
        builder.accept(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(types), TextUtils.format(capacity.types())));
    }

    public record DriveCapacity(long count, int types) {

        public static final Codec<DriveCapacity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              ExtraCodecs.NON_NEGATIVE_LONG.fieldOf(SerializationConstants.COUNT).forGetter(DriveCapacity::count),
              ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.TYPES).forGetter(DriveCapacity::types)
        ).apply(instance, DriveCapacity::new));
        public static final StreamCodec<ByteBuf, DriveCapacity> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.VAR_LONG, DriveCapacity::count,
              ByteBufCodecs.VAR_INT, DriveCapacity::types,
              DriveCapacity::new
        );

    }
}