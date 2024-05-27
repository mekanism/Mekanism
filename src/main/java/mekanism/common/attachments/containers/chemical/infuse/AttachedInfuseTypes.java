package mekanism.common.attachments.containers.chemical.infuse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedInfuseTypes(List<InfusionStack> containers) implements IAttachedContainers<InfusionStack, AttachedInfuseTypes> {

    public static final AttachedInfuseTypes EMPTY = new AttachedInfuseTypes(Collections.emptyList());

    public static final Codec<AttachedInfuseTypes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          InfusionStack.OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.INFUSION_TANKS).forGetter(AttachedInfuseTypes::containers)
    ).apply(instance, AttachedInfuseTypes::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachedInfuseTypes> STREAM_CODEC =
          InfusionStack.OPTIONAL_STREAM_CODEC.<List<InfusionStack>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                .map(AttachedInfuseTypes::new, AttachedInfuseTypes::containers);

    public static AttachedInfuseTypes create(int containers) {
        return new AttachedInfuseTypes(NonNullList.withSize(containers, InfusionStack.EMPTY));
    }

    public AttachedInfuseTypes {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }

    @Override
    public AttachedInfuseTypes create(List<InfusionStack> containers) {
        return new AttachedInfuseTypes(containers);
    }
}