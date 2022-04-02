package mekanism.common.registration;

import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class WrappedForgeDeferredRegister<T extends IForgeRegistryEntry<T>> extends WrappedDeferredRegister<T> {

    protected WrappedForgeDeferredRegister(String modid, IForgeRegistry<T> registry) {
        super(DeferredRegister.create(registry, modid));
    }

    /**
     * @apiNote For use with custom registries
     */
    protected WrappedForgeDeferredRegister(String modid, ResourceKey<? extends Registry<T>> registryName) {
        super(modid, registryName);
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
}