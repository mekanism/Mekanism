package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.ITransactionHelper;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class EnergyCubeEnergyContainer extends BasicEnergyContainer {

    public static EnergyCubeEnergyContainer create(TileEntityEnergyCube tile, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Energy cube tile cannot be null");
        EnergyCubeTier tier = tile.getTier();
        LongSupplier gameTimeSupplier = MekanismUtils.getGameTimeSupplier(tile);
        IntSupplier rateLimit = tier::getTransferRate;
        //Only limit the internal rate to change the speed at which this can be filled or drained by an item stored in a slot
        return new EnergyCubeEnergyContainer(tier, ITransactionHelper.INSTANCE.createInternalOnlyRateLimit(gameTimeSupplier, rateLimit),
              ITransactionHelper.INSTANCE.createInternalOnlyRateLimit(gameTimeSupplier, rateLimit), listener);
    }

    private final boolean isCreative;

    private EnergyCubeEnergyContainer(EnergyCubeTier tier, @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter,
          @Nullable IContentsListener listener) {
        super(tier.getCapacity(), ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), insertionRateLimiter, extractionRateLimiter, listener);
        isCreative = tier == EnergyCubeTier.CREATIVE;
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