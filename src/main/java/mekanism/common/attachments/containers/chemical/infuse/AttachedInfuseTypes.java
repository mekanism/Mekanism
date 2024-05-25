package mekanism.common.attachments.containers.chemical.infuse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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

    public static final Codec<AttachedInfuseTypes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          InfusionStack.OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.INFUSION_TANKS).forGetter(AttachedInfuseTypes::containers)
    ).apply(instance, AttachedInfuseTypes::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachedInfuseTypes> STREAM_CODEC =
          InfusionStack.OPTIONAL_STREAM_CODEC.<List<InfusionStack>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                .map(AttachedInfuseTypes::new, AttachedInfuseTypes::containers);
    private static final Int2ObjectMap<AttachedInfuseTypes> EMPTY_DEFAULTS = new Int2ObjectOpenHashMap<>();

    public static AttachedInfuseTypes create(int containers) {
        return EMPTY_DEFAULTS.computeIfAbsent(containers, AttachedInfuseTypes::new);
    }

    public AttachedInfuseTypes {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    private AttachedInfuseTypes(int containers) {
        this(NonNullList.withSize(containers, InfusionStack.EMPTY));
    }

    @Override
    public AttachedInfuseTypes create(List<InfusionStack> containers) {
        return new AttachedInfuseTypes(containers);
    }
}