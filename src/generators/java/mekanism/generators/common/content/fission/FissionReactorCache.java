package mekanism.generators.common.content.fission;

import mekanism.api.NBTConstants;
import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.CompoundNBT;

public class FissionReactorCache extends MultiblockCache<SynchronizedFissionReactorData> {

    private double reactorDamage;

    @Override
    public void apply(SynchronizedFissionReactorData data) {
        super.apply(data);
        data.reactorDamage = reactorDamage;
    }

    @Override
    public void sync(SynchronizedFissionReactorData data) {
        super.sync(data);
        reactorDamage = data.reactorDamage;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        reactorDamage = nbtTags.getDouble(NBTConstants.REACTOR_DAMAGE);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putDouble(NBTConstants.REACTOR_DAMAGE, reactorDamage);
    }
}
