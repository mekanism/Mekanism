package mekanism.common.attachments.containers.energy;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.LongTransferUtils;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedEnergyHandler extends ComponentBackedHandler<Long, IEnergyContainer, AttachedEnergy> implements IMekanismStrictEnergyHandler {

    public ComponentBackedEnergyHandler(ItemStack attachedTo, int totalContainers) {
        super(attachedTo, totalContainers);
    }

    @Override
    protected ContainerType<IEnergyContainer, AttachedEnergy, ?> containerType() {
        return ContainerType.ENERGY;
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return getContainers();
    }

    @Nullable
    @Override
    public IEnergyContainer getEnergyContainer(int container, @Nullable Direction side) {
        return getContainer(container);
    }

    @Override
    public int getEnergyContainerCount(@Nullable Direction side) {
        return size();
    }

    @Override
    public long getEnergy(int container, @Nullable Direction side) {
        return getContents(container);
    }

    @Override
    public long insertEnergy(long amount, @Nullable Direction side, Action action) {
        return LongTransferUtils.insert(amount, action, AutomationType.handler(side), size(), this);
    }

    @Override
    public long extractEnergy(long amount, @Nullable Direction side, Action action) {
        return LongTransferUtils.extract(amount, action, AutomationType.handler(side), size(), this);
    }
}