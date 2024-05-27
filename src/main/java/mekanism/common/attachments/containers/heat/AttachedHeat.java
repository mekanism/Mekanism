package mekanism.common.attachments.containers.heat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedHeat(List<HeatCapacitorData> containers) implements IAttachedContainers<HeatCapacitorData, AttachedHeat> {

    public static final AttachedHeat EMPTY = new AttachedHeat(Collections.emptyList());

    public static final Codec<AttachedHeat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          HeatCapacitorData.CODEC.listOf().fieldOf(SerializationConstants.HEAT_CAPACITORS).forGetter(AttachedHeat::containers)
    ).apply(instance, AttachedHeat::new));
    public static final StreamCodec<ByteBuf, AttachedHeat> STREAM_CODEC = HeatCapacitorData.STREAM_CODEC.
          <List<HeatCapacitorData>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)).map(AttachedHeat::new, AttachedHeat::containers);

    public AttachedHeat {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public HeatCapacitorData getEmptyStack() {
        throw new UnsupportedOperationException("Attached heat has no concept of a default stack and callers should override methods instead to use the proper default data");
    }

    @Override
    public AttachedHeat create(List<HeatCapacitorData> containers) {
        return new AttachedHeat(containers);
    }
}