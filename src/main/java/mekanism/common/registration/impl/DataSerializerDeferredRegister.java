package mekanism.common.registration.impl;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class DataSerializerDeferredRegister extends WrappedDeferredRegister<EntityDataSerializer<?>> {

    public DataSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS);
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

    public <T> DataSerializerRegistryObject<T> register(String name, Supplier<EntityDataSerializer<T>> sup) {
        return register(name, sup, DataSerializerRegistryObject::new);
    }
}