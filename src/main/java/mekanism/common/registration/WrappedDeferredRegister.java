package mekanism.common.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class WrappedDeferredRegister<T extends IForgeRegistryEntry<T>> {

    protected final DeferredRegister<T> internal;

    public WrappedDeferredRegister(String modid, IForgeRegistry<T> registry) {
        internal = DeferredRegister.create(registry, modid);
    }

    /**
     * @apiNote For use with custom registries
     */
    public WrappedDeferredRegister(String modid, Class<T> base) {
        internal = DeferredRegister.create(base, modid);
    }

    public <I extends T, W extends WrappedRegistryObject<I>> W register(String name, Supplier<? extends I> sup, Function<RegistryObject<I>, W> objectWrapper) {
        return objectWrapper.apply(internal.register(name, sup));
    }

    /**
     * Only call this from mekanism and for custom registries
     */
    public void createAndRegister(IEventBus bus, String name) {
        internal.makeRegistry(name, RegistryBuilder::new);
        register(bus);
    }

    public void register(IEventBus bus) {
        internal.register(bus);
    }
}