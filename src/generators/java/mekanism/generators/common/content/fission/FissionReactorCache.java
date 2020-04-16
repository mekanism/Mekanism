package mekanism.generators.common.content.fission;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class FissionReactorCache extends MultiblockCache<SynchronizedFissionReactorData> {

    private double reactorDamage;
    private long rateLimit;
    private boolean active;

    @Override
    public void merge(MultiblockCache<SynchronizedFissionReactorData> mergeCache, List<ItemStack> rejectedItems) {
        super.merge(mergeCache, rejectedItems);
        reactorDamage = Math.max(reactorDamage, ((FissionReactorCache) mergeCache).reactorDamage);
        rateLimit = Math.max(rateLimit, ((FissionReactorCache) mergeCache).rateLimit);
        active |= ((FissionReactorCache) mergeCache).active;
    }

    @Override
    public void apply(SynchronizedFissionReactorData data) {
        super.apply(data);
        data.reactorDamage = reactorDamage;
        data.rateLimit = rateLimit;
        data.active = active;
    }

    @Override
    public void sync(SynchronizedFissionReactorData data) {
        super.sync(data);
        reactorDamage = data.reactorDamage;
        rateLimit = data.rateLimit;
        active = data.active;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        reactorDamage = nbtTags.getDouble(NBTConstants.REACTOR_DAMAGE);
        NBTUtils.setLongIfPresent(nbtTags, NBTConstants.INJECTION_RATE, (value) -> rateLimit = value);
        active = nbtTags.getBoolean(NBTConstants.ACTIVE);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putDouble(NBTConstants.REACTOR_DAMAGE, reactorDamage);
        nbtTags.putLong(NBTConstants.INJECTION_RATE, rateLimit);
        nbtTags.putBoolean(NBTConstants.ACTIVE, active);
    }
}
