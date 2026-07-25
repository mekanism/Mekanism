package mekanism.common.registration;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jspecify.annotations.Nullable;

public class AdvancedDatapackDeferredRegister<T, SERIALIZER> extends MekanismDeferredRegister<SERIALIZER> {

    public static AdvancedDatapackDeferredRegister<Chemical, ChemicalSerializer> chemicals(String modid) {
        return new AdvancedDatapackDeferredRegister<>(modid, MekanismRegistries.Keys.CHEMICAL_SERIALIZERS, MekanismRegistries.Keys.CHEMICAL);
    }

    private final ResourceKey<Registry<T>> datapackRegistryName;

    public AdvancedDatapackDeferredRegister(String modid, ResourceKey<? extends Registry<SERIALIZER>> serializerRegistryName, ResourceKey<Registry<T>> datapackRegistryName) {
        super(serializerRegistryName, modid);
        this.datapackRegistryName = datapackRegistryName;
    }

    /// Only call this from mekanism and for custom datapack registries
    public void createAndRegisterDatapack(IEventBus bus, Codec<T> directCodec, @Nullable Codec<T> networkCodec, Consumer<RegistryBuilder<T>> consumer) {
        register(bus);
        //Create a new datapack registry using the direct codec that is created based on the serializer's codec
        bus.addListener(DataPackRegistryEvent.NewRegistry.class, event -> event.dataPackRegistry(datapackRegistryName, directCodec, networkCodec, consumer));
    }

    public ResourceKey<T> dataKey(String name) {
        return ResourceKey.create(datapackRegistryName, Identifier.fromNamespaceAndPath(getNamespace(), name));
    }
}