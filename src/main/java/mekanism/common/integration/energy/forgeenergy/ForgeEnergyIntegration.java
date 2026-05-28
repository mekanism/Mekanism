package mekanism.common.integration.energy.forgeenergy;

import com.google.common.primitives.Ints;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.BaseEnergyIntegration;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.VisibleForTesting;

@NothingNullByDefault
public class ForgeEnergyIntegration extends BaseEnergyIntegration implements EnergyHandler {

    public ForgeEnergyIntegration(IStrictEnergyHandler handler) {
        this(handler, EnergyUnit.FORGE_ENERGY);
    }

    @VisibleForTesting
    ForgeEnergyIntegration(IStrictEnergyHandler handler, IEnergyConversion converter) {
        super(handler, converter);
    }

    @Override
    protected long convertTo(long joules) {
        //Note: FE is clamped to ints, so we need to just convert it to an int
        return converter.convertToAsInt(joules);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return 0;
        } else if (converter.isOneToOne()) {
            return Ints.saturatedCast(handler.insert(amount, transaction));
        }
        long toInsert = calculateToInsert(amount, transaction);
        if (toInsert == 0) {
            //If converting back and forth between our compat type and Joules causes us to be clamped at zero, that means we can't accept anything or could only
            // accept a partial amount; we need to exit early returning that we couldn't insert anything
            return 0;
        }
        return converter.convertToAsInt(handler.insert(toInsert, transaction));
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return 0;
        } else if (converter.isOneToOne()) {
            return Ints.saturatedCast(handler.extract(amount, transaction));
        }
        long toExtract = calculateToExtract(amount, transaction);
        if (toExtract == 0) {
            //If converting back and forth between FE and Joules causes us to be clamped at zero, that means we can't provide anything or could only
            // provide a partial amount; we need to exit early returning that nothing could be extracted
            return 0;
        }
        return converter.convertToAsInt(handler.extract(toExtract, transaction));
    }

    @Override
    public long getAmountAsLong() {
        return calculateSum(IStrictEnergyHandler::getAmountAsLong);
    }

    @Override
    public long getCapacityAsLong() {
        return calculateSum(IStrictEnergyHandler::getCapacityAsLong);
    }
}