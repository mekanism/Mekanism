package mekanism.common.content.sps;

import mekanism.api.SerializationConstants;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class SPSCache extends MultiblockCache<SPSMultiblockData> {

    private double progress;
    private int inputProcessed;
    private boolean couldOperate;
    private long receivedEnergy = 0;
    private double lastProcessed;

    @Override
    public void merge(MultiblockCache<SPSMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        SPSCache spsMergeCache = (SPSCache) mergeCache;
        progress += spsMergeCache.progress;
        inputProcessed += spsMergeCache.inputProcessed;
        couldOperate |= spsMergeCache.couldOperate;
        receivedEnergy = MathUtils.addClamped(receivedEnergy, spsMergeCache.receivedEnergy);
        lastProcessed = Math.max(lastProcessed, spsMergeCache.lastProcessed);
    }

    @Override
    public void apply(HolderLookup.Provider provider, SPSMultiblockData data) {
        super.apply(provider, data);
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
    public void load(@NotNull ValueInput input) {
        super.load(input);
        progress = input.getDoubleOr(SerializationConstants.PROGRESS, progress);
        inputProcessed = input.getIntOr(SerializationConstants.PROCESSED, inputProcessed);
        couldOperate = input.getBooleanOr(SerializationConstants.COULD_OPERATE, couldOperate);
        receivedEnergy = input.getLongOr(SerializationConstants.ENERGY_USAGE, receivedEnergy);
        lastProcessed = input.getDoubleOr(SerializationConstants.LAST_PROCESSED, lastProcessed);
    }

    @Override
    public void save(@NotNull ValueOutput output) {
        super.save(output);
        output.putDouble(SerializationConstants.PROGRESS, progress);
        output.putInt(SerializationConstants.PROCESSED, inputProcessed);
        output.putBoolean(SerializationConstants.COULD_OPERATE, couldOperate);
        output.putLong(SerializationConstants.ENERGY_USAGE, receivedEnergy);
        output.putDouble(SerializationConstants.LAST_PROCESSED, lastProcessed);
    }
}