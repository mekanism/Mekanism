package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.SerializationConstants;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.FrequencyAware;
import mekanism.common.component.containers.resource.AttachedResources;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.transfer.resource.Resource;

//TODO - 26.2: Re-evaluate our usages of cacheEncoding. Neo's wiki states: cacheEncoding caches the encoding result of the Codec such that any subsequent encodes
// uses the cached value if the component value hasn't changed. This should only be used if the component value is expected to rarely or never change.
//TODO - 26.2: Use ignoreSwapAnimation() for some of our components, most notably probably energy storage
public class DataComponentDeferredRegister extends MekanismDeferredRegister<DataComponentType<?>> {

    public DataComponentDeferredRegister(String namespace) {
        super(Registries.DATA_COMPONENT_TYPE, namespace);
    }

    public <TYPE> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<TYPE>> simple(String name, UnaryOperator<DataComponentType.Builder<TYPE>> operator) {
        return register(name, () -> operator.apply(DataComponentType.builder()).build());
    }

    public <TYPE> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<TYPE>> simple(String name, Codec<TYPE> codec,
          StreamCodec<? super RegistryFriendlyByteBuf, TYPE> streamCodec) {
        return simple(name, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
    }

    public <FREQ extends Frequency> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<FREQ>>> registerFrequencyAware(String name,
          Supplier<FrequencyType<FREQ>> frequencyTypeSupplier) {
        return simple(name, builder -> {
            FrequencyType<FREQ> frequencyType = frequencyTypeSupplier.get();
            return builder.persistent(FrequencyAware.codec(frequencyType))
                  .networkSynchronized(FrequencyAware.streamCodec(frequencyType));
        });
    }

    public <RESOURCE extends Resource> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<AttachedResources<RESOURCE>>> registerAttachedContents(String name,
          LargeResourceStack.StackHelper<RESOURCE> stackHelper) {
        return simple(name, builder -> builder.persistent(
              RecordCodecBuilder.create(instance -> instance.group(
                    stackHelper.orEmptyCodec().listOf().fieldOf(SerializationConstants.CONTAINERS).forGetter(AttachedResources::containers)
              ).apply(instance, AttachedResources::new))
        ).networkSynchronized(stackHelper.streamCodec()
              .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>, List<LargeResourceStack<RESOURCE>>>collection(NonNullList::createWithCapacity))
              .map(AttachedResources::new, AttachedResources::containers)
        ).cacheEncoding());
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Unit>> registerUnit(String name) {
        return simple(name, Unit.CODEC, Unit.STREAM_CODEC);
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> registerBoolean(String name) {
        return simple(name, Codec.BOOL, ByteBufCodecs.BOOL);
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> registerNonNegativeInt(String name) {
        return simple(name, ExtraCodecs.POSITIVE_INT, ByteBufCodecs.VAR_INT);
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> registerInt(String name) {
        return simple(name, Codec.INT, ByteBufCodecs.VAR_INT);
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> registerNonNegativeLong(String name) {
        return simple(name, ExtraCodecs.NON_NEGATIVE_LONG, ByteBufCodecs.VAR_LONG);
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> registerUUID(String name) {
        return simple(name, UUIDUtil.CODEC, UUIDUtil.STREAM_CODEC);
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Component>> registerComponent(String name) {
        //Copied from DataComponents.CUSTOM_NAME and ITEM_NAME
        return simple(name, builder -> builder.persistent(ComponentSerialization.CODEC)
              .networkSynchronized(ComponentSerialization.STREAM_CODEC)
              .cacheEncoding());
    }

    public <TYPE> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<TYPE>>> registerResourceKey(String name,
          ResourceKey<? extends Registry<TYPE>> registryKey) {
        return simple(name, ResourceKey.codec(registryKey), ResourceKey.streamCodec(registryKey));
    }
}