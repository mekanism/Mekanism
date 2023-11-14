package mekanism.common.capabilities.resolver;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.util.NonNullLazy;
import net.neoforged.neoforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicCapabilityResolver<CAPABILITY, CONTEXT> implements ICapabilityResolver<CONTEXT> {

    public static <CAPABILITY, CONTEXT> BasicCapabilityResolver<CAPABILITY, CONTEXT> create(BlockCapability<CAPABILITY, CONTEXT> supportedCapability,
          NonNullSupplier<CAPABILITY> supplier) {
        return new BasicCapabilityResolver<>(supportedCapability, supplier);
    }

    /**
     * Creates a capability resolver that strongly caches the result of the supplier. Persisting the calculated value through capability invalidation.
     */
    public static <CAPABILITY, CONTEXT> BasicCapabilityResolver<CAPABILITY, CONTEXT> persistent(BlockCapability<CAPABILITY, CONTEXT> supportedCapability,
          NonNullSupplier<CAPABILITY> supplier) {
        return create(supportedCapability, supplier instanceof NonNullLazy ? supplier : NonNullLazy.of(supplier));
    }

    /**
     * Creates a capability resolver of a constant value. Usually {@code this} for tiles.
     */
    public static <CAPABILITY, CONTEXT> BasicCapabilityResolver<CAPABILITY, CONTEXT> constant(BlockCapability<CAPABILITY, CONTEXT> supportedCapability,
          CAPABILITY value) {//TODO: Do we want to remove this?
        return create(supportedCapability, () -> value);
    }

    private final List<BlockCapability<?, CONTEXT>> supportedCapabilities;
    private final NonNullSupplier<CAPABILITY> supplier;
    @Nullable
    private CAPABILITY cachedCapability;

    protected BasicCapabilityResolver(BlockCapability<CAPABILITY, CONTEXT> capabilityType, NonNullSupplier<CAPABILITY> supplier) {
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
        //TODO: Fix invalidating this
        return (T) cachedCapability;
    }

    @Override
    public void invalidate(BlockCapability<?, CONTEXT> capability, CONTEXT side) {
        //We only have one capability so just invalidate everything
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        cachedCapability = null;
    }
}