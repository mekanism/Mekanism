package mekanism.api.energy;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismStrictEnergyHandler extends IStrictEnergyHandler, IContentsListener {

    List<IEnergyContainer> getContainers();

    @Nullable
    default IEnergyContainer getContainer(int index) {
        //TODO - 26.1: Should we make this throw instead of return null when invalid? That means it would propagate the exception times that resource handler defines
        List<IEnergyContainer> containers = getContainers();
        return index >= 0 && index < containers.size() ? containers.get(index) : null;
    }

    @Override
    default int size() {
        return getContainers().size();
    }

    @Override
    default long getAmountAsLong(int index) {
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.getEnergy();
    }

    @Override
    default long getCapacityAsLong(int index) {
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.getMaxEnergy();
    }

    default long insert(int index, long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.insert(amount, transaction, automationType);
    }

    default long insert(long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        long inserted = 0;
        for (IEnergyContainer container : getContainers()) {
            inserted += container.insert(amount - inserted, transaction, automationType);
            if (inserted == amount) {
                break;
            }
        }
        return inserted;
    }

    @Override
    default long insert(int index, long amount, TransactionContext transaction) {
        return insert(index, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    default long insert(long amount, TransactionContext transaction) {
        return insert(amount, transaction, AutomationType.INTERNAL);
    }

    default long extract(int index, long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.extract(amount, transaction, automationType);
    }

    default long extract(long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        long extracted = 0;
        for (IEnergyContainer container : getContainers()) {
            extracted += container.extract(amount - extracted, transaction, automationType);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }

    @Override
    default long extract(int index, long amount, TransactionContext transaction) {
        return extract(index, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    default long extract(long amount, TransactionContext transaction) {
        return extract(amount, transaction, AutomationType.INTERNAL);
    }


    @Override
    default void setEnergy(int container, long energy) {
        IEnergyContainer energyContainer = getContainer(container);
        if (energyContainer != null) {
            energyContainer.setEnergy(Math.max(0, energy));
        }
    }

    @Override
    default long getNeededEnergy(int container) {
        IEnergyContainer energyContainer = getContainer(container);
        return energyContainer == null ? 0L : energyContainer.getNeeded();
    }
}