package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.ITransactionHelper;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class ChemicalTankChemicalTank extends BasicChemicalTank {

    public static ChemicalTankChemicalTank create(TileEntityChemicalTank tile, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Chemical tank tile cannot be null");
        ChemicalTankTier tier = tile.getTier();
        LongSupplier gameTimeSupplier = tile::getGameTime;
        IntSupplier rateLimit = tier::getTransferRate;
        //Only limit the internal rate to change the speed at which this can be filled or drained by an item stored in a slot
        return new ChemicalTankChemicalTank(tier, ITransactionHelper.INSTANCE.createInternalOnlyRateLimit(gameTimeSupplier, rateLimit),
              ITransactionHelper.INSTANCE.createInternalOnlyRateLimit(gameTimeSupplier, rateLimit), listener);
    }

    private final boolean isCreative;

    private ChemicalTankChemicalTank(ChemicalTankTier tier, @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter,
          @Nullable IContentsListener listener) {
        //TODO - 26.2: Should this and the one for fluid tanks and energy cubes be variable capacity instead of just caching the capacity at time of creation?
        super(tier.getCapacity(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), insertionRateLimiter,
              extractionRateLimiter, tier.isCreative() ? ChemicalAttributeValidator.ALWAYS_ALLOW : null, listener);
        isCreative = tier.isCreative();
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            if (isEmpty() && !automationType.isExternal()) {
                //If a player manually inserts into a creative tank (or internally, via a ChemicalInventorySlot), that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative tanks acts as a "void" for automation
                try (Transaction simulation = Transaction.open(transaction)) {
                    if (super.insert(resource, amount, simulation, automationType) == 0) {
                        return 0;
                    }
                }
                //If we managed to insert anything, set the contents to the maximum amount of that item type
                // Note: We just set it as unchecked as we have already validated it
                setContents(resource, capacityAsLong(resource), transaction);
                //Return that we accepted the entire amount we were passed
                return amount;
            }
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(resource, amount, simulation, automationType);
            }
        }
        return super.insert(resource, amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.extract(resource, amount, simulation, automationType);
            }
        }
        return super.extract(resource, amount, transaction, automationType);
    }
}