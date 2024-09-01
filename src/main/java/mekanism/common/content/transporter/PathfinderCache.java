package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterPathfinder.Pathfinder;
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

    public static CachedPath addCachedPath(LogisticalTransporterBase start, BlockPos destination, Pathfinder pathfinder) {
        CachedPath cachedPath = new CachedPath(pathfinder.getPath(), pathfinder.getFinalScore());
        PathData data = new PathData(start.getBlockPos(), destination, pathfinder.getSide());
        cachedPaths.computeIfAbsent(start.getTransmitterNetwork().getUUID(), uuid -> new HashMap<>()).put(data, cachedPath);
        return cachedPath;
    }

    @Nullable
    public static CachedPath getCache(LogisticalTransporterBase start, BlockPos end, Set<Direction> sides) {
        CachedPath ret = null;
        UUID uuid = start.getTransmitterNetwork().getUUID();
        Map<PathData, CachedPath> pathMap = cachedPaths.get(uuid);
        if (pathMap != null) {
            BlockPos startPos = start.getBlockPos();
            for (Direction side : sides) {
                CachedPath test = pathMap.get(new PathData(startPos, end, side));
                if (test != null) {
                    if (ret == null || test.cost() < ret.cost()) {
                        ret = test;
                    }
                }
            }
        }
        return ret;
    }

    @Nullable
    public static CachedPath getSingleCache(LogisticalTransporterBase start, BlockPos end, Direction side) {
        UUID uuid = start.getTransmitterNetwork().getUUID();
        Map<PathData, CachedPath> pathMap = cachedPaths.get(uuid);
        if (pathMap != null) {
            BlockPos startPos = start.getBlockPos();
            return pathMap.get(new PathData(startPos, end, side));
        }
        return null;
    }

    public static void reset() {
        cachedPaths.clear();
    }

    public record CachedPath(LongList path, double cost) {
    }

    private record PathData(BlockPos startTransporter, BlockPos end, Direction endSide) {
    }
}