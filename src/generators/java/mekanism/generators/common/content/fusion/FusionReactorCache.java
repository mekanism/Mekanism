package mekanism.generators.common.content.fusion;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class FusionReactorCache extends MultiblockCache<FusionReactorMultiblockData> {

    private double plasmaTemperature = -1;
    private int injectionRate = -1;
    private boolean burning;

    private int getInjectionRate() {
        if (injectionRate == -1) {
            //If it never got set default to 2
            return 2;
        }
        //Otherwise return the actual so that it can be manually set down to zero
        return injectionRate;
    }

    @Override
    public void merge(MultiblockCache<FusionReactorMultiblockData> mergeCache, List<ItemStack> rejectedItems) {
        super.merge(mergeCache, rejectedItems);
        plasmaTemperature = Math.max(plasmaTemperature, ((FusionReactorCache) mergeCache).plasmaTemperature);
        injectionRate = Math.max(injectionRate, ((FusionReactorCache) mergeCache).injectionRate);
        burning |= ((FusionReactorCache) mergeCache).burning;
    }

    @Override
    public void apply(FusionReactorMultiblockData data) {
        super.apply(data);
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
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        plasmaTemperature = nbtTags.getDouble(NBTConstants.PLASMA_TEMP);
        injectionRate = nbtTags.getInt(NBTConstants.INJECTION_RATE);
        burning = nbtTags.getBoolean(NBTConstants.BURNING);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putDouble(NBTConstants.PLASMA_TEMP, plasmaTemperature);
        nbtTags.putInt(NBTConstants.INJECTION_RATE, getInjectionRate());
        nbtTags.putBoolean(NBTConstants.BURNING, burning);
    }
}
