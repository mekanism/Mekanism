package mekanism.common.attachments.containers.type;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public final class EnergyContainerType extends CapableContainerType<IEnergyContainer, Long, EnergyHandler> implements ISingleContainerType<IEnergyContainer, Long> {

    EnergyContainerType() {
        super(MekanismDataComponents.ATTACHED_ENERGY, SerializationConstants.ENERGY_CONTAINERS, Capabilities.ENERGY, 0L);
    }

    @Nullable
    @Override
    protected EnergyHandler createHandler(ItemAccess itemAccess) {
        ItemResource resource = itemAccess.getResource();
        if (supports(resource)) {
            //Note: All our energy handlers that we expose on items, currently validate the backing item type just like Neo's ItemAccessEnergyHandler does.
            // If it is desired to skip that check similar to ItemAccessResourceHandler, such as because we have a handler that changes between item instances
            // similar to a bucket, then we just need to adjust this to pass false in those cases to the handler.
            return new ComponentBackedEnergyHandler(this, itemAccess, true);
        }
        return null;
    }

    @Override
    public @Nullable IEnergyContainer getContainer(TileEntityMekanism tile) {
        return tile.getEnergyContainer();
    }

    @Override
    public void copyToContainer(IEnergyContainer container, Long stored) {
        container.setEnergy(stored, null);
    }

    @Override
    public Long attachedCopyOf(IEnergyContainer container) {
        return container.getAmountAsLong();
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return tile.canHandleEnergy();
    }

    @Override
    public void copy(IEnergyContainer from, IEnergyContainer to) {
        to.copyContents(from);
    }

    //TODO - 26.1: Evaluate what other spots should be clamped
    public void clampContents(IEnergyContainer container, @Nullable TransactionContext transaction) {
        if (!container.isEmpty()) {
            long capacity = container.getCapacityAsLong();
            if (capacity == 0 && container instanceof VariableCapacityEnergyContainer) {
                //Our capacity should never actually be zero, and given we fake it being zero
                // until we finish building the network, we need to override this method to bypass the upper limit check
                // when our upper limit is zero
                return;
            }
            if (container.getAmountAsLong() > capacity) {
                container.setEnergy(capacity, transaction);
            }
        }
    }
}