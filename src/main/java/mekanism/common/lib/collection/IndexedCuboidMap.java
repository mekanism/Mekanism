package mekanism.common.lib.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.util.ChunkUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Stores a map of BoundingBox+Centre to VALUE, indexed by chunk for quickly determining relevant boxes to scan.
 * <p>
 * Values MUST support equals, else removal by value will not work.
 */
@NothingNullByDefault
public class IndexedCuboidMap<VALUE> {

    private final BiLongMultimap<CentredBoundingBox> chunkIndex = new BiLongMultimap<>();
    private final Map<CentredBoundingBox, VALUE> valueMap = new HashMap<>();

    /**
     * Add a value to the map with a fixed radius in all axes
     * @param value value to store
     * @param center the centre or controlling position
     * @param blockRadius the fixed radius to add/subtract from the centre position (inclusive)
     */
    public void track(VALUE value, BlockPos center, int blockRadius) {
        track(
              value,
              center,
              center.getX() - blockRadius,
              center.getY() - blockRadius,
              center.getZ() - blockRadius,
              center.getX() + blockRadius,
              center.getY() + blockRadius,
              center.getZ() + blockRadius);
    }

    /**
     * Adds a value to the map with an explicitly defined box
     * @param value value to add to the map
     * @param center centre or controlling position. Must be within the defined box.
     * @param minX minimum X pos of the box (inclusive)
     * @param minY minimum Y pos of the box (inclusive)
     * @param minZ minimum Z pos of the box (inclusive)
     * @param maxX maximum X pos of the box (inclusive)
     * @param maxY maximum Y pos of the box (inclusive)
     * @param maxZ maximum Z pos of the box (inclusive)
     */
    public void track(VALUE value, BlockPos center, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){

        CentredBoundingBox box = new CentredBoundingBox(center, minX, minY, minZ, maxX, maxY, maxZ);
        if (!box.isInside(center)) {
            throw new IllegalArgumentException("center must be within the box");
        }
        VALUE previous = valueMap.put(box, value);
        if (previous != null) {
            return;//chunks should already be baked
        }

        int minChunkX = SectionPos.blockToSectionCoord(box.minX());
        int minChunkZ = SectionPos.blockToSectionCoord(box.minZ());
        int maxChunkX = SectionPos.blockToSectionCoord(box.maxX());
        int maxChunkZ = SectionPos.blockToSectionCoord(box.maxZ());

        if (minChunkX == maxChunkX && minChunkZ == maxChunkZ) {
            chunkIndex.put(ChunkPos.asLong(minChunkX, minChunkZ), box);
        } else {
            chunkIndex.putAll(ChunkUtils.rangeClosed(minChunkX, minChunkZ, maxChunkX, maxChunkZ), box);
        }
    }

    /**
     * Remove a value from the map
     * @param value value to remove
     */
    public void remove(VALUE value) {
        List<CentredBoundingBox> toRemove = new ArrayList<>(valueMap.size());
        for (Entry<CentredBoundingBox, VALUE> valueEntry : valueMap.entrySet()) {
            if (valueEntry.getValue().equals(value)) {
                toRemove.add(valueEntry.getKey());
            }
        }
        for (CentredBoundingBox box : toRemove) {
            valueMap.remove(box);
            chunkIndex.removeValue(box);
        }
    }

    /**
     * Remove values from the map with a specified centre pos
     *
     * @param center centre position to remove
     *
     * @return true if a value was removed
     */
    public boolean removeAt(BlockPos center) {
        List<CentredBoundingBox> toRemove = new ArrayList<>(valueMap.size());
        for (Entry<CentredBoundingBox, VALUE> valueEntry : valueMap.entrySet()) {
            if (valueEntry.getKey().center.equals(center)) {
                toRemove.add(valueEntry.getKey());
            }
        }
        for (CentredBoundingBox box : toRemove) {
            valueMap.remove(box);
            chunkIndex.removeValue(box);
        }
        return !toRemove.isEmpty();
    }

    /**
     * Find values which have the position as inside the box (edge inclusive)
     * @param searchPos position to search
     * @return an iterator of matching values
     */
    public Iterator<VALUE> find(BlockPos searchPos) {
        Set<CentredBoundingBox> values = chunkIndex.getValues(ChunkPos.asLong(searchPos));
        if (values == null) {
            return Collections.emptyIterator();
        }
        return new FilterTransformIterator<>(values.iterator()) {
            @Override
            protected @Nullable VALUE filterTransform(CentredBoundingBox box) {
                if (box.isInside(searchPos)) {
                    return valueMap.get(box);
                }
                return null;
            }
        };
    }

    /**
     * Finds the FIRST value with a centre position, regardless of its box (aside from searching with it).
     *
     * @param centre the centre pos to check
     *
     * @return the first value found, or null
     */
    @Nullable
    public VALUE findFirstAt(BlockPos centre) {
        Set<CentredBoundingBox> values = chunkIndex.getValues(ChunkPos.asLong(centre));
        if (values == null) {
            return null;
        }
        for (CentredBoundingBox box : values) {
            if (centre.equals(box.center)) {
                return Objects.requireNonNull(valueMap.get(box), "Box existed with no value??");
            }
        }
        return null;
    }

    /**
     * Find values with a centre in the specified chunk
     * @param chunkX the X pos of the chunk to check
     * @param chunkZ the Z pos of the chunk to check
     * @return an iterator of matching values
     */
    public Iterator<VALUE> allCentredInChunk(int chunkX, int chunkZ) {
        return allCentredInChunk(ChunkPos.asLong(chunkX, chunkZ));
    }

    /**
     * Find values with a centre in the specified chunk
     * @param chunkPos the packed chunk position
     * @return an iterator of matching values
     */
    public Iterator<VALUE> allCentredInChunk(long chunkPos) {
        Set<CentredBoundingBox> values = chunkIndex.getValues(chunkPos);
        if (values == null) {
            return Collections.emptyIterator();
        }
        return new FilterTransformIterator<>(values.iterator()) {
            @Override
            protected @Nullable VALUE filterTransform(CentredBoundingBox box) {
                if (ChunkPos.asLong(box.center) == chunkPos) {
                    return valueMap.get(box);
                }
                return null;
            }
        };
    }

    /**
     * @return All contained values. DO NOT MODIFY
     */
    public Collection<VALUE> values() {
        return valueMap.values();
    }

    public boolean isEmpty() {
        return valueMap.isEmpty();
    }

    @VisibleForTesting
    boolean indexIsEmpty() {
        return chunkIndex.isEmpty();
    }

    public void removeIf(Predicate<VALUE> predicate) {
        Iterator<Entry<CentredBoundingBox, VALUE>> iterator = valueMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<CentredBoundingBox, VALUE> entry = iterator.next();
            if (predicate.test(entry.getValue())) {
                iterator.remove();
                chunkIndex.removeValue(entry.getKey());
            }
        }
    }

    public void clear() {
        this.valueMap.clear();
        this.chunkIndex.clear();
    }

    /**
     * Like BoundingBox, but with a centre/controlling position
     */
    private record CentredBoundingBox(BlockPos center, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public boolean isInside(Vec3i vector) {
            return this.isInside(vector.getX(), vector.getY(), vector.getZ());
        }

        public boolean isInside(int x, int y, int z) {
            return x >= this.minX
                   && x <= this.maxX
                   && z >= this.minZ
                   && z <= this.maxZ
                   && y >= this.minY
                   && y <= this.maxY;
        }
    }
}
