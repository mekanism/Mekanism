package mekanism.common.registration;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DoubleDeferredRegister<PRIMARY, SECONDARY> {

    private final DeferredRegister<PRIMARY> primaryRegister;
    private final DeferredRegister<SECONDARY> secondaryRegister;

    protected DoubleDeferredRegister(DeferredRegister<PRIMARY> primaryRegistry, DeferredRegister<SECONDARY> secondaryRegistry) {
        this.primaryRegister = primaryRegistry;
        this.secondaryRegister = secondaryRegistry;
    }

    protected DoubleDeferredRegister(String modid, ResourceKey<? extends Registry<PRIMARY>> primaryRegistryName,
          ResourceKey<? extends Registry<SECONDARY>> secondaryRegistryName) {
        this(modid, primaryRegistryName, DeferredRegister.create(secondaryRegistryName, modid));
    }

    protected DoubleDeferredRegister(String modid, ResourceKey<? extends Registry<PRIMARY>> primaryRegistryName, DeferredRegister<SECONDARY> secondaryRegistry) {
        this(DeferredRegister.create(primaryRegistryName, modid), secondaryRegistry);
    }

    public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W register(String name,
          Supplier<? extends P> primarySupplier, Supplier<? extends S> secondarySupplier, BiFunction<DeferredHolder<PRIMARY, P>,
          DeferredHolder<SECONDARY, S>, W> objectWrapper) {
        return objectWrapper.apply(primaryRegister.register(name, primarySupplier), secondaryRegister.register(name, secondarySupplier));
    }

    public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W register(String name,
          Supplier<? extends P> primarySupplier, Function<P, S> secondarySupplier, BiFunction<DeferredHolder<PRIMARY, P>,
          DeferredHolder<SECONDARY, S>, W> objectWrapper) {
        return registerAdvanced(name, primarySupplier, secondarySupplier.compose(Supplier::get), objectWrapper);
    }

    public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W registerAdvanced(String name,
          Supplier<? extends P> primarySupplier, Function<DeferredHolder<PRIMARY, P>, S> secondarySupplier, BiFunction<DeferredHolder<PRIMARY, P>,
          DeferredHolder<SECONDARY, S>, W> objectWrapper) {
        DeferredHolder<PRIMARY, P> primaryObject = primaryRegister.register(name, primarySupplier);
        return objectWrapper.apply(primaryObject, secondaryRegister.register(name, () -> secondarySupplier.apply(primaryObject)));
    }

    public void register(IEventBus bus) {
        primaryRegister.register(bus);
        secondaryRegister.register(bus);
    }

    public Collection<DeferredHolder<PRIMARY, ? extends PRIMARY>> getPrimaryEntries() {
        return primaryRegister.getEntries();
    }

    public Collection<DeferredHolder<SECONDARY, ? extends SECONDARY>> getSecondaryEntries() {
        return secondaryRegister.getEntries();
    }
}