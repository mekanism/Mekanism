package mekanism.common.capabilities.resolver.manager;

import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyHandlerManager implements ICapabilityHandlerManager<IEnergyContainer> {

    private final Map<Direction, Map<BlockCapability<?, @Nullable Direction>, Object>> cachedCapabilities;
    private final Map<BlockCapability<?, @Nullable Direction>, Object> cachedReadOnlyCapabilities;
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
            cachedReadOnlyCapabilities = new IdentityHashMap<>();
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
    public List<BlockCapability<?, @Nullable Direction>> getSupportedCapabilities() {
        return EnergyCompatUtils.getEnabledEnergyCapabilities();
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     *
     * @apiNote Assumes that {@link #canHandle} has been called before this and that it was {@code true}.
     */
    @Nullable
    @Override
    public <T> T resolve(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (getContainers(side).isEmpty()) {
            //If we don't have any containers accessible from that side, don't return a handler
            //TODO: Evaluate moving this somehow into being done via the is disabled check
            return null;
        }
        if (side == null) {
            if (readOnlyHandler == null) {
                //Note: Only should enter this if statement, if we don't already have a cache,
                // so we just check it beforehand as it is a quick check and simplifies the code
                readOnlyHandler = new ProxyStrictEnergyHandler(baseHandler, null, holder);
            }
            return getCachedOrResolve(capability, cachedReadOnlyCapabilities, readOnlyHandler);
        }
        //Note: Only should enter this if statement, if we don't already have a cache,
        // so we just check it beforehand as it is a quick check and simplifies the code
        IStrictEnergyHandler handler = handlers.computeIfAbsent(side, s -> new ProxyStrictEnergyHandler(baseHandler, s, holder));
        return getCachedOrResolve(capability, cachedCapabilities.computeIfAbsent(side, key -> new IdentityHashMap<>()), handler);
    }

    @Override
    public void invalidate(BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        //Note: We don't invalidate the base handlers as they are still valid regardless, and the holder just removes slots when they shouldn't be accessible
        // we only bother invalidating the lazy optionals
        if (side == null) {
            cachedReadOnlyCapabilities.remove(capability);
        } else {
            Map<BlockCapability<?, @Nullable Direction>, ?> cachedSide = cachedCapabilities.get(side);
            if (cachedSide != null) {
                cachedSide.remove(capability);
            }
        }
    }

    @Override
    public void invalidateAll() {
        //Note: We don't invalidate the base handlers as they are still valid regardless, and the holder just removes slots when they shouldn't be accessible
        // we only bother invalidating the lazy optionals
        for (Map<BlockCapability<?, @Nullable Direction>, ?> cachedSide : cachedCapabilities.values()) {
            cachedSide.clear();
        }
        cachedReadOnlyCapabilities.clear();
    }

    @Nullable
    public static <T> T getCachedOrResolve(BlockCapability<T, @Nullable Direction> capability, Map<BlockCapability<?, @Nullable Direction>, Object> cachedCapabilities,
          IStrictEnergyHandler handler) {
        //If we already contain a cached object for this instance then just use it, otherwise wrap it
        return (T) cachedCapabilities.computeIfAbsent(capability, cap -> EnergyCompatUtils.wrapStrictEnergyHandler(capability, handler));
    }
}