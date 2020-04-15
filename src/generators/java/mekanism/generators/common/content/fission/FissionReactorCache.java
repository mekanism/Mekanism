package mekanism.generators.common.content.fission;

import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.CompoundNBT;

public class FissionReactorCache extends MultiblockCache<SynchronizedFissionReactorData> {

    @Override
    public void apply(SynchronizedFissionReactorData data) {
        super.apply(data);
    }

    @Override
    public void sync(SynchronizedFissionReactorData data) {
        super.sync(data);
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
    }
}
