package mekanism.common.capabilities.resolver;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicCapabilityResolver implements ICapabilityResolver {

    public static <T> BasicCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        return new BasicCapabilityResolver(supportedCapability, supplier);
    }

    /**
     * Creates a capability resolver that strongly caches the result of the supplier. Persisting the calculated value through capability invalidation.
     */
    public static <T> BasicCapabilityResolver persistent(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        return create(supportedCapability, NonNullLazy.of(supplier));
    }

    /**
     * Creates a capability resolver of a constant value. Usually {@code this} for tiles.
     */
    public static <T> BasicCapabilityResolver constant(Capability<T> supportedCapability, T value) {
        return create(supportedCapability, () -> value);
    }

    private final List<Capability<?>> supportedCapability;
    private final NonNullSupplier<?> supplier;
    private LazyOptional<?> cachedCapability;

    protected <T> BasicCapabilityResolver(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        this.supportedCapability = Collections.singletonList(supportedCapability);
        this.supplier = supplier;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (cachedCapability == null || !cachedCapability.isPresent()) {
            //If the capability has not been retrieved yet or it is not valid then recreate it
            cachedCapability = LazyOptional.of(supplier);
        }
        return cachedCapability.cast();
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        //We only have one capability so just invalidate everything
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
            cachedCapability = null;
        }
    }
}