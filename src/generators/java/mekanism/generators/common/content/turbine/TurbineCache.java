package mekanism.generators.common.content.turbine;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class TurbineCache extends MultiblockCache<TurbineMultiblockData> {

    private GasMode dumpMode = GasMode.IDLE;

    @Override
    public void merge(MultiblockCache<TurbineMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        dumpMode = ((TurbineCache) mergeCache).dumpMode;
    }

    @Override
    public void apply(TurbineMultiblockData data) {
        super.apply(data);
        data.dumpMode = dumpMode;
    }

    @Override
    public void sync(TurbineMultiblockData data) {
        super.sync(data);
        dumpMode = data.dumpMode;
    }

    @Override
    public void load(@NotNull ValueInput input) {
        super.load(input);
        NBTUtils.setEnumIfPresent(input, SerializationConstants.DUMP_MODE, GasMode.BY_ID, mode -> dumpMode = mode);
    }

    @Override
    public void save(@NotNull ValueOutput output) {
        super.save(output);
        NBTUtils.writeEnum(output, SerializationConstants.DUMP_MODE, dumpMode);
    }
}