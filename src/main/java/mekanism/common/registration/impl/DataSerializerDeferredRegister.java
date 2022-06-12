package mekanism.common.registration.impl;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class DataSerializerDeferredRegister extends WrappedDeferredRegister<EntityDataSerializer<?>> {

    public DataSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.Keys.DATA_SERIALIZERS);
    }

    public <T extends Enum<T>> DataSerializerRegistryObject<T> registerEnum(String name, Class<T> enumClass) {
        return register(name, () -> EntityDataSerializer.simpleEnum(enumClass));
    }

    public <T> DataSerializerRegistryObject<T> registerSimple(String name, FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader) {
        return register(name, () -> EntityDataSerializer.simple(writer, reader));
    }

    public <T> DataSerializerRegistryObject<T> register(String name, FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader, UnaryOperator<T> copier) {
        return register(name, () -> new EntityDataSerializer<>() {
            @Override
            public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull T value) {
                writer.accept(buffer, value);
            }

            @Nonnull
            @Override
            public T read(@Nonnull FriendlyByteBuf buffer) {
                return reader.apply(buffer);
            }

            @Nonnull
            @Override
            public T copy(@Nonnull T value) {
                return copier.apply(value);
            }
        });
    }

    public <T> DataSerializerRegistryObject<T> register(String name, Supplier<EntityDataSerializer<T>> sup) {
        return register(name, sup, DataSerializerRegistryObject::new);
    }
}