package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import mekanism.api.datamaps.holderset.DataMapHolderSetMerger;
import mekanism.api.datamaps.holderset.DataMapHolderSetRemover;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class DataMapTypeRegister {

    private final String namespace;

    public DataMapTypeRegister(String namespace) {
        this.namespace = namespace;
    }

    private final List<DataMapType<?, ?>> types = new ArrayList<>();

    private void validateNameSpace(Identifier name) {
        if (!name.getNamespace().equals(namespace)) {
            throw new IllegalArgumentException("Trying to register data map type with the wrong namespace. Expected: '" + namespace + "', but received: '" + name.getNamespace() + "'");
        }
    }

    public <R, T> DataMapType<R, T> register(Identifier name, ResourceKey<Registry<R>> registryKey, Codec<T> codec, UnaryOperator<DataMapType.Builder<T, R>> builder) {
        validateNameSpace(name);
        final DataMapType<R, T> type = builder.apply(DataMapType.builder(name, registryKey, codec)).build();
        this.types.add(type);
        return type;
    }

    public <R, T, VR extends DataMapValueRemover<R, T>> AdvancedDataMapType<R, T, VR> registerAdvanced(Identifier name, ResourceKey<Registry<R>> registryKey, Codec<T> codec,
          Function<AdvancedDataMapType.Builder<T, R, DataMapValueRemover.Default<T, R>>, AdvancedDataMapType.Builder<T, R, VR>> builder) {
        validateNameSpace(name);
        final AdvancedDataMapType<R, T, VR> type = builder.apply(AdvancedDataMapType.builder(name, registryKey, codec)).build();
        this.types.add(type);
        return type;
    }

    public <R, TYPE> AdvancedDataMapType<R, HolderSet<TYPE>, DataMapHolderSetRemover<R, TYPE>> registerSyncedHolderSet(Identifier name, ResourceKey<Registry<R>> registryKey,
          ResourceKey<? extends Registry<TYPE>> holderRegistryKey, Codec<Holder<TYPE>> holderCodec) {
        return registerAdvanced(name, registryKey, HolderSetCodec.create(holderRegistryKey, holderCodec, false), builder -> builder
              //TODO - 26.2: Does this sync it flat?
              .synced(HolderSetCodec.create(holderRegistryKey, holderCodec, true), true)
              .merger(DataMapHolderSetMerger.instance())
              .remover(DataMapHolderSetRemover.codec(holderRegistryKey, holderCodec)));
    }

    public <R, T> DataMapType<R, T> registerSynced(Identifier name, ResourceKey<Registry<R>> registryKey, Codec<T> codec, Codec<T> networkCodec) {
        return register(name, registryKey, codec, builder -> builder.synced(networkCodec, true));
    }

    public <R, T> DataMapType<R, T> registerSimpleSynced(Identifier name, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
        return registerSynced(name, registryKey, codec, codec);
    }

    public <R, T> DataMapType<R, T> registerSimple(Identifier name, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
        return register(name, registryKey, codec, UnaryOperator.identity());
    }

    public void register(IEventBus bus) {
        bus.addListener(RegisterDataMapTypesEvent.class, event -> types.forEach(event::register));
    }
}
