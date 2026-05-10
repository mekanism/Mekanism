package mekanism.api.energy;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public interface IMekanismStrictEnergyHandler extends IStrictEnergyHandler, IContentsListener {

    List<IEnergyContainer> getContainers();

    @Nullable
    default IEnergyContainer getContainer(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        //TODO - 26.1: Should we make this throw instead of return null when invalid? That means it would propagate the exception times that resource handler defines
        List<IEnergyContainer> containers = getContainers();
        return index >= 0 && index < containers.size() ? containers.get(index) : null;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int size() {
        return getContainers().size();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.getEnergy();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.getMaxEnergy();
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    default long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction,
          AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.insert(amount, transaction, automationType);
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    default long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
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
    @Range(from = 0, to = Long.MAX_VALUE)
    default long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return insert(index, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return insert(amount, transaction, AutomationType.INTERNAL);
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    default long extract(int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        IEnergyContainer container = getContainer(index);
        return container == null ? 0 : container.extract(amount, transaction, automationType);
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    default long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
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
    @Range(from = 0, to = Long.MAX_VALUE)
    default long extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return extract(index, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return extract(amount, transaction, AutomationType.INTERNAL);
    }


    @Override
    default void setEnergy(@Range(from = 0, to = Integer.MAX_VALUE) int container, @Range(from = 0, to = Long.MAX_VALUE) long energy) {
        MekanismPreconditions.checkNonNegative(energy);
        IEnergyContainer energyContainer = getContainer(container);
        if (energyContainer != null) {
            energyContainer.setEnergy(energy);
        }
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeededEnergy(@Range(from = 0, to = Integer.MAX_VALUE) int container) {
        IEnergyContainer energyContainer = getContainer(container);
        return energyContainer == null ? 0L : energyContainer.getNeeded();
    }
}