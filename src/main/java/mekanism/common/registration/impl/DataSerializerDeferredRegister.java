package mekanism.common.registration.impl;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedForgeDeferredRegister;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.ForgeRegistries;

public class DataSerializerDeferredRegister extends WrappedForgeDeferredRegister<DataSerializerEntry> {

    public DataSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.Keys.DATA_SERIALIZERS);
    }

    public <T extends Enum<T>> DataSerializerRegistryObject<T> registerEnum(String name, Class<T> enumClass) {
        return registerSimple(name, FriendlyByteBuf::writeEnum, buffer -> buffer.readEnum(enumClass));
    }

    public <T> DataSerializerRegistryObject<T> registerSimple(String name, BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        return register(name, writer, reader, UnaryOperator.identity());
    }

    public <T> DataSerializerRegistryObject<T> register(String name, BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader, UnaryOperator<T> copier) {
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
        return register(name, () -> new DataSerializerEntry(sup.get()), DataSerializerRegistryObject::new);
    }
}