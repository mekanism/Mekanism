package mekanism.generators.common.content.fission;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FissionReactorCache extends MultiblockCache<FissionReactorMultiblockData> {

    private double reactorDamage;
    private double rateLimit = -1;
    private double burnRemaining;
    private double partialWaste;
    private boolean active;
    private boolean forceDisable;

    private double getRateLimit() {
        if (rateLimit == -1) {
            //If it never got set it to the default
            return MekanismGeneratorsConfig.generators.defaultBurnRate.get();
        }
        //Otherwise, return the actual so that it can be manually set down to zero
        return rateLimit;
    }

    @Override
    public void merge(MultiblockCache<FissionReactorMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        reactorDamage = Math.max(reactorDamage, ((FissionReactorCache) mergeCache).reactorDamage);
        rateLimit = Math.max(rateLimit, ((FissionReactorCache) mergeCache).rateLimit);
        burnRemaining += ((FissionReactorCache) mergeCache).burnRemaining;
        partialWaste += ((FissionReactorCache) mergeCache).partialWaste;
        active |= ((FissionReactorCache) mergeCache).active;
        forceDisable |= ((FissionReactorCache) mergeCache).forceDisable;
    }

    @Override
    public void apply(FissionReactorMultiblockData data) {
        super.apply(data);
        data.reactorDamage = reactorDamage;
        data.rateLimit = Math.clamp(getRateLimit(), 0, data.getMaxBurnRate());
        data.burnRemaining = burnRemaining;
        data.partialWaste = partialWaste;
        //Update the force disabled state of it before setting it to active to make sure that we properly deny it being active,
        // if we should be denying it
        // Note: We don't update force disabled here based on temperature, damage, and meltdowns being enabled as if they are
        // the next tick we will unset it, and if not it will enter a meltdown
        data.setForceDisable(forceDisable);
        data.setActive(active);
    }

    @Override
    public void sync(FissionReactorMultiblockData data) {
        super.sync(data);
        reactorDamage = data.reactorDamage;
        rateLimit = data.rateLimit;
        burnRemaining = data.burnRemaining;
        partialWaste = data.partialWaste;
        forceDisable = data.isForceDisabled();
        active = data.isActive();
    }

    @Override
    public void load(ValueInput input) {
        super.load(input);
        //TODO - 26.2: These (except injection rate) used to just get instead of only getting if present, should the fallback be zero or the existing value?
        reactorDamage = input.getDoubleOr(SerializationConstants.REACTOR_DAMAGE, reactorDamage);
        rateLimit = input.getDoubleOr(SerializationConstants.INJECTION_RATE, rateLimit);
        burnRemaining = input.getDoubleOr(SerializationConstants.BURN_TIME, burnRemaining);
        partialWaste = input.getDoubleOr(SerializationConstants.PARTIAL_WASTE, partialWaste);
        forceDisable = input.getBooleanOr(SerializationConstants.DISABLED, forceDisable);
        active = input.getBooleanOr(SerializationConstants.ACTIVE, active);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.putDouble(SerializationConstants.REACTOR_DAMAGE, reactorDamage);
        output.putDouble(SerializationConstants.INJECTION_RATE, getRateLimit());
        output.putDouble(SerializationConstants.BURN_TIME, burnRemaining);
        output.putDouble(SerializationConstants.PARTIAL_WASTE, partialWaste);
        output.putBoolean(SerializationConstants.DISABLED, forceDisable);
        output.putBoolean(SerializationConstants.ACTIVE, active);
    }
}
