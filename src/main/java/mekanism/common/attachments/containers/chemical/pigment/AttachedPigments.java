package mekanism.common.attachments.containers.chemical.pigment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedPigments(List<PigmentStack> containers) implements IAttachedContainers<PigmentStack, AttachedPigments> {

    public static final Codec<AttachedPigments> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          PigmentStack.OPTIONAL_CODEC.listOf().fieldOf(NBTConstants.PIGMENT_TANKS).forGetter(AttachedPigments::containers)
    ).apply(instance, AttachedPigments::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachedPigments> STREAM_CODEC =
          PigmentStack.OPTIONAL_STREAM_CODEC.<List<PigmentStack>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                .map(AttachedPigments::new, AttachedPigments::containers);

    public AttachedPigments {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    public AttachedPigments(int containers) {
        this(NonNullList.withSize(containers, PigmentStack.EMPTY));
    }

    @Override
    public AttachedPigments create(List<PigmentStack> containers) {
        return new AttachedPigments(containers);
    }
}