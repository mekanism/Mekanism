package mekanism.api.energy;

import java.util.List;
import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public interface IMekanismStrictEnergyHandler extends IStrictEnergyHandler, IContentsListener {

    List<IEnergyContainer> getContainers();

    default IEnergyContainer getContainer(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        List<IEnergyContainer> containers = getContainers();
        Objects.checkIndex(index, containers.size());
        return containers.get(index);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int size() {
        return getContainers().size();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return getContainer(index).energy();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return getContainer(index).capacity();
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    default long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction,
          AutomationType automationType) {
        return getContainer(index).insert(amount, transaction, automationType);
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
        return getContainer(index).extract(amount, transaction, automationType);
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
    default void setEnergy(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long energy) {
        getContainer(index).setEnergy(energy);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeededEnergy(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return getContainer(index).getNeeded();
    }

    @Override
    default boolean isEmpty() {
        for (IEnergyContainer container : getContainers()) {
            if (!container.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}