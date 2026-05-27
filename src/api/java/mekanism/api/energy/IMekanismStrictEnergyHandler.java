package mekanism.api.energy;

import java.util.List;
import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

@FunctionalInterface
@NothingNullByDefault
public interface IMekanismStrictEnergyHandler extends IStrictEnergyHandler {

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
        return insert(index, amount, transaction, defaultAutomationType());
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return insert(amount, transaction, defaultAutomationType());
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
        return extract(index, amount, transaction, defaultAutomationType());
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return extract(amount, transaction, defaultAutomationType());
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

    /// Determines which automation type methods defined via [ResourceHandler] methods will use.
    private AutomationType defaultAutomationType() {
        //TODO - 26.1: Should this fallback for insert and extract use internal or external as the automation type?
        // I think it used to fall back to internal due to technically being the null side, but I think external makes more sense
        return AutomationType.EXTERNAL;
    }
}