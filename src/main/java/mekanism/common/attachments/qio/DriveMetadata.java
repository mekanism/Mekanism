package mekanism.common.attachments.qio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.common.content.qio.QIODriveData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record DriveMetadata(long count, int types) {

    public static final DriveMetadata EMPTY = new DriveMetadata(0, 0);

    public static final Codec<DriveMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.COUNT).forGetter(DriveMetadata::count),
          ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.TYPES).forGetter(DriveMetadata::types)
    ).apply(instance, DriveMetadata::new));
    public static final StreamCodec<ByteBuf, DriveMetadata> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, DriveMetadata::count,
          ByteBufCodecs.VAR_INT, DriveMetadata::types,
          DriveMetadata::new
    );

    public DriveMetadata(QIODriveData data) {
        this(data.getTotalCount(), data.getTotalTypes());
    }

    public boolean isEmpty() {
        return count == 0 && types == 0;
    }
}