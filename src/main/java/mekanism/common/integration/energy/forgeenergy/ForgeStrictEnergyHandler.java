package mekanism.common.integration.energy.forgeenergy;

import com.google.common.primitives.Ints;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.common.integration.energy.SingleContainerStrictEnergyHandler;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.VisibleForTesting;

@NothingNullByDefault
public class ForgeStrictEnergyHandler extends SingleContainerStrictEnergyHandler {

    private final EnergyHandler neoHandler;

    public ForgeStrictEnergyHandler(EnergyHandler neoHandler) {
        this(neoHandler, EnergyUnit.FORGE_ENERGY);
    }

    @VisibleForTesting
    ForgeStrictEnergyHandler(EnergyHandler neoHandler, IEnergyConversion converter) {
        super(converter);
        this.neoHandler = neoHandler;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int container) {
        return container == 0 ? converter.convertFrom(neoHandler.getAmountAsLong()) : 0L;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int container) {
        return container == 0 ? converter.convertFrom(neoHandler.getCapacityAsLong()) : 0L;
    }

    @Override
    protected long convertTo(long joules) {
        //Note: FE is clamped to ints, so we need to just convert it to an int
        return converter.convertToAsInt(joules);
    }

    @Override
    protected long insertCompat(long toInsert, TransactionContext transaction) {
        //Note: FE is clamped to ints, so clamp it to an int
        return neoHandler.insert(Ints.saturatedCast(toInsert), transaction);
    }

    @Override
    protected long extractCompat(long toExtract, TransactionContext transaction) {
        //Note: FE is clamped to ints, so clamp it to an int
        return neoHandler.extract(Ints.saturatedCast(toExtract), transaction);
    }
}