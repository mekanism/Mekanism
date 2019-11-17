package mekanism.common.registration;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class WrappedRegistryObject<T extends IForgeRegistryEntry<? super T>> implements Supplier<T> {

    private final RegistryObject<T> registryObject;

    public WrappedRegistryObject(RegistryObject<T> registryObject) {
        this.registryObject = registryObject;
    }

    //TODO: Should this be nullable?? the registryObject.get is. We should handle the fact that extenders of this previously thought it is nonnull
    @Nullable
    @Override
    public T get() {
        return registryObject.get();
    }

    public RegistryObject<T> getInternal() {
        return registryObject;
    }
}