package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class PathfinderCache {

    private PathfinderCache() {
    }

    private static final Map<UUID, Map<PathData, CachedPath>> cachedPaths = new Object2ObjectOpenHashMap<>();

    public static void onChanged(InventoryNetwork... networks) {
        for (InventoryNetwork network : networks) {
            cachedPaths.remove(network.getUUID());
        }
    }

    /**
     * @apiNote Expects positions to be unmodifiable/immutable (at the very least not mutated after being passed).
     */
    public static void addCachedPath(LogisticalTransporterBase start, BlockPos destination, Direction destinationSide, List<BlockPos> positions, double cost) {
        PathData data = new PathData(start.getTilePos(), destination, destinationSide);
        cachedPaths.computeIfAbsent(start.getTransmitterNetwork().getUUID(), uuid -> new Object2ObjectOpenHashMap<>()).put(data, new CachedPath(positions, cost));
    }

    @Nullable
    public static CachedPath getCache(LogisticalTransporterBase start, BlockPos end, Set<Direction> sides) {
        UUID uuid = start.getTransmitterNetwork().getUUID();
        Map<PathData, CachedPath> pathMap = cachedPaths.get(uuid);
        if (pathMap != null) {
            BlockPos startPos = start.getTilePos();
            return sides.stream().map(side -> pathMap.get(new PathData(startPos, end, side))).filter(Objects::nonNull)
                  .min(Comparator.comparingDouble(CachedPath::cost)).orElse(null);
        }
        return null;
    }

    public static void reset() {
        cachedPaths.clear();
    }

    public record CachedPath(List<BlockPos> path, double cost) {
    }

    private record PathData(BlockPos startTransporter, BlockPos end, Direction endSide) {
    }
}