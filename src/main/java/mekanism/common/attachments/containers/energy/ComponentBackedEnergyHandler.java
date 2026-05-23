package mekanism.common.attachments.containers.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Range;

//TODO - 26.1: Look at ItemAccessResourceHandler, and make it so that it returns proper values for when the access is empty
@NothingNullByDefault
public class ComponentBackedEnergyHandler extends ComponentBackedHandler<Long, IEnergyContainer, AttachedEnergy> implements IMekanismStrictEnergyHandler {

    public ComponentBackedEnergyHandler(ContainerType<IEnergyContainer, AttachedEnergy, ? extends ComponentBackedEnergyHandler> containerType, ItemAccess attachedAccess,
          int totalContainers) {
        super(containerType, attachedAccess, totalContainers);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int container) {//TODO - 26.1: Evaluate if there are any other methods we need to be overriding
        //Override to just return the stored contents to avoid having to hook up the container just to read the stored amount
        return getContents(container);
    }
}