package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@NothingNullByDefault
public final class DataMapTypeRegister {

    private final String namespace;

    public DataMapTypeRegister(String namespace) {
        this.namespace = namespace;
    }

    private final List<DataMapType<?, ?>> types = new ArrayList<>();

    public <R, T> DataMapType<R, T> register(ResourceLocation name, ResourceKey<Registry<R>> registryKey, Codec<T> codec, UnaryOperator<DataMapType.Builder<T, R>> builder) {
        if (name.getNamespace().equals(namespace)) {
            final DataMapType<R, T> type = builder.apply(DataMapType.builder(name, registryKey, codec)).build();
            this.types.add(type);
            return type;
        }
        throw new IllegalArgumentException("Trying to register data map type with the wrong namespace. Expected: '" + namespace + "', but received: '" +
                                           name.getNamespace() + "'");
    }

    public <R, T> DataMapType<R, T> registerSynced(ResourceLocation name, ResourceKey<Registry<R>> registryKey, Codec<T> codec, Codec<T> networkCodec) {
        return register(name, registryKey, codec, builder -> builder.synced(networkCodec, true));
    }

    public <R, T> DataMapType<R, T> registerSimpleSynced(ResourceLocation name, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
        return registerSynced(name, registryKey, codec, codec);
    }

    public <R, T> DataMapType<R, T> registerSimple(ResourceLocation name, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
        return register(name, registryKey, codec, UnaryOperator.identity());
    }

    public void register(IEventBus bus) {
        bus.addListener(RegisterDataMapTypesEvent.class, event -> types.forEach(event::register));
    }
}
