package mekanism.common.multiblock;

import java.util.HashSet;
import java.util.Set;
import mekanism.api.Coord4D;
import net.minecraft.nbt.CompoundNBT;

public abstract class MultiblockCache<T extends SynchronizedData<T>> {

    public Set<Coord4D> locations = new HashSet<>();

    public abstract void apply(T data);

    public abstract void sync(T data);

    public abstract void load(CompoundNBT nbtTags);

    public abstract void save(CompoundNBT nbtTags);
}