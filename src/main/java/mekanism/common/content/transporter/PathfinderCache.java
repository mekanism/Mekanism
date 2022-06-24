package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class PathfinderCache {

    private PathfinderCache() {
    }

    private static final Map<UUID, Map<PathData, CachedPath>> cachedPaths = new Object2ObjectOpenHashMap<>();

    public static void onChanged(InventoryNetwork... networks) {
        for (InventoryNetwork network : networks) {
            cachedPaths.remove(network.getUUID());
        }
    }

    public static void addCachedPath(LogisticalTransporterBase start, PathData data, List<BlockPos> positions, double cost) {
        cachedPaths.computeIfAbsent(start.getTransmitterNetwork().getUUID(), uuid -> new Object2ObjectOpenHashMap<>()).put(data, new CachedPath(positions, cost));
    }

    public static CachedPath getCache(LogisticalTransporterBase start, BlockPos end, Set<Direction> sides) {
        CachedPath ret = null;
        UUID uuid = start.getTransmitterNetwork().getUUID();
        if (cachedPaths.containsKey(uuid)) {
            Map<PathData, CachedPath> pathMap = cachedPaths.get(uuid);
            for (Direction side : sides) {
                CachedPath test = pathMap.get(new PathData(start.getTilePos(), end, side));
                if (ret == null || (test != null && test.cost() < ret.cost())) {
                    ret = test;
                }
            }
        }
        return ret;
    }

    public static void reset() {
        cachedPaths.clear();
    }

    public record CachedPath(List<BlockPos> path, double cost) {
    }

    public static class PathData {

        private final BlockPos startTransporter;
        private final BlockPos end;
        private final Direction endSide;
        private final int hash;

        public PathData(BlockPos s, BlockPos e, Direction es) {
            startTransporter = s;
            end = e;
            endSide = es;
            int code = 1;
            code = 31 * code + startTransporter.hashCode();
            code = 31 * code + end.hashCode();
            code = 31 * code + endSide.hashCode();
            hash = code;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PathData data && data.startTransporter.equals(startTransporter) && data.end.equals(end) && data.endSide == endSide;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}