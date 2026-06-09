package mekanism.common.lib.radiation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.SerializationConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.listener.ConfigBasedCachedIntSupplier;
import mekanism.common.lib.collection.IndexedCuboidMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class RadiationLevelData implements ValueIOSerializable {
    private static final IntSupplier MAX_BLOCK_RANGE = new ConfigBasedCachedIntSupplier(() -> {
        int chunkRadius = MekanismConfig.general.radiationChunkCheckRadius.get();
        // we only compute exposure when within the MAX_RANGE bounds
        return chunkRadius * 16;
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

    public Iterator<RadiationSource> getSources(int chunkX, int chunkZ) {
        return sources.allCenteredInChunk(chunkX, chunkZ);
    }

    @Override
    public void serialize(ValueOutput output) {
        if (!isEmpty()) {
            TypedOutputList<RadiationSource> sourceOutput = output.list(SerializationConstants.RADIATION, RadiationSource.CODEC);
            for (RadiationSource source : sources.values()) {
                sourceOutput.add(source);
            }
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        //TODO - 26.1: Re-evaluate if we want this to be stored under radiation, as previously it was just as a list without needing a key
        // Also figure out if this properly supports being lenient if say one radiation source is of a broken format
        for (RadiationSource source : input.listOrEmpty(SerializationConstants.RADIATION, RadiationSource.CODEC)) {
            addNew(source);
        }
    }
}
