package mekanism.common.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class WrappedDeferredRegister<T> {

    protected final DeferredRegister<T> internal;

    protected WrappedDeferredRegister(DeferredRegister<T> internal) {
        this.internal = internal;
    }

    protected WrappedDeferredRegister(String modid, IForgeRegistry<T> registry) {
        this(DeferredRegister.create(registry, modid));
    }

    /**
     * @apiNote For use with vanilla or custom registries
     */
    protected WrappedDeferredRegister(String modid, ResourceKey<? extends Registry<T>> registryName) {
        this(DeferredRegister.create(registryName, modid));
    }

    protected <I extends T, W extends WrappedRegistryObject<I>> W register(String name, Supplier<? extends I> sup, Function<RegistryObject<I>, W> objectWrapper) {
        return objectWrapper.apply(internal.register(name, sup));
    }

    public void register(IEventBus bus) {
        internal.register(bus);
    }

    /**
     * Only call this from mekanism and for custom registries
     */
    public void createAndRegister(IEventBus bus) {
        createAndRegister(bus, UnaryOperator.identity());
    }

    /**
     * Only call this from mekanism and for custom chemical registries
     */
    public void createAndRegisterChemical(IEventBus bus) {
        createAndRegister(bus, builder -> builder.hasTags().setDefaultKey(Mekanism.rl("empty")));
    }

    /**
     * Only call this from mekanism and for custom registries
     */
    public void createAndRegister(IEventBus bus, UnaryOperator<RegistryBuilder<T>> builder) {
        internal.makeRegistry(() -> builder.apply(new RegistryBuilder<>()));
        register(bus);
    }
}