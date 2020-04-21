package mekanism.common.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

//TODO: JavaDocs
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CapabilityCache {

    private final Map<Capability<?>, ICapabilityResolver> capabilityResolvers = new HashMap<>();
    /**
     * List of unique resolvers to make invalidating all easier as some resolvers (energy) may support multiple capabilities.
     */
    private final List<ICapabilityResolver> uniqueResolvers = new ArrayList<>();
    //TODO: Use these
    private final Set<Capability<?>> alwaysDisabled = new HashSet<>();
    private final Map<Capability<?>, List<BooleanSupplier>> semiDisabled = new HashMap<>();
    private TileComponentConfig config;

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

    public void addDisabledCapabilities(Capability<?>... capabilities) {
        Collections.addAll(alwaysDisabled, capabilities);
    }

    public void addDisabledCapabilities(Collection<Capability<?>> capabilities) {
        alwaysDisabled.addAll(capabilities);
    }

    public void addSemiDisabledCapability(Capability<?> capability, BooleanSupplier checker) {
        //TODO: Note about the fact it uses a list so that it can properly also allow parents to separately disable the same capability
        semiDisabled.computeIfAbsent(capability, cap -> new ArrayList<>()).add(checker);
    }

    public void addConfigComponent(TileComponentConfig config) {
        if (this.config != null) {
            Mekanism.logger.warn("Config component already registered. Overriding");
        }
        this.config = config;
    }

    public boolean isCapabilityDisabled(Capability<?> capability, @Nullable Direction side) {
        //TODO: Note in the java docs that the capability does not have to be one that we support
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

    public boolean canResolve(Capability<?> capability) {
        return capabilityResolvers.containsKey(capability);
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!isCapabilityDisabled(capability, side) && canResolve(capability)) {
            return getCapabilityUnchecked(capability, side);
        }
        return LazyOptional.empty();
    }

    //TODO: Note that this is called unchecked as it does not check if the capability is disabled
    public <T> LazyOptional<T> getCapabilityUnchecked(Capability<T> capability, @Nullable Direction side) {
        ICapabilityResolver capabilityResolver = capabilityResolvers.get(capability);
        if (capabilityResolver == null) {
            return LazyOptional.empty();
        }
        return capabilityResolver.resolve(capability, side);
    }

    //TODO: Call this when needed, such as when configurable sides change
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