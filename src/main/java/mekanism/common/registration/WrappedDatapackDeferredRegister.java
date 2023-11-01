package mekanism.common.registration;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

public class WrappedDatapackDeferredRegister<T> extends WrappedDeferredRegister<Codec<? extends T>> {

    protected final ResourceKey<Registry<T>> datapackRegistryName;
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
    public Codec<T> createAndRegisterDatapack(IEventBus bus, Function<? super T, Codec<? extends T>> baseCodec) {
        return createAndRegisterDatapack(bus, baseCodec, null);
    }

    /**
     * Only call this from mekanism and for custom datapack registries
     */
    public Codec<T> createAndRegisterDatapack(IEventBus bus, Function<? super T, Codec<? extends T>> baseCodec, @Nullable Codec<T> networkCodec) {
        //Create the register for the serializers and mark they don't need to be persisted or sync'd
        Supplier<IForgeRegistry<Codec<? extends T>>> serializerRegistry = createAndRegister(bus, builder -> builder.disableSaving().disableSync());
        Codec<T> directCodec = ExtraCodecs.lazyInitializedCodec(() -> serializerRegistry.get().getCodec())
              .dispatch(baseCodec, Function.identity());
        //Create a new datapack registry using the direct codec that is created based on the serializer's codec
        bus.addListener((DataPackRegistryEvent.NewRegistry event) -> event.dataPackRegistry(datapackRegistryName, directCodec, networkCodec));
        return directCodec;
    }

    public ResourceKey<T> dataKey(String name) {
        return ResourceKey.create(datapackRegistryName, new ResourceLocation(modid, name));
    }
}