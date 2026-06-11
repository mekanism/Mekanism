package mekanism.common.capabilities.resolver;

import java.util.List;
import java.util.function.Supplier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;

public class BasicCapabilityResolver<CAPABILITY, CONTEXT extends @Nullable Object> implements ICapabilityResolver<CONTEXT> {

    public static <CAPABILITY, CONTEXT extends @Nullable Object> BasicCapabilityResolver<CAPABILITY, CONTEXT> create(BlockCapability<CAPABILITY, CONTEXT> supportedCapability,
          Supplier<CAPABILITY> supplier) {
        return new BasicCapabilityResolver<>(supportedCapability, supplier);
    }

    /// Creates a capability resolver that strongly caches the result of the supplier. Persisting the calculated value through capability invalidation.
    public static <CAPABILITY, CONTEXT extends @Nullable Object> BasicCapabilityResolver<CAPABILITY, CONTEXT> persistent(BlockCapability<CAPABILITY, CONTEXT> supportedCapability,
          Supplier<CAPABILITY> supplier) {
        return create(supportedCapability, supplier instanceof Lazy ? supplier : Lazy.of(supplier));
    }

    private final List<BlockCapability<?, CONTEXT>> supportedCapabilities;
    private final Supplier<CAPABILITY> supplier;
    @Nullable
    private CAPABILITY cachedCapability;

    protected BasicCapabilityResolver(BlockCapability<CAPABILITY, CONTEXT> capabilityType, Supplier<CAPABILITY> supplier) {
        this.supportedCapabilities = List.of(capabilityType);
        this.supplier = supplier;
    }

    @Override
    public List<BlockCapability<?, CONTEXT>> getSupportedCapabilities() {
        return supportedCapabilities;
    }

    @Nullable
    @Override
    public <T> T resolve(BlockCapability<T, CONTEXT> capability, CONTEXT context) {
        if (cachedCapability == null) {
            //If the capability has not been retrieved yet, or it is not valid then recreate it
            cachedCapability = supplier.get();
        }
        return (T) cachedCapability;
    }

    @Override
    public void invalidate(BlockCapability<?, CONTEXT> capability, CONTEXT side) {
        cachedCapability = null;
    }

    @Override
    public void invalidateAll() {
        cachedCapability = null;
    }
}