package mekanism.generators.common.content.turbine;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.ValueUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TurbineCache extends MultiblockCache<TurbineMultiblockData> {

    private GasMode dumpMode = GasMode.IDLE;

    @Override
    public void merge(MultiblockCache<TurbineMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        dumpMode = ((TurbineCache) mergeCache).dumpMode;
    }

    @Override
    public void apply(TurbineMultiblockData data, TransactionContext transaction) {
        super.apply(data, transaction);
        data.dumpMode = dumpMode;
    }

    @Override
    public void sync(TurbineMultiblockData data, TransactionContext transaction) {
        super.sync(data, transaction);
        dumpMode = data.dumpMode;
    }

    @Override
    public void load(ValueInput input) {
        super.load(input);
        ValueUtils.setEnumIfPresent(input, SerializationConstants.DUMP_MODE, GasMode.BY_ID, mode -> dumpMode = mode);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        ValueUtils.writeEnum(output, SerializationConstants.DUMP_MODE, dumpMode);
    }
}