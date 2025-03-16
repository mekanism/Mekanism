package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

@NothingNullByDefault
public class DataComponentDeferredRegister extends MekanismDeferredRegister<DataComponentType<?>> {

    public DataComponentDeferredRegister(String namespace) {
        super(Registries.DATA_COMPONENT_TYPE, namespace);
    }

    public <TYPE> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<TYPE>> simple(String name, UnaryOperator<DataComponentType.Builder<TYPE>> operator) {
        return register(name, () -> operator.apply(DataComponentType.builder()).build());
    }

    public <FREQ extends Frequency> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FrequencyAware<FREQ>>> registerFrequencyAware(String name,
          Supplier<FrequencyType<FREQ>> frequencyTypeSupplier) {
        return simple(name, builder -> {
            FrequencyType<FREQ> frequencyType = frequencyTypeSupplier.get();
            return builder.persistent(FrequencyAware.codec(frequencyType))
                  .networkSynchronized(FrequencyAware.streamCodec(frequencyType));
        });
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> registerBoolean(String name) {
        return simple(name, builder -> builder.persistent(Codec.BOOL)
              .networkSynchronized(ByteBufCodecs.BOOL));
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> registerNonNegativeInt(String name) {
        return simple(name, builder -> builder.persistent(ExtraCodecs.POSITIVE_INT)
              .networkSynchronized(ByteBufCodecs.VAR_INT));
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> registerInt(String name) {
        return simple(name, builder -> builder.persistent(Codec.INT)
              .networkSynchronized(ByteBufCodecs.VAR_INT));
    }

    @SuppressWarnings("removal")
    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> registerNonNegativeLong(String name) {
        return simple(name, builder -> builder.persistent(SerializerHelper.POSITIVE_LONG_CODEC_LEGACY)
              .networkSynchronized(ByteBufCodecs.VAR_LONG));
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<UUID>> registerUUID(String name) {
        return simple(name, builder -> builder.persistent(UUIDUtil.CODEC)
              .networkSynchronized(UUIDUtil.STREAM_CODEC));
    }

    public MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Component>> registerComponent(String name) {
        //Copied from DataComponents.CUSTOM_NAME and ITEM_NAME
        return simple(name, builder -> builder.persistent(ComponentSerialization.FLAT_CODEC)
              .networkSynchronized(ComponentSerialization.STREAM_CODEC)
              .cacheEncoding());
    }

    public <TYPE> MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<TYPE>>> registerResourceKey(String name,
          ResourceKey<? extends Registry<TYPE>> registryKey) {
        return simple(name, builder -> builder.persistent(ResourceKey.codec(registryKey))
              .networkSynchronized(ResourceKey.streamCodec(registryKey)));
    }
}