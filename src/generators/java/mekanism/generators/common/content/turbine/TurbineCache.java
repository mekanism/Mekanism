package mekanism.generators.common.content.turbine;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TurbineCache extends MultiblockCache<TurbineMultiblockData> {

    private GasMode dumpMode = TurbineMultiblockData.DEFAULT_DUMP_MODE;

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
        dumpMode = input.read(SerializationConstants.DUMP_MODE, GasMode.CODEC).orElse(TurbineMultiblockData.DEFAULT_DUMP_MODE);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.store(SerializationConstants.DUMP_MODE, GasMode.CODEC, dumpMode);
    }
}