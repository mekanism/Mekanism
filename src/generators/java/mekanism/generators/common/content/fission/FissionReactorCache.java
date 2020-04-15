package mekanism.generators.common.content.fission;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class FissionReactorCache extends MultiblockCache<SynchronizedFissionReactorData> {

    private double reactorDamage;
    private boolean active;

    @Override
    public void merge(MultiblockCache<SynchronizedFissionReactorData> mergeCache, List<ItemStack> rejectedItems) {
        super.merge(mergeCache, rejectedItems);
        reactorDamage = Math.max(reactorDamage, ((FissionReactorCache) mergeCache).reactorDamage);
        active |= ((FissionReactorCache) mergeCache).active;
    }

    @Override
    public void apply(SynchronizedFissionReactorData data) {
        super.apply(data);
        data.reactorDamage = reactorDamage;
        data.active = active;
    }

    @Override
    public void sync(SynchronizedFissionReactorData data) {
        super.sync(data);
        reactorDamage = data.reactorDamage;
        active = data.active;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        reactorDamage = nbtTags.getDouble(NBTConstants.REACTOR_DAMAGE);
        active = nbtTags.getBoolean(NBTConstants.ACTIVE);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putDouble(NBTConstants.REACTOR_DAMAGE, reactorDamage);
        nbtTags.putBoolean(NBTConstants.ACTIVE, active);
    }
}
