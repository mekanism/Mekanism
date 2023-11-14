package mekanism.common.capabilities;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class CapabilityCache {//TODO - 1.20.2: Is this fine for the identity and reference maps?

    private final Map<BlockCapability<?, @Nullable Direction>, ICapabilityResolver<@Nullable Direction>> capabilityResolvers = new IdentityHashMap<>();
    /**
     * List of unique resolvers to make invalidating all easier as some resolvers (energy) may support multiple capabilities.
     */
    private final List<ICapabilityResolver<?>> uniqueResolvers = new ArrayList<>();
    private final Set<BlockCapability<?, @Nullable Direction>> alwaysDisabled = new ReferenceOpenHashSet<>();
    private final Map<BlockCapability<?, @Nullable Direction>, List<BooleanSupplier>> semiDisabled = new IdentityHashMap<>();
    @Nullable
    private TileComponentConfig config;

    /**
     * Adds a capability resolver to the list of resolvers for this cache.
     */
    public void addCapabilityResolver(ICapabilityResolver<@Nullable Direction> resolver) {
        uniqueResolvers.add(resolver);
        List<BlockCapability<?, @Nullable Direction>> supportedCapabilities = resolver.getSupportedCapabilities();
        for (BlockCapability<?, @Nullable Direction> supportedCapability : supportedCapabilities) {
            //Note: We add the capability regardless of if it is registered as we will just short circuit and always disable the capability
            // if it isn't in use by the time the capability is queried. In theory, we shouldn't ever be getting created before the capabilities
            // have been registered, but just in case we ensure it works properly
            if (capabilityResolvers.put(supportedCapability, resolver) != null) {
                Mekanism.logger.warn("Multiple capability resolvers registered for {}. Overriding", supportedCapability.name(), new Exception());
            }
        }
    }

    /**
     * Marks all the given capabilities as always being disabled.
     */
    @SafeVarargs
    public final void addDisabledCapabilities(BlockCapability<?, @Nullable Direction>... capabilities) {
        Collections.addAll(alwaysDisabled, capabilities);
    }

    /**
     * Marks all the given capabilities as always being disabled.
     */
    public void addDisabledCapabilities(Collection<BlockCapability<?, @Nullable Direction>> capabilities) {
        alwaysDisabled.addAll(capabilities);
    }

    /**
     * Marks the given capability as having a check for sometimes being disabled.
     *
     * @implNote These "semi disabled" checks are stored in a list so that children can define more cases a capability should be disabled than the ones the parent already
     * wants them to be disabled in.
     */
    public void addSemiDisabledCapability(BlockCapability<?, @Nullable Direction> capability, BooleanSupplier checker) {
        semiDisabled.computeIfAbsent(capability, cap -> new ArrayList<>()).add(checker);
    }

    /**
     * Adds the given config component for use in checking if capabilities are disabled on a specific side.
     */
    public void addConfigComponent(TileComponentConfig config) {
        if (this.config != null) {
            Mekanism.logger.warn("Config component already registered. Overriding", new Exception());
        }
        this.config = config;
    }

    /**
     * Checks if the given capability is disabled for the specific side.
     *
     * @return {@code true} if the capability is disabled, {@code false} otherwise.
     */
    public boolean isCapabilityDisabled(BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        if (alwaysDisabled.contains(capability)) {
            return true;
        }
        if (semiDisabled.containsKey(capability)) {
            List<BooleanSupplier> predicates = semiDisabled.get(capability);
            for (BooleanSupplier predicate : predicates) {
                if (predicate.getAsBoolean()) {
                    return true;
                }
            }
        }
        if (config == null) {
            return false;
        }
        return config.isCapabilityDisabled(capability, side);
    }

    @Nullable
    public ICapabilityResolver<@Nullable Direction> getResolver(BlockCapability<?, @Nullable Direction> capability) {
        return capabilityResolvers.get(capability);
    }

    public ICapabilityResolver<@Nullable Direction> getResolver(BlockCapability<?, @Nullable Direction> capability,
          Supplier<ICapabilityResolver<@Nullable Direction>> resolver) {
        return capabilityResolvers.computeIfAbsent(capability, c -> resolver.get());
    }

    /**
     * Invalidates the given capability on the given side.
     *
     * @param capability Capability
     * @param side       Side
     */
    public void invalidate(BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        ICapabilityResolver<@Nullable Direction> capabilityResolver = capabilityResolvers.get(capability);
        if (capabilityResolver != null) {
            capabilityResolver.invalidate(capability, side);
        }
    }

    /**
     * Invalidates the given capability on the given sides.
     *
     * @param capability Capability
     * @param sides      Sides
     */
    public void invalidateSides(BlockCapability<?, @Nullable Direction> capability, Direction... sides) {
        ICapabilityResolver<@Nullable Direction> capabilityResolver = capabilityResolvers.get(capability);
        if (capabilityResolver != null) {
            for (Direction side : sides) {
                capabilityResolver.invalidate(capability, side);
            }
        }
    }

    /**
     * Invalidates all cached capabilities.
     */
    public void invalidateAll() {
        uniqueResolvers.forEach(ICapabilityResolver::invalidateAll);
    }
}