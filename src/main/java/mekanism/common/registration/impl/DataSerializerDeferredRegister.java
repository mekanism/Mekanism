package mekanism.common.registration.impl;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;

public class DataSerializerDeferredRegister extends WrappedDeferredRegister<DataSerializerEntry> {

    public DataSerializerDeferredRegister(String modid) {
        super(modid, DataSerializerEntry.class);
    }

    public <T> DataSerializerRegistryObject<T> registerSimple(String name, BiConsumer<PacketBuffer, T> writer, Function<PacketBuffer, T> reader) {
        return register(name, writer, reader, UnaryOperator.identity());
    }

    public <T> DataSerializerRegistryObject<T> register(String name, BiConsumer<PacketBuffer, T> writer, Function<PacketBuffer, T> reader, UnaryOperator<T> copier) {
        return register(name, () -> new IDataSerializer<T>() {
            @Override
            public void write(@Nonnull PacketBuffer buffer, @Nonnull T value) {
                writer.accept(buffer, value);
            }

            @Nonnull
            @Override
            public T read(@Nonnull PacketBuffer buffer) {
                return reader.apply(buffer);
            }

            @Nonnull
            @Override
            public T copy(@Nonnull T value) {
                return copier.apply(value);
            }
        });
    }

    public <T> DataSerializerRegistryObject<T> register(String name, Supplier<IDataSerializer<T>> sup) {
        return register(name, () -> new DataSerializerEntry(sup.get()), DataSerializerRegistryObject::new);
    }
}