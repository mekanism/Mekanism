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

    private final Map<Capability<?>, ICapabilityResolver> capabilityResolvers = new HashMap<>();
    private final Map<Capability<?>, LazyOptional<?>> cachedCapabilities = new HashMap<>();

    public void addCapabilityResolver(ICapabilityResolver resolver) {
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

    public <T> LazyOptional<T> getCapabilityUnchecked(Capability<T> capability, @Nullable Direction side) {
        //TODO: FIX Sidedness handling, currently we don't take the side into account for our cached value
        // We will want to determine some way to define if we care about the side and how many sides we actually do
        // care about. So that we can re-use the lazy optionals as much as possible
        if (cachedCapabilities.containsKey(capability)) {
            //If we already contain a cached object for this lazy optional then get it and use it
            LazyOptional<?> cachedCapability = cachedCapabilities.get(capability);
            if (cachedCapability.isPresent()) {
                //If the capability is no longer present (not valid), then re-retrieve it
                return cachedCapability.cast();
            }
        }
        LazyOptional<T> uncachedCapability = capabilityResolvers.get(capability).resolve(capability, side);
        cachedCapabilities.put(capability, uncachedCapability);
        return uncachedCapability;
    }

    public void invalidate(Capability<?> capability) {
        if (cachedCapabilities.containsKey(capability)) {
            invalidate(cachedCapabilities.get(capability));
        }
    }

    //TODO: Actually invalidate capabilities when they should be invalidated, including properly handling toggleable capabilities
    public void invalidateAll() {
        List<LazyOptional<?>> toInvalidate = new ArrayList<>(cachedCapabilities.values());
        toInvalidate.forEach(this::invalidate);
        //Note: We don't bother clearing cachedCapabilities as the elements will be ignored and re-retrieved anyways
        // and this makes it less likely there will be some concurrent modification exception
    }

    private void invalidate(LazyOptional<?> cachedCapability) {
        if (cachedCapability.isPresent()) {
            //If it hasn't already been invalidated invalidate it
            cachedCapability.invalidate();
        }
    }
}