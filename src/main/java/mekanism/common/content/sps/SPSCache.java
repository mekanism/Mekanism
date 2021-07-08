package mekanism.common.content.sps;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class SPSCache extends MultiblockCache<SPSMultiblockData> {

    private double progress;
    private int inputProcessed;
    private boolean couldOperate;
    private FloatingLong receivedEnergy = FloatingLong.ZERO;
    private double lastProcessed;

    @Override
    public void merge(MultiblockCache<SPSMultiblockData> mergeCache, List<ItemStack> rejectedItems) {
        super.merge(mergeCache, rejectedItems);
        progress += ((SPSCache) mergeCache).progress;
        inputProcessed += ((SPSCache) mergeCache).inputProcessed;
        couldOperate |= ((SPSCache) mergeCache).couldOperate;
        receivedEnergy = receivedEnergy.add(((SPSCache) mergeCache).receivedEnergy);
        lastProcessed = Math.max(lastProcessed, ((SPSCache) mergeCache).lastProcessed);
    }

    @Override
    public void apply(SPSMultiblockData data) {
        super.apply(data);
        data.progress = progress;
        data.inputProcessed = inputProcessed;
        data.couldOperate = couldOperate;
        data.receivedEnergy = receivedEnergy;
        data.lastProcessed = lastProcessed;
    }

    @Override
    public void sync(SPSMultiblockData data) {
        super.sync(data);
        progress = data.progress;
        inputProcessed = data.inputProcessed;
        couldOperate = data.couldOperate;
        receivedEnergy = data.receivedEnergy;
        lastProcessed = data.lastProcessed;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.PROGRESS, val -> progress = val);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.PROCESSED, val -> inputProcessed = val);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.COULD_OPERATE, val -> couldOperate = val);
        NBTUtils.setFloatingLongIfPresent(nbtTags, NBTConstants.ENERGY_USAGE, val -> receivedEnergy = val);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.LAST_PROCESSED, val -> lastProcessed = val);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putDouble(NBTConstants.PROGRESS, progress);
        nbtTags.putInt(NBTConstants.PROCESSED, inputProcessed);
        nbtTags.putBoolean(NBTConstants.COULD_OPERATE, couldOperate);
        nbtTags.putString(NBTConstants.ENERGY_USAGE, receivedEnergy.toString());
        nbtTags.putDouble(NBTConstants.LAST_PROCESSED, lastProcessed);
    }
}