package mekanism.common.attachments.containers.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedEnergyHandler extends ComponentBackedHandler<Long, IEnergyContainer, AttachedEnergy> implements IMekanismStrictEnergyHandler {

    public ComponentBackedEnergyHandler(ContainerType<IEnergyContainer, AttachedEnergy, ? extends ComponentBackedEnergyHandler> containerType, ItemStack attachedTo, int totalContainers) {
        super(containerType, attachedTo, totalContainers);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int container) {//TODO - 26.1: Evaluate if there are any other methods we need to be overriding
        //Override to just return the stored contents to avoid having to hook up the container just to read the stored amount
        return getContents(container);
    }
}