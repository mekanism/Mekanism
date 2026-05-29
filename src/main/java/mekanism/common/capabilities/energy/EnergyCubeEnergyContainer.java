package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.IntSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.tier.EnergyCubeTier;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class EnergyCubeEnergyContainer extends BasicEnergyContainer {

    public static EnergyCubeEnergyContainer create(EnergyCubeTier tier, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tier, "Energy cube tier cannot be null");
        return new EnergyCubeEnergyContainer(tier, listener);
    }

    private final boolean isCreative;
    private final IntSupplier rate;

    private EnergyCubeEnergyContainer(EnergyCubeTier tier, @Nullable IContentsListener listener) {
        super(tier.getCapacity(), ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), listener);
        isCreative = tier == EnergyCubeTier.CREATIVE;
        rate = tier::getTransferRate;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getInsertionRate(AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType.isInternal() ? rate.getAsInt() : super.getInsertionRate(automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getExtractionRate(AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType.isInternal() ? rate.getAsInt() : super.getExtractionRate(automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        //Note: Unlike other creative items, the creative energy cube does not allow changing it to always full
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes)
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(amount, simulation, automationType);
            }
        }
        return super.insert(amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.extract(amount, simulation, automationType);
            }
        }
        return super.extract(amount, transaction, automationType);
    }
}