package mekanism.common.content.sps;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class SPSCache extends MultiblockCache<SPSMultiblockData> {

    private double progress;
    private int inputProcessed;

    @Override
    public void merge(MultiblockCache<SPSMultiblockData> mergeCache, List<ItemStack> rejectedItems) {
        super.merge(mergeCache, rejectedItems);
        progress = ((SPSCache) mergeCache).progress;
        inputProcessed = ((SPSCache) mergeCache).inputProcessed;
    }

    @Override
    public void apply(SPSMultiblockData data) {
        super.apply(data);
        data.progress = progress;
        data.inputProcessed = inputProcessed;
    }

    @Override
    public void sync(SPSMultiblockData data) {
        super.sync(data);
        progress = data.progress;
        inputProcessed = data.inputProcessed;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.PROGRESS, val -> progress = val);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.PROCESSED, val -> inputProcessed = val);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putDouble(NBTConstants.PROGRESS, progress);
        nbtTags.putInt(NBTConstants.PROCESSED, inputProcessed);
    }
}
