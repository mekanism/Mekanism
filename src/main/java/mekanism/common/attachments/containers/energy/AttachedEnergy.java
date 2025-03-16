
package mekanism.common.attachments.containers.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedEnergy(List<Long> containers) implements IAttachedContainers<Long, AttachedEnergy> {

    public static final AttachedEnergy EMPTY = new AttachedEnergy(Collections.emptyList());

    @SuppressWarnings("removal")
    public static final Codec<AttachedEnergy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          SerializerHelper.POSITIVE_LONG_CODEC_LEGACY.listOf().fieldOf(SerializationConstants.ENERGY_CONTAINERS).forGetter(AttachedEnergy::containers)
    ).apply(instance, AttachedEnergy::new));
    public static final StreamCodec<ByteBuf, AttachedEnergy> STREAM_CODEC =
          ByteBufCodecs.VAR_LONG.<List<Long>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                .map(AttachedEnergy::new, AttachedEnergy::containers);

    public static AttachedEnergy create(int containers) {
        return new AttachedEnergy(NonNullList.withSize(containers, 0L));
    }

    public AttachedEnergy {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public Long getEmptyStack() {
        return 0L;
    }

    @Override
    public AttachedEnergy create(List<Long> containers) {
        return new AttachedEnergy(containers);
    }
}