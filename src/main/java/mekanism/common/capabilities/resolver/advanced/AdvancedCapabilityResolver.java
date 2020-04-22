package mekanism.common.capabilities.resolver.advanced;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;

/**
 * Capability resolver for handling a read only variant, a generic unsided implementation variant.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedCapabilityResolver implements ICapabilityResolver {

    public static <T> AdvancedCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier, NonNullSupplier<T> readOnlySupplier) {
        return new AdvancedCapabilityResolver(supportedCapability, supplier, readOnlySupplier);
    }

    /**
     * Creates a capability resolver that strongly caches the result of the read only supplier. Persisting the calculated value through capability invalidation.
     */
    public static <T> AdvancedCapabilityResolver readOnly(Capability<T> supportedCapability, T value, NonNullSupplier<T> readOnlySupplier) {
        return create(supportedCapability, () -> value, NonNullLazy.of(readOnlySupplier));
    }

    private final List<Capability<?>> supportedCapability;
    private final NonNullSupplier<?> supplier;
    private final NonNullSupplier<?> readOnlySupplier;
    private LazyOptional<?> cachedCapability;
    private LazyOptional<?> cachedReadOnlyCapability;

    protected <T> AdvancedCapabilityResolver(Capability<T> supportedCapability, NonNullSupplier<T> supplier, NonNullSupplier<T> readOnlySupplier) {
        this.supportedCapability = Collections.singletonList(supportedCapability);
        this.supplier = supplier;
        this.readOnlySupplier = readOnlySupplier;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (side == null) {
            if (cachedReadOnlyCapability == null || !cachedReadOnlyCapability.isPresent()) {
                //If the capability has not been retrieved yet or it is not valid then recreate it
                cachedReadOnlyCapability = LazyOptional.of(readOnlySupplier);
            }
            return cachedReadOnlyCapability.cast();
        }
        if (cachedCapability == null || !cachedCapability.isPresent()) {
            //If the capability has not been retrieved yet or it is not valid then recreate it
            cachedCapability = LazyOptional.of(supplier);
        }
        return cachedCapability.cast();
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        if (side == null) {
            invalidateReadOnly();
        } else {
            invalidate();
        }
    }

    @Override
    public void invalidateAll() {
        invalidate();
        invalidateReadOnly();
    }

    private void invalidateReadOnly() {
        if (cachedReadOnlyCapability != null && cachedReadOnlyCapability.isPresent()) {
            cachedReadOnlyCapability.invalidate();
            cachedReadOnlyCapability = null;
        }
    }

    private void invalidate() {
        if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
            cachedCapability = null;
        }
    }
}