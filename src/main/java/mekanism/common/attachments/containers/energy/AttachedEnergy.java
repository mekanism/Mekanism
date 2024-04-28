
package mekanism.common.attachments.containers.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedEnergy(List<FloatingLong> containers) implements IAttachedContainers<FloatingLong, AttachedEnergy> {

    public static final Codec<AttachedEnergy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          FloatingLong.CODEC.listOf().fieldOf(NBTConstants.ENERGY_CONTAINERS).forGetter(AttachedEnergy::containers)
    ).apply(instance, AttachedEnergy::new));
    public static final StreamCodec<ByteBuf, AttachedEnergy> STREAM_CODEC =
          FloatingLong.STREAM_CODEC.<List<FloatingLong>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                .map(AttachedEnergy::new, AttachedEnergy::containers);

    public AttachedEnergy {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    public AttachedEnergy(int containers) {
        this(NonNullList.withSize(containers, FloatingLong.ZERO));
    }

    @Override
    public AttachedEnergy create(List<FloatingLong> containers) {
        return new AttachedEnergy(containers);
    }
}