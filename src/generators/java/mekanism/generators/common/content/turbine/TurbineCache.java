package mekanism.generators.common.content.turbine;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class TurbineCache extends MultiblockCache<TurbineMultiblockData> {

    private GasMode dumpMode = GasMode.IDLE;

    @Override
    public void merge(MultiblockCache<TurbineMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        dumpMode = ((TurbineCache) mergeCache).dumpMode;
    }

    @Override
    public void apply(HolderLookup.Provider provider, TurbineMultiblockData data) {
        super.apply(provider, data);
        data.dumpMode = dumpMode;
    }

    @Override
    public void sync(TurbineMultiblockData data) {
        super.sync(data);
        dumpMode = data.dumpMode;
    }

    @Override
    public void load(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.load(provider, nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, SerializationConstants.DUMP_MODE, GasMode.BY_ID, mode -> dumpMode = mode);
    }

    @Override
    public void save(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.save(provider, nbtTags);
        NBTUtils.writeEnum(nbtTags, SerializationConstants.DUMP_MODE, dumpMode);
    }
}