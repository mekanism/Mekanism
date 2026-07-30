package mekanism.common.component.qio;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.IQIODriveCapacity;
import mekanism.common.tier.QIODriveTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record DriveMetadata(long count, int types, Either<QIODriveTier, IQIODriveCapacity> rawCapacity) implements TooltipProvider {

    public static final Codec<DriveMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ExtraCodecs.NON_NEGATIVE_LONG.fieldOf(SerializationConstants.COUNT).forGetter(DriveMetadata::count),
          ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.TYPES).forGetter(DriveMetadata::types),
          Codec.either(QIODriveTier.CODEC, DriveCapacity.CODEC).fieldOf(SerializationConstants.CAPACITY).forGetter(DriveMetadata::rawCapacity)
    ).apply(instance, DriveMetadata::new));
    public static final StreamCodec<ByteBuf, DriveMetadata> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, DriveMetadata::count,
          ByteBufCodecs.VAR_INT, DriveMetadata::types,
          ByteBufCodecs.either(
                QIODriveTier.STREAM_CODEC,
                DriveCapacity.STREAM_CODEC
          ), DriveMetadata::rawCapacity,
          DriveMetadata::new
    );

    public DriveMetadata(long count, int types, IQIODriveCapacity capacity) {
        Either<QIODriveTier, IQIODriveCapacity> rawCapacity = switch (capacity) {
            case QIODriveTier tier -> Either.left(tier);
            default -> Either.right(capacity);
        };
        this(count, types, rawCapacity);
    }

    public boolean isEmpty() {
        return count == 0 && types == 0;
    }

    public IQIODriveCapacity capacity() {
        return rawCapacity.map(Function.identity(), Function.identity());
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        IQIODriveCapacity capacity = capacity();
        builder.accept(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(count), TextUtils.format(capacity.count())));
        builder.accept(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(types), TextUtils.format(capacity.types())));
    }

    public record DriveCapacity(long count, int types) implements IQIODriveCapacity {

        public static final Codec<IQIODriveCapacity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              ExtraCodecs.NON_NEGATIVE_LONG.fieldOf(SerializationConstants.COUNT).forGetter(IQIODriveCapacity::count),
              ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.TYPES).forGetter(IQIODriveCapacity::types)
        ).apply(instance, DriveCapacity::new));
        public static final StreamCodec<ByteBuf, IQIODriveCapacity> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.VAR_LONG, IQIODriveCapacity::count,
              ByteBufCodecs.VAR_INT, IQIODriveCapacity::types,
              DriveCapacity::new
        );

    }
}