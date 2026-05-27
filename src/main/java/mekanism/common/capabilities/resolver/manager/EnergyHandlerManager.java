package mekanism.common.capabilities.resolver.manager;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.capabilities.resolver.BasicSidedCapabilityResolver.ProxyCreator;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.LastEnergyTracker;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class EnergyHandlerManager implements ICapabilityHandlerManager<IEnergyContainer> {

    private final Map<Direction, Map<BlockCapability<?, @Nullable Direction>, Object>> cachedCapabilities = new EnumMap<>(Direction.class);
    private final Map<BlockCapability<?, @Nullable Direction>, Object> cachedReadOnlyCapabilities = new IdentityHashMap<>();
    private final LastEnergyTracker lastEnergyTracker;
    private final Map<Direction, IStrictEnergyHandler> handlers = new EnumMap<>(Direction.class);
    private final ProxyCreator<IContainerHolder<IEnergyContainer>, IStrictEnergyHandler> proxyCreator;
    @Nullable
    private IStrictEnergyHandler readOnlyHandler;
    private final IContainerHolder<IEnergyContainer> holder;

    public EnergyHandlerManager(IContainerHolder<IEnergyContainer> holder, LongSupplier gameTimeSupplier) {
        this.holder = holder;
        this.lastEnergyTracker = new LastEnergyTracker(gameTimeSupplier);
        this.proxyCreator = (side, energyHolder) -> new ProxyStrictEnergyHandler(new IMekanismStrictEnergyHandler() {
            @Override
            public List<IEnergyContainer> getContainers() {
                return energyHolder.getContainers(side);
            }

            @Override
            @Range(from = 0, to = Long.MAX_VALUE)
            public long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction,
                  AutomationType automationType) {
                long inserted = IMekanismStrictEnergyHandler.super.insert(index, amount, transaction, automationType);
                lastEnergyTracker.received(inserted, transaction);
                return inserted;
            }

            @Override
            @Range(from = 0, to = Long.MAX_VALUE)
            public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
                //Note: Super bypasses calling insert(int container, ...) so we need to override it here as well
                long inserted = IMekanismStrictEnergyHandler.super.insert(amount, transaction, automationType);
                lastEnergyTracker.received(inserted, transaction);
                return inserted;
            }
        }, side, energyHolder);
    }

    public LastEnergyTracker getLastEnergyTracker() {
        return lastEnergyTracker;
    }

    @Override
    public List<IEnergyContainer> getContainers(@Nullable Direction side) {
        return holder.getContainers(side);
    }

    @Override
    public List<BlockCapability<?, @Nullable Direction>> getSupportedCapabilities() {
        //Return all loaded energy caps so that we get attached and then just handle config checks in the actual lookup
        return EnergyCompatUtils.getLoadedEnergyCapabilities();
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
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
                    readOnlyHandler = proxyCreator.create(null, holder);
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
                handler = proxyCreator.create(side, holder);
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