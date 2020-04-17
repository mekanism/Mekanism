package mekanism.generators.common.content.fission;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class FissionReactorCache extends MultiblockCache<SynchronizedFissionReactorData> {

    private double reactorDamage;
    private double rateLimit;
    private double burnRemaining;
    private double partialWaste;
    public boolean active;

    @Override
    public void merge(MultiblockCache<SynchronizedFissionReactorData> mergeCache, List<ItemStack> rejectedItems) {
        super.merge(mergeCache, rejectedItems);
        reactorDamage = Math.max(reactorDamage, ((FissionReactorCache) mergeCache).reactorDamage);
        rateLimit = Math.max(rateLimit, ((FissionReactorCache) mergeCache).rateLimit);
        burnRemaining = Math.max(burnRemaining, ((FissionReactorCache) mergeCache).burnRemaining);
        partialWaste = Math.max(partialWaste, ((FissionReactorCache) mergeCache).partialWaste);
        active |= ((FissionReactorCache) mergeCache).active;
    }

    @Override
    public void apply(SynchronizedFissionReactorData data) {
        super.apply(data);
        data.reactorDamage = reactorDamage;
        data.rateLimit = rateLimit;
        data.burnRemaining = burnRemaining;
        data.partialWaste = partialWaste;
        data.setActive(active);
    }

    @Override
    public void sync(SynchronizedFissionReactorData data) {
        super.sync(data);
        reactorDamage = data.reactorDamage;
        rateLimit = data.rateLimit;
        burnRemaining = data.burnRemaining;
        partialWaste = data.partialWaste;
        active = data.isActive();
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        reactorDamage = nbtTags.getDouble(NBTConstants.REACTOR_DAMAGE);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.INJECTION_RATE, (value) -> rateLimit = value);
        burnRemaining = nbtTags.getDouble(NBTConstants.BURN_TIME);
        partialWaste = nbtTags.getDouble(NBTConstants.PARTIAL_WASTE);
        active = nbtTags.getBoolean(NBTConstants.ACTIVE);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putDouble(NBTConstants.REACTOR_DAMAGE, reactorDamage);
        nbtTags.putDouble(NBTConstants.INJECTION_RATE, rateLimit);
        nbtTags.putDouble(NBTConstants.BURN_TIME, burnRemaining);
        nbtTags.putDouble(NBTConstants.PARTIAL_WASTE, partialWaste);
        nbtTags.putBoolean(NBTConstants.ACTIVE, active);
    }
}
