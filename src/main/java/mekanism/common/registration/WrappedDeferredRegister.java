package mekanism.common.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

//TODO: Make it so we can have "custom" WrapperRegistryObject's easier that add QoL stuff that the enums currently add in the subclasses of this
public class WrappedDeferredRegister<T extends IForgeRegistryEntry<T>> {

    protected final DeferredRegister<T> internal;

    public WrappedDeferredRegister(String modid, IForgeRegistry<T> registry) {
        internal = new DeferredRegister<>(registry, modid);
    }

    public <I extends T, W extends WrappedRegistryObject<I>> W register(String name, Supplier<? extends I> sup, Function<RegistryObject<I>, W> objectWrapper) {
        return objectWrapper.apply(internal.register(name, sup));
    }

    public void register(IEventBus bus) {
        internal.register(bus);
    }
}