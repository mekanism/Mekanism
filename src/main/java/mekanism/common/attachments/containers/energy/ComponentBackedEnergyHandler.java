package mekanism.common.attachments.containers.energy;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongTransferUtils;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedEnergyHandler extends ComponentBackedHandler<FloatingLong, IEnergyContainer, AttachedEnergy> implements IMekanismStrictEnergyHandler {

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
        return containerCount();
    }

    @Override
    public FloatingLong getEnergy(int container, @Nullable Direction side) {
        return getContents(container);
    }

    @Override
    public FloatingLong insertEnergy(FloatingLong amount, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the energy containers as necessary/we actually get to iterating against them?
        return FloatingLongTransferUtils.insert(amount, side, this::getEnergyContainers, action, AutomationType.handler(side));
    }

    @Override
    public FloatingLong extractEnergy(FloatingLong amount, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the energy containers as necessary/we actually get to iterating against them?
        return FloatingLongTransferUtils.extract(amount, side, this::getEnergyContainers, action, AutomationType.handler(side));
    }
}