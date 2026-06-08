package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.ITransactionHelper;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class FluidTankFluidTank extends BasicFluidTank {

    public static FluidTankFluidTank create(TileEntityFluidTank tile, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Fluid tank tile entity cannot be null");
        return new FluidTankFluidTank(tile, listener);
    }

    private final TileEntityFluidTank tile;
    private final boolean isCreative;

    private FluidTankFluidTank(TileEntityFluidTank tile, @Nullable IContentsListener listener) {
        LongSupplier gameTimeSupplier = MekanismUtils.getGameTimeSupplier(tile);
        IntSupplier rateLimit = tile.tier::getTransferRate;
        super(tile.tier.getCapacity(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              //Only limit the internal rate to change the speed at which this can be filled or drained by an item stored in a slot
              ITransactionHelper.INSTANCE.createInternalOnlyRateLimit(gameTimeSupplier, rateLimit),
              ITransactionHelper.INSTANCE.createInternalOnlyRateLimit(gameTimeSupplier, rateLimit), listener);
        this.tile = tile;
        isCreative = tile.tier == FluidTankTier.CREATIVE;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int inserted;
        if (isCreative) {
            if (isEmpty() && !automationType.isExternal()) {
                //If a player manually inserts into a creative tank (or internally, via a FluidInventorySlot), that is empty we need to allow setting the type,
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
                inserted = super.insert(resource, amount, simulation, automationType);
            }
        } else {
            inserted = super.insert(resource, amount, transaction, automationType);
        }
        //Ensure we have the same type of fluid stored as we failed to insert, in which case we want to try to insert to the one above
        if (inserted < amount && resource().equals(resource)) {
            //If we have any leftover check if we can send it to the tank that is above
            TileEntityFluidTank tileAbove = WorldUtils.getTileEntity(TileEntityFluidTank.class, this.tile.getLevel(), this.tile.getBlockPos().above());
            if (tileAbove != null) {
                //Note: We do external so that it is not limited by the internal rate limits
                inserted += tileAbove.fluidTank.insert(resource, amount - inserted, transaction, AutomationType.EXTERNAL);
            }
        }
        return inserted;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.extract(resource, amount, simulation, automationType);
            }
        }
        return super.extract(resource, amount, transaction, automationType);
    }
}