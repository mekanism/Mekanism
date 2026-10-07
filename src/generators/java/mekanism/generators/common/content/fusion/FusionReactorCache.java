package mekanism.generators.common.content.fusion;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class FusionReactorCache extends MultiblockCache<FusionReactorMultiblockData> {

    private static final int NO_INJECTION_RATE_SET = -1;
    private static final double NO_PLASMA_TEMPERATURE_SET = -1;

    private double plasmaTemperature = NO_PLASMA_TEMPERATURE_SET;
    private int injectionRate = NO_INJECTION_RATE_SET;
    private boolean burning = FusionReactorMultiblockData.DEFAULT_BURNING;

    private int getInjectionRate() {
        if (injectionRate == NO_INJECTION_RATE_SET) {
            //If it never got set it to the default
            return FusionReactorMultiblockData.DEFAULT_INJECTION_RATE;
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
    public void apply(FusionReactorMultiblockData data, TransactionContext transaction) {
        super.apply(data, transaction);
        if (plasmaTemperature >= 0) {
            data.setPlasmaTemp(plasmaTemperature, transaction);
        }
        data.setInjectionRate(getInjectionRate(), transaction);
        data.setBurning(burning);
        data.updateTemperatures();
    }

    @Override
    public void sync(FusionReactorMultiblockData data, TransactionContext transaction) {
        super.sync(data, transaction);
        plasmaTemperature = data.getPlasmaTemp();
        injectionRate = data.getInjectionRate();
        burning = data.isBurning();
    }

    @Override
    public void load(ValueInput input) {
        super.load(input);
        plasmaTemperature = input.getDoubleOr(SerializationConstants.PLASMA_TEMP, NO_PLASMA_TEMPERATURE_SET);
        injectionRate = input.getIntOr(SerializationConstants.INJECTION_RATE, NO_INJECTION_RATE_SET);
        burning = input.getBooleanOr(SerializationConstants.BURNING, FusionReactorMultiblockData.DEFAULT_BURNING);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.putDouble(SerializationConstants.PLASMA_TEMP, plasmaTemperature);
        output.putInt(SerializationConstants.INJECTION_RATE, injectionRate);
        output.putBoolean(SerializationConstants.BURNING, burning);
    }
}
