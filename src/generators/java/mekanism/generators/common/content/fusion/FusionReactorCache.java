package mekanism.generators.common.content.fusion;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

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
    public void load(@NotNull ValueInput input) {
        super.load(input);
        //TODO - 1.21.11: These used to just get instead of only getting if present, should the fallback be zero or the existing value?
        plasmaTemperature = input.getDoubleOr(SerializationConstants.PLASMA_TEMP, plasmaTemperature);
        injectionRate = input.getIntOr(SerializationConstants.INJECTION_RATE, injectionRate);
        burning = input.getBooleanOr(SerializationConstants.BURNING, burning);
    }

    @Override
    public void save(@NotNull ValueOutput output) {
        super.save(output);
        output.putDouble(SerializationConstants.PLASMA_TEMP, plasmaTemperature);
        output.putInt(SerializationConstants.INJECTION_RATE, getInjectionRate());
        output.putBoolean(SerializationConstants.BURNING, burning);
    }
}
