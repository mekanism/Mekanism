package mekanism.common.capabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

//TODO: JavaDocs
@ParametersAreNonnullByDefault
public class CapabilityCache {

    //TODO: Allow for keeping track of capability managers via the capability cache?
    private final Map<Capability<?>, ICapabilityResolver> capabilityResolvers = new HashMap<>();
    /**
     * List of unique resolvers to make invalidating all easier as some resolvers (energy) may support multiple capabilities.
     */
    private final List<ICapabilityResolver> uniqueResolvers = new ArrayList<>();

    public void addCapabilityResolver(ICapabilityResolver resolver) {
        //TODO: Do we want to validate if the resolver has already been added.
        uniqueResolvers.add(resolver);
        List<Capability<?>> supportedCapabilities = resolver.getSupportedCapabilities();
        for (Capability<?> supportedCapability : supportedCapabilities) {
            if (capabilityResolvers.put(supportedCapability, resolver) != null) {
                Mekanism.logger.warn("Multiple capability resolvers registered for {}. Overriding", supportedCapability);
            }
        }
    }

    //TODO: Do we want ones that can only be resolved on specific sides?
    public boolean canResolve(Capability<?> capability) {
        return capabilityResolvers.containsKey(capability);
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (canResolve(capability)) {
            return getCapabilityUnchecked(capability, side);
        }
        return LazyOptional.empty();
    }

    //TODO: Decide on if this really should be unchecked given it *partially* checks it by checking the existence
    // though we are not checking that it is not "disabled"
    public <T> LazyOptional<T> getCapabilityUnchecked(Capability<T> capability, @Nullable Direction side) {
        ICapabilityResolver capabilityResolver = capabilityResolvers.get(capability);
        if (capabilityResolver == null) {
            return LazyOptional.empty();
        }
        return capabilityResolver.resolve(capability, side);
    }

    //TODO: Actually invalidate capabilities when they should be invalidated, including properly handling toggleable capabilities
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        ICapabilityResolver capabilityResolver = capabilityResolvers.get(capability);
        if (capabilityResolver != null) {
            capabilityResolver.invalidate(capability, side);
        }
    }

    public void invalidateAll() {
        uniqueResolvers.forEach(ICapabilityResolver::invalidateAll);
    }
}