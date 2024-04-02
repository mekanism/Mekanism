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
        //Return all loaded energy caps so that we get attached and then just handle config checks in the actual lookup
        return EnergyCompatUtils.getLoadedEnergyCapabilities();
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     *
     * @apiNote Assumes that {@link #canHandle} has been called before this and that it was {@code true}.
     */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolve(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (getContainers(side).isEmpty()) {
            //If we don't have any containers accessible from that side, don't return a handler
            return null;
        }
        if (side == null) {
            //If we already contain a cached object for this instance then just use it, otherwise wrap it
            Object result = cachedReadOnlyCapabilities.get(capability);
            if (result == null) {
                if (readOnlyHandler == null) {
                    //We haven't initiated the backing handler yet, so we need to calculate it
                    readOnlyHandler = new ProxyStrictEnergyHandler(baseHandler, null, holder);
                }
                result = EnergyCompatUtils.wrapStrictEnergyHandler(capability, readOnlyHandler);
                cachedReadOnlyCapabilities.put(capability, result);
            }
            return (T) result;
        }
        //If we already contain a cached object for this instance then just use it, otherwise calculate the base handler and wrap it
        Map<BlockCapability<?, @Nullable Direction>, Object> cache = cachedCapabilities.computeIfAbsent(side, key -> new IdentityHashMap<>());
        Object result = cache.get(capability);
        if (result == null) {
            //If we haven't initiated the backing handler yet, calculate it
            IStrictEnergyHandler handler = handlers.get(side);
            if (handler == null) {
                handler = new ProxyStrictEnergyHandler(baseHandler, side, holder);
                handlers.put(side, handler);
            }
            result = EnergyCompatUtils.wrapStrictEnergyHandler(capability, handler);
            cache.put(capability, result);
        }
        return (T) result;
    }

    @Override
    public void invalidate(BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        //Note: We don't invalidate the base handlers as they are still valid regardless and are just wrappers with the holder
        // removing slots when they shouldn't be accessible, so we only invalidate the cached exposed handlers
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
        //Note: We don't invalidate the base handlers as they are still valid regardless and are just wrappers with the holder
        // removing slots when they shouldn't be accessible, so we only invalidate the cached exposed handlers
        cachedCapabilities.clear();
        cachedReadOnlyCapabilities.clear();
    }
}