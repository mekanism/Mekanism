package mekanism.common.registration;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

public class WrappedDatapackDeferredRegister<T> extends WrappedDeferredRegister<Codec<? extends T>> {

    private final ResourceKey<Registry<T>> datapackRegistryName;
    private final String modid;

    protected WrappedDatapackDeferredRegister(String modid, ResourceKey<? extends Registry<Codec<? extends T>>> serializerRegistryName,
          ResourceKey<Registry<T>> datapackRegistryName) {
        super(modid, serializerRegistryName);
        this.modid = modid;
        this.datapackRegistryName = datapackRegistryName;
    }

    /**
     * Only call this from mekanism and for custom datapack registries
     */
    public void createAndRegisterDatapack(IEventBus bus, Codec<T> directCodec, @Nullable Codec<T> networkCodec) {
        register(bus);
        //Create a new datapack registry using the direct codec that is created based on the serializer's codec
        bus.addListener((DataPackRegistryEvent.NewRegistry event) -> event.dataPackRegistry(datapackRegistryName, directCodec, networkCodec));
    }

    public ResourceKey<T> dataKey(String name) {
        return ResourceKey.create(datapackRegistryName, new ResourceLocation(modid, name));
    }
}