package mekanism.common.capabilities.resolver.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.capabilities.resolver.EnergyCapabilityResolver;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyHandlerManager implements ICapabilityHandlerManager<IEnergyContainer> {

    private final Map<Direction, Map<Capability<?>, LazyOptional<?>>> cachedCapabilities;
    private final Map<Capability<?>, LazyOptional<?>> cachedReadOnlyCapabilities;
    private final Map<Direction, IStrictEnergyHandler> handlers;
    private final ISidedStrictEnergyHandler baseHandler;
    private final boolean canHandle;
    @Nullable
    private IStrictEnergyHandler readOnlyHandler;
    @Nullable
    private final IEnergyContainerHolder holder;

    public EnergyHandlerManager(@Nullable IEnergyContainerHolder holder, ISidedStrictEnergyHandler baseHandler) {
        this.holder = holder;
        this.canHandle = this.holder != null;
        this.baseHandler = baseHandler;
        if (this.canHandle) {
            handlers = new EnumMap<>(Direction.class);
            cachedCapabilities = new EnumMap<>(Direction.class);
            cachedReadOnlyCapabilities = new HashMap<>();
        } else {
            handlers = Collections.emptyMap();
            cachedCapabilities = Collections.emptyMap();
            cachedReadOnlyCapabilities = Collections.emptyMap();
        }
    }

    @Override
    public boolean canHandle() {
        return canHandle;
    }

    @Override
    public List<IEnergyContainer> getContainers(@Nullable Direction side) {
        return canHandle() ? holder.getEnergyContainers(side) : Collections.emptyList();
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return EnergyCompatUtils.getEnabledEnergyCapabilities();
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     *
     * @apiNote Assumes that {@link #canHandle} has been called before this and that it was {@code true}.
     */
    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (getContainers(side).isEmpty()) {
            //If we don't have any containers accessible from that side, don't return a handler
            //TODO: Evaluate moving this somehow into being done via the is disabled check
            return LazyOptional.empty();
        }
        if (side == null) {
            if (readOnlyHandler == null) {
                //Note: Only should enter this if statement if we don't already have a cache,
                // so we just check it beforehand as it is a quick check and simplifies the code
                readOnlyHandler = new ProxyStrictEnergyHandler(baseHandler, null, holder);
            }
            return EnergyCapabilityResolver.getCachedOrResolve(capability, cachedReadOnlyCapabilities, readOnlyHandler);
        }
        //Note: Only should enter this if statement if we don't already have a cache,
        // so we just check it beforehand as it is a quick check and simplifies the code
        IStrictEnergyHandler handler = handlers.computeIfAbsent(side, s -> new ProxyStrictEnergyHandler(baseHandler, s, holder));
        return EnergyCapabilityResolver.getCachedOrResolve(capability, cachedCapabilities.computeIfAbsent(side, key -> new HashMap<>()), handler);
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        //Note: We don't invalidate the base handlers as they are still valid regardless, and the holder just removes slots when they shouldn't be accessible
        // we only bother invalidating the lazy optionals
        if (side == null) {
            invalidate(cachedReadOnlyCapabilities.get(capability));
        } else {
            Map<Capability<?>, LazyOptional<?>> cachedSide = cachedCapabilities.get(side);
            if (cachedSide != null) {
                invalidate(cachedSide.get(capability));
            }
        }
    }

    @Override
    public void invalidateAll() {
        //Note: We don't invalidate the base handlers as they are still valid regardless, and the holder just removes slots when they shouldn't be accessible
        // we only bother invalidating the lazy optionals
        for (Map<Capability<?>, LazyOptional<?>> cachedSide : cachedCapabilities.values()) {
            for (LazyOptional<?> lazyOptional : new ArrayList<>(cachedSide.values())) {
                invalidate(lazyOptional);
            }
        }
        for (LazyOptional<?> lazyOptional : new ArrayList<>(cachedReadOnlyCapabilities.values())) {
            invalidate(lazyOptional);
        }
    }

    protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
        if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
        }
    }
}