package mekanism.common.attachments.containers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.container.LargeResourceStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.NonNull;

public record AttachedResources<RESOURCE extends @NonNull Resource>(List<@NonNull LargeResourceStack<RESOURCE>> containers)
      implements IAttachedContainers<LargeResourceStack<RESOURCE>, AttachedResources<RESOURCE>> {

    private static final AttachedResources<?> EMPTY = new AttachedResources<>(Collections.emptyList());

    @SuppressWarnings("unchecked")
    public static <RESOURCE extends @NonNull Resource> AttachedResources<RESOURCE> empty() {
        return (AttachedResources<RESOURCE>) EMPTY;
    }

    public static <RESOURCE extends @NonNull Resource> Codec<AttachedResources<RESOURCE>> codec(Codec<RESOURCE> resourceCodec, RESOURCE emptyResource, String containerListKey) {
        //TODO - 26.1: See about simplifying/merging this codec
        LargeResourceStack<RESOURCE> emptyStack = new LargeResourceStack<>(emptyResource, 0);
        Codec<LargeResourceStack<RESOURCE>> stackCodec = RecordCodecBuilder.create(instance -> instance.group(
              resourceCodec.fieldOf(SerializationConstants.TYPE).forGetter(LargeResourceStack::resource),
              SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.AMOUNT).forGetter(LargeResourceStack::amount)
        ).apply(instance, LargeResourceStack::new));
        Codec<LargeResourceStack<RESOURCE>> optionalStackCodec = ExtraCodecs.optionalEmptyMap(stackCodec)
              .xmap(optional -> optional.orElse(emptyStack), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
        Codec<LargeResourceStack<RESOURCE>> lenientOptionalStackCodec = optionalStackCodec
              .promotePartial(error -> MekanismAPI.logger.error("Tried to load invalid resource: '{}'", error))
              .orElse(emptyStack);
        //TODO - 26.1: Can this just be a "generic" key instead of something we have to pass in
        return RecordCodecBuilder.create(instance -> instance.group(
              lenientOptionalStackCodec.listOf().fieldOf(containerListKey).forGetter(AttachedResources::containers)
        ).apply(instance, AttachedResources::new));
    }

    public static <RESOURCE extends @NonNull Resource> StreamCodec<RegistryFriendlyByteBuf, AttachedResources<RESOURCE>> streamCodec(
          StreamCodec<RegistryFriendlyByteBuf, RESOURCE> resourceCodec) {
        StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>> stackCodec = StreamCodec.composite(
              resourceCodec, LargeResourceStack::resource,
              ByteBufCodecs.VAR_LONG, LargeResourceStack::amount,
              LargeResourceStack::new
        );
        return stackCodec.<List<LargeResourceStack<RESOURCE>>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
              .map(AttachedResources::new, AttachedResources::containers);
    }

    @NonNull
    public static <RESOURCE extends @NonNull Resource> AttachedResources<RESOURCE> create(int containers, RESOURCE emptyType) {
        return new AttachedResources<>(NonNullList.withSize(containers, new LargeResourceStack<>(emptyType, 0)));
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