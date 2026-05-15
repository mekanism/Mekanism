package mekanism.common.attachments.containers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.NonNull;

public record AttachedResources<RESOURCE extends @NonNull Resource>(List<@NonNull LargeResourceStack<RESOURCE>> containers)
      implements IAttachedContainers<LargeResourceStack<RESOURCE>, AttachedResources<RESOURCE>> {

    private static final AttachedResources<?> EMPTY = new AttachedResources<>(Collections.emptyList());

    @SuppressWarnings("unchecked")
    public static <RESOURCE extends @NonNull Resource> AttachedResources<RESOURCE> empty() {
        return (AttachedResources<RESOURCE>) EMPTY;
    }

    public static <RESOURCE extends @NonNull Resource> Codec<AttachedResources<RESOURCE>> codec(Codec<LargeResourceStack<RESOURCE>> lenientOptionalStackCodec,
          String containerListKey) {
        //TODO - 26.1: Can this just be a "generic" key instead of something we have to pass in
        return RecordCodecBuilder.create(instance -> instance.group(
              lenientOptionalStackCodec.listOf().fieldOf(containerListKey).forGetter(AttachedResources::containers)
        ).apply(instance, AttachedResources::new));
    }

    public static <RESOURCE extends @NonNull Resource> StreamCodec<RegistryFriendlyByteBuf, AttachedResources<RESOURCE>> streamCodec(
          StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>> stackCodec) {
        return stackCodec.<List<LargeResourceStack<RESOURCE>>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
              .map(AttachedResources::new, AttachedResources::containers);
    }

    @NonNull
    public static <RESOURCE extends @NonNull Resource> AttachedResources<RESOURCE> create(int containers, LargeResourceStack<RESOURCE> emptyStack) {
        return new AttachedResources<>(NonNullList.withSize(containers, emptyStack));
    }

    public AttachedResources {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @NonNull
    @Override
    public AttachedResources<RESOURCE> create(@NonNull List<LargeResourceStack<RESOURCE>> containers) {
        return new AttachedResources<>(containers);
    }
}