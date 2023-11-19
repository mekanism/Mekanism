package mekanism.common.registration.impl;

import java.util.function.UnaryOperator;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class DataSerializerDeferredRegister extends MekanismDeferredRegister<EntityDataSerializer<?>> {

    public DataSerializerDeferredRegister(String modid) {
        super(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, modid);
    }

    public <T extends Enum<T>> MekanismDeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<T>> registerEnum(String name, Class<T> enumClass) {
        return register(name, () -> EntityDataSerializer.simpleEnum(enumClass));
    }

    public <T> MekanismDeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<T>> registerSimple(String name, FriendlyByteBuf.Writer<T> writer,
          FriendlyByteBuf.Reader<T> reader) {
        return register(name, () -> EntityDataSerializer.simple(writer, reader));
    }

    public <T> MekanismDeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<ResourceKey<T>>> register(String name,
          ResourceKey<? extends Registry<T>> registryName) {
        return registerSimple(name, FriendlyByteBuf::writeResourceKey, buf -> buf.readResourceKey(registryName));
    }

    public <T> MekanismDeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<T>> register(String name, FriendlyByteBuf.Writer<T> writer,
          FriendlyByteBuf.Reader<T> reader, UnaryOperator<T> copier) {
        return register(name, () -> new EntityDataSerializer<>() {
            @Override
            public void write(@NotNull FriendlyByteBuf buffer, @NotNull T value) {
                writer.accept(buffer, value);
            }

            @NotNull
            @Override
            public T read(@NotNull FriendlyByteBuf buffer) {
                return reader.apply(buffer);
            }

            @NotNull
            @Override
            public T copy(@NotNull T value) {
                return copier.apply(value);
            }
        });
    }
}