package mekanism.generators.common.content.fusion;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class FusionReactorCache extends MultiblockCache<FusionReactorMultiblockData> {

    private double plasmaTemperature = -1;
    private int injectionRate = -1;
    private boolean burning;

    private int getInjectionRate() {
        if (injectionRate == -1) {
            //If it never got set default to 2
            return 2;
        }
        //Otherwise, return the actual so that it can be manually set down to zero
        return injectionRate;
    }

    @Override
    public void merge(MultiblockCache<FusionReactorMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        plasmaTemperature = Math.max(plasmaTemperature, ((FusionReactorCache) mergeCache).plasmaTemperature);
        injectionRate = Math.max(injectionRate, ((FusionReactorCache) mergeCache).injectionRate);
        burning |= ((FusionReactorCache) mergeCache).burning;
    }

    @Override
    public void apply(HolderLookup.Provider provider, FusionReactorMultiblockData data) {
        super.apply(provider, data);
        if (plasmaTemperature >= 0) {
            data.plasmaTemperature = plasmaTemperature;
        }
        data.setInjectionRate(getInjectionRate());
        data.setBurning(burning);
        data.updateTemperatures();
    }

    @Override
    public void sync(FusionReactorMultiblockData data) {
        super.sync(data);
        plasmaTemperature = data.plasmaTemperature;
        injectionRate = data.getInjectionRate();
        burning = data.isBurning();
    }

    @Override
    public void load(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.load(provider, nbtTags);
        plasmaTemperature = nbtTags.getDouble(SerializationConstants.PLASMA_TEMP);
        injectionRate = nbtTags.getInt(SerializationConstants.INJECTION_RATE);
        burning = nbtTags.getBoolean(SerializationConstants.BURNING);
    }

    @Override
    public void save(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.save(provider, nbtTags);
        nbtTags.putDouble(SerializationConstants.PLASMA_TEMP, plasmaTemperature);
        nbtTags.putInt(SerializationConstants.INJECTION_RATE, getInjectionRate());
        nbtTags.putBoolean(SerializationConstants.BURNING, burning);
    }
}
