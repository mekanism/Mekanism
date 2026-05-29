package mekanism.api.energy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Range;

@FunctionalInterface
@NothingNullByDefault
public interface IMekanismEnergyHandler extends EnergyHandler {

    IEnergyContainer getEnergyContainer();

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getAmountAsLong() {
        return getEnergyContainer().energy();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getCapacityAsLong() {
        return getEnergyContainer().capacity();
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        return getEnergyContainer().insert(amount, transaction, automationType);
    }

    @Override
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return insert(amount, transaction, defaultAutomationType());
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    default int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        return getEnergyContainer().extract(amount, transaction, automationType);
    }

    @Override
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return extract(amount, transaction, defaultAutomationType());
    }

    /// Determines which automation type methods defined via [ResourceHandler] methods will use.
    private AutomationType defaultAutomationType() {
        //TODO - 26.1: Should this fallback for insert and extract use internal or external as the automation type?
        // I think it used to fall back to internal due to technically being the null side, but I think external makes more sense
        return AutomationType.EXTERNAL;
    }
}