package mekanism.generators.common.content.fission;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

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
    public void apply(HolderLookup.Provider provider, FissionReactorMultiblockData data) {
        super.apply(provider, data);
        data.reactorDamage = reactorDamage;
        data.rateLimit = Mth.clamp(getRateLimit(), 0, data.getMaxBurnRate());
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
    public void load(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.load(provider, nbtTags);
        reactorDamage = nbtTags.getDouble(SerializationConstants.REACTOR_DAMAGE);
        NBTUtils.setDoubleIfPresent(nbtTags, SerializationConstants.INJECTION_RATE, value -> rateLimit = value);
        burnRemaining = nbtTags.getDouble(SerializationConstants.BURN_TIME);
        partialWaste = nbtTags.getDouble(SerializationConstants.PARTIAL_WASTE);
        forceDisable = nbtTags.getBoolean(SerializationConstants.DISABLED);
        active = nbtTags.getBoolean(SerializationConstants.ACTIVE);
    }

    @Override
    public void save(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.save(provider, nbtTags);
        nbtTags.putDouble(SerializationConstants.REACTOR_DAMAGE, reactorDamage);
        nbtTags.putDouble(SerializationConstants.INJECTION_RATE, getRateLimit());
        nbtTags.putDouble(SerializationConstants.BURN_TIME, burnRemaining);
        nbtTags.putDouble(SerializationConstants.PARTIAL_WASTE, partialWaste);
        nbtTags.putBoolean(SerializationConstants.DISABLED, forceDisable);
        nbtTags.putBoolean(SerializationConstants.ACTIVE, active);
    }
}
