package mekanism.common.capabilities.resolver.manager;

import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.proxy.ProxyEnergyHandler;
import mekanism.common.capabilities.resolver.BasicSidedCapabilityResolver;
import mekanism.common.lib.LastEnergyTracker;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class EnergyHandlerManager extends BasicSidedCapabilityResolver<IEnergyContainerHolder, EnergyHandler> {

    private final IEnergyContainerHolder holder;
    private final LastEnergyTracker lastEnergyTracker;

    public EnergyHandlerManager(IEnergyContainerHolder holder, LongSupplier gameTimeSupplier) {
        LastEnergyTracker lastEnergyTracker = new LastEnergyTracker(gameTimeSupplier);
        super(holder, Capabilities.ENERGY.block(), (side, energyHolder) -> new TrackingEnergyHandler(side, energyHolder, lastEnergyTracker));
        this.holder = holder;
        this.lastEnergyTracker = lastEnergyTracker;
    }

    @Nullable
    public IEnergyContainer getContainer(@Nullable Direction side) {
        return holder.getContainer(side);
    }

    public LastEnergyTracker getLastEnergyTracker() {
        return lastEnergyTracker;
    }

    private static class TrackingEnergyHandler extends ProxyEnergyHandler {

        private final LastEnergyTracker lastEnergyTracker;

        public TrackingEnergyHandler(@Nullable Direction side, IEnergyContainerHolder holder, LastEnergyTracker lastEnergyTracker) {
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