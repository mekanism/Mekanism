package mekanism.common.capabilities.resolver.manager;

import java.util.function.LongSupplier;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.capabilities.proxy.ProxyEnergyHandler;
import mekanism.common.capabilities.resolver.BasicSingleContainerHandlerManager;
import mekanism.common.lib.transaction.LastEnergyTracker;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class EnergyHandlerManager extends BasicSingleContainerHandlerManager<IEnergyContainer, EnergyHandler> {

    private final LastEnergyTracker lastEnergyTracker;

    public EnergyHandlerManager(ISingleContainerHolder<IEnergyContainer> holder, LongSupplier gameTimeSupplier) {
        LastEnergyTracker lastEnergyTracker = new LastEnergyTracker(gameTimeSupplier);
        super(holder, Capabilities.ENERGY.block(), (side, energyHolder) -> new TrackingEnergyHandler(side, energyHolder, lastEnergyTracker));
        this.lastEnergyTracker = lastEnergyTracker;
    }

    public LastEnergyTracker getLastEnergyTracker() {
        return lastEnergyTracker;
    }

    private static class TrackingEnergyHandler extends ProxyEnergyHandler {

        private final LastEnergyTracker lastEnergyTracker;

        public TrackingEnergyHandler(@Nullable Direction side, ISingleContainerHolder<IEnergyContainer> holder, LastEnergyTracker lastEnergyTracker) {
            super(side, holder);
            this.lastEnergyTracker = lastEnergyTracker;
        }

        @Override
        @Range(from = 0, to = Integer.MAX_VALUE)
        public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
            //Note: Super bypasses calling insert(int container, ...) so we need to override it here as well
            int inserted = super.insert(amount, transaction);
            lastEnergyTracker.received(inserted, transaction);
            return inserted;
        }
    }
}