package mekanism.generators.common.content.fission;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class FissionReactorCache extends MultiblockCache<FissionReactorMultiblockData> {

    private static final double NO_RATE_LIMIT_SET = -1;

    private double reactorDamage = FissionReactorMultiblockData.DEFAULT_REACTOR_DAMAGE;
    private double rateLimit = NO_RATE_LIMIT_SET;
    private double burnRemaining = FissionReactorMultiblockData.DEFAULT_BURN_REMAINING;
    private double partialWaste = FissionReactorMultiblockData.DEFAULT_PARTIAL_WASTE;
    private boolean active = FissionReactorMultiblockData.DEFAULT_ACTIVE;
    private boolean forceDisable = FissionReactorMultiblockData.DEFAULT_FORCE_DISABLE;

    private double getRateLimit() {
        if (rateLimit == NO_RATE_LIMIT_SET) {
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
    public void apply(FissionReactorMultiblockData data, TransactionContext transaction) {
        super.apply(data, transaction);
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
    public void sync(FissionReactorMultiblockData data, TransactionContext transaction) {
        super.sync(data, transaction);
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
        reactorDamage = input.getDoubleOr(SerializationConstants.REACTOR_DAMAGE, FissionReactorMultiblockData.DEFAULT_REACTOR_DAMAGE);
        rateLimit = input.getDoubleOr(SerializationConstants.INJECTION_RATE, NO_RATE_LIMIT_SET);
        burnRemaining = input.getDoubleOr(SerializationConstants.BURN_TIME, FissionReactorMultiblockData.DEFAULT_BURN_REMAINING);
        partialWaste = input.getDoubleOr(SerializationConstants.PARTIAL_WASTE, FissionReactorMultiblockData.DEFAULT_PARTIAL_WASTE);
        forceDisable = input.getBooleanOr(SerializationConstants.DISABLED, FissionReactorMultiblockData.DEFAULT_FORCE_DISABLE);
        active = input.getBooleanOr(SerializationConstants.ACTIVE, FissionReactorMultiblockData.DEFAULT_ACTIVE);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.putDouble(SerializationConstants.REACTOR_DAMAGE, reactorDamage);
        output.putDouble(SerializationConstants.INJECTION_RATE, rateLimit);
        output.putDouble(SerializationConstants.BURN_TIME, burnRemaining);
        output.putDouble(SerializationConstants.PARTIAL_WASTE, partialWaste);
        output.putBoolean(SerializationConstants.DISABLED, forceDisable);
        output.putBoolean(SerializationConstants.ACTIVE, active);
    }
}
