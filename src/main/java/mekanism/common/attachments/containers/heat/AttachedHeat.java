package mekanism.common.attachments.containers.heat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.attachments.containers.IAttachedContainers;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.attachments.containers.fluid.AttachedFluids;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedHeat(List<HeatCapacitorData> containers) implements IAttachedContainers<HeatCapacitorData, AttachedHeat> {

    public static final Codec<AttachedHeat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          HeatCapacitorData.CODEC.listOf().fieldOf(NBTConstants.HEAT_CAPACITORS).forGetter(AttachedHeat::containers)
    ).apply(instance, AttachedHeat::new));
    public static final StreamCodec<ByteBuf, AttachedHeat> STREAM_CODEC = HeatCapacitorData.STREAM_CODEC.
          <List<HeatCapacitorData>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)).map(AttachedHeat::new, AttachedHeat::containers);

    public AttachedHeat {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public AttachedHeat create(List<HeatCapacitorData> containers) {
        return new AttachedHeat(containers);
    }
}