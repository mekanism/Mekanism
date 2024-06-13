package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

@NothingNullByDefault
public final class DataMapTypeRegister {
    private final String namespace;

    public DataMapTypeRegister(String namespace) {
        this.namespace = namespace;
    }

    private final List<DataMapType<?, ?>> types = new ArrayList<>();

    public <R, T> DataMapType<R, T> register(String name, ResourceKey<Registry<R>> registryKey, Codec<T> codec, UnaryOperator<DataMapType.Builder<T, R>> builder) {
        final DataMapType<R, T> type = builder.apply(DataMapType.builder(ResourceLocation.fromNamespaceAndPath(namespace, name), registryKey, codec)).build();
        this.types.add(type);
        return type;
    }

    public <R, T> DataMapType<R, T> registerSimple(String name, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
        return register(name, registryKey, codec, UnaryOperator.identity());
    }

    public void register(IEventBus bus) {
        bus.addListener(RegisterDataMapTypesEvent.class, event -> types.forEach(event::register));
    }
}
