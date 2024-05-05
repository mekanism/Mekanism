package mekanism.common.registration.impl;

import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DataSerializerDeferredRegister extends MekanismDeferredRegister<EntityDataSerializer<?>> {

    public DataSerializerDeferredRegister(String modid) {
        super(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, modid);
    }

    public <T> MekanismDeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<T>> register(String name, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        return register(name, () -> EntityDataSerializer.forValueType(codec));
    }

    public <T> MekanismDeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<ResourceKey<T>>> register(String name, ResourceKey<? extends Registry<T>> registryName) {
        return register(name, ResourceLocation.STREAM_CODEC.map(rl -> ResourceKey.create(registryName, rl), ResourceKey::location));
    }
}