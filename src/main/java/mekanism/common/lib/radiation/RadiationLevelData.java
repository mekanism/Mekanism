package mekanism.common.lib.radiation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.listener.ConfigBasedCachedIntSupplier;
import mekanism.common.lib.collection.IndexedCuboidMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RadiationLevelData implements INBTSerializable<ListTag> {
    private static final IntSupplier MAX_BLOCK_RANGE = new ConfigBasedCachedIntSupplier(() -> {
        int chunkRadius = MekanismConfig.general.radiationChunkCheckRadius.get();
        // we only compute exposure when within the MAX_RANGE bounds
        return Mth.square(chunkRadius * 16);
    }, MekanismConfig.general.radiationChunkCheckRadius);

    private final IndexedCuboidMap<RadiationSource> sources = new IndexedCuboidMap<>();

    public LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(BlockPos checkPos) {
        if (isEmpty()) {//Short circuit when the radiation table is empty
            return LevelAndMaxMagnitude.BASELINE;
        }
        double level = RadiationManager.get().baselineRadiation();
        double maxMagnitude = level;

        Iterator<RadiationSource> sourceIterator = sources.find(checkPos);
        while (sourceIterator.hasNext()) {
            RadiationSource source = sourceIterator.next();
            level += RadiationUtil.computeExposure(source, checkPos);
            maxMagnitude = Math.max(maxMagnitude, source.getMagnitude());
        }

        if (level <= LevelAndMaxMagnitude.BASELINE.level() && maxMagnitude <= LevelAndMaxMagnitude.BASELINE.maxMagnitude()) {
            return LevelAndMaxMagnitude.BASELINE;
        }

        return new LevelAndMaxMagnitude(level, maxMagnitude);
    }

    public void radiate(BlockPos pos, double magnitude) {
        RadiationSource src = sources.findFirstAt(pos);

        if (src == null) {
            addNew(new RadiationSource(pos, magnitude));
        } else {
            src.radiate(magnitude);
        }
    }

    private void addNew(RadiationSource value) {
        sources.track(value, value.getPosition(), MAX_BLOCK_RANGE.getAsInt());
    }

    public boolean removeRadiationSources(int chunkX, int chunkZ) {
        if (isEmpty()) {
            return false;
        }
        List<RadiationSource> toRemove = new ArrayList<>();
        Iterator<RadiationSource> centredInChunk = sources.allCenteredInChunk(chunkX, chunkZ);
        while (centredInChunk.hasNext()) {
            toRemove.add(centredInChunk.next());
        }
        for (RadiationSource radiationSource : toRemove) {
            sources.remove(radiationSource);
        }
        return !toRemove.isEmpty();
    }

    public boolean removeRadiationSource(BlockPos pos) {
        return sources.removeAt(pos);
    }

    public boolean isEmpty() {
        return sources.isEmpty();
    }

    public void decay() {
        sources.removeIf(RadiationSource::decay);
    }

    public void clearAll() {
        this.sources.clear();
    }

    @Deprecated(forRemoval = true, since = "10.7.15") //backcompat
    public Collection<RadiationSource> values() {
        return sources.values();
    }

    public Iterator<RadiationSource> getSources(int chunkX, int chunkZ) {
        return sources.allCenteredInChunk(chunkX, chunkZ);
    }

    @Override
    @Nullable
    public ListTag serializeNBT(Provider provider) {
        if (isEmpty()) {
            return null;
        }
        ListTag tag = new ListTag(sources.values().size());
        for (RadiationSource value : sources.values()) {
            tag.add(value.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(Provider provider, ListTag nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            RadiationSource src = RadiationSource.deserializeNBT(nbt.getCompound(i));
            if (src != null) {
                addNew(src);
            }
        }
    }
}
