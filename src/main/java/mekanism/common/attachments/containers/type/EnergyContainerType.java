package mekanism.common.attachments.containers.type;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
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
            return new ComponentBackedEnergyHandler(this, itemAccess);
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
        //TODO - 26.1: Evaluate how the resistive heater component is handled
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
}