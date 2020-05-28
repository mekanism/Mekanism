package mekanism.generators.common.content.fusion;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class FusionReactorCache extends MultiblockCache<FusionReactorMultiblockData> {

    private double plasmaTemperature = -1;
    private int injectionRate;
    private boolean burning;

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
        data.setInjectionRate(injectionRate);
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
        nbtTags.putInt(NBTConstants.INJECTION_RATE, injectionRate);
        nbtTags.putBoolean(NBTConstants.BURNING, burning);
    }
}
