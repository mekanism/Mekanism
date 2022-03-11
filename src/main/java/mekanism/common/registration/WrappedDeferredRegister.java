package mekanism.common.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class WrappedDeferredRegister<T extends IForgeRegistryEntry<T>> {

    protected final DeferredRegister<T> internal;

    protected WrappedDeferredRegister(String modid, IForgeRegistry<T> registry) {
        internal = DeferredRegister.create(registry, modid);
    }

    /**
     * @apiNote For use with custom registries
     */
    protected WrappedDeferredRegister(String modid, ResourceKey<? extends Registry<T>> registryName) {
        internal = DeferredRegister.create(registryName, modid);
    }

    protected <I extends T, W extends WrappedRegistryObject<I>> W register(String name, Supplier<? extends I> sup, Function<RegistryObject<I>, W> objectWrapper) {
        return objectWrapper.apply(internal.register(name, sup));
    }

    /**
     * Only call this from mekanism and for custom registries
     */
    public void createAndRegister(IEventBus bus, Class<T> type) {
        createAndRegister(bus, type, UnaryOperator.identity());
    }

    /**
     * Only call this from mekanism and for custom registries
     */
    public void createAndRegister(IEventBus bus, Class<T> type, UnaryOperator<RegistryBuilder<T>> builder) {
        internal.makeRegistry(type, () -> builder.apply(new RegistryBuilder<>()));
        register(bus);
    }

    public void register(IEventBus bus) {
        internal.register(bus);
    }
}