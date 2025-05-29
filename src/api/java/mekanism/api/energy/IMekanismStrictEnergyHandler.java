package mekanism.api.energy;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.LongTransferUtils;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismStrictEnergyHandler extends ISidedStrictEnergyHandler, IContentsListener {

    /**
     * Used to check if an instance of {@link IMekanismStrictEnergyHandler} actually has the ability to handle energy.
     *
     * @return True if we are actually capable of handling energy.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismStrictEnergyHandler} without having retrieved the object via the strict energy handler capability,
     * then you must call this method to make sure that it really can handle energy. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleEnergy() {
        return true;
    }

    /**
     * Returns the list of IEnergyContainers that this energy handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IEnergyContainers that this {@link IMekanismStrictEnergyHandler} contains for the given side. If there are no containers for the side or
     * {@link #canHandleEnergy()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all containers in the handler. Additionally, if {@link #canHandleEnergy()} is
     * false, this <em>MUST</em> return an empty list.
     */
    List<IEnergyContainer> getEnergyContainers(@Nullable Direction side);

    /**
     * Returns the {@link IEnergyContainer} that has the given index from the list of containers on the given side.
     *
     * @param container The index of the container to retrieve.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IEnergyContainer} that has the given index from the list of containers on the given side.
     */
    @Nullable
    default IEnergyContainer getEnergyContainer(int container, @Nullable Direction side) {
        List<IEnergyContainer> containers = getEnergyContainers(side);
        return container >= 0 && container < containers.size() ? containers.get(container) : null;
    }

    @Override
    default int getEnergyContainerCount(@Nullable Direction side) {
        return getEnergyContainers(side).size();
    }

    @Override
    default long getEnergy(int container, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0L : energyContainer.getEnergy();
    }

    @Override
    default void setEnergy(int container, long energy, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        if (energyContainer != null) {
            energyContainer.setEnergy(Math.max(0, energy));
        }
    }

    @Override
    default long getMaxEnergy(int container, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0L : energyContainer.getMaxEnergy();
    }

    @Override
    default long getNeededEnergy(int container, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0L : energyContainer.getNeeded();
    }

    /**
     * @implNote Any overrides to this should also override {@link ISidedStrictEnergyHandler#insertEnergy(long, Direction, Action)} as it bypasses calling this method in
     * order to skip looking up the containers for every sub operation.
     */
    @Override
    default long insertEnergy(int container, long amount, @Nullable Direction side, Action action) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? amount : energyContainer.insert(amount, action, AutomationType.handler(side));
    }

    /**
     * @implNote Any overrides to this should also override {@link ISidedStrictEnergyHandler#extractEnergy(long, Direction, Action)} as it bypasses calling this method in
     * order to skip looking up the containers for every sub operation.
     */
    @Override
    default long extractEnergy(int container, long amount, @Nullable Direction side, Action action) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0L : energyContainer.extract(amount, action, AutomationType.handler(side));
    }

    @Override
    default long insertEnergy(long amount, @Nullable Direction side, Action action) {
        return LongTransferUtils.insert(amount, side, this::getEnergyContainers, action, AutomationType.handler(side));
    }

    @Override
    default long extractEnergy(long amount, @Nullable Direction side, Action action) {
        return LongTransferUtils.extract(amount, side, this::getEnergyContainers, action, AutomationType.handler(side));
    }
}