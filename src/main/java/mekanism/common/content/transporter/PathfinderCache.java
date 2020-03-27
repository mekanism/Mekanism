package mekanism.common.content.transporter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.Coord4D;
import net.minecraft.util.Direction;

public class PathfinderCache {

    private static Map<PathData, CachedPath> cachedPaths = new Object2ObjectOpenHashMap<>();

    public static void onChanged(Coord4D location) {
        reset();
    }

    public static void addCachedPath(PathData data, List<Coord4D> coords, double cost) {
        cachedPaths.put(data, new CachedPath(coords, cost));
    }

    public static CachedPath getCache(Coord4D start, Coord4D end, Set<Direction> sides) {
        CachedPath ret = null;
        for (Direction side : sides) {
            CachedPath test = cachedPaths.get(new PathData(start, end, side));
            if (ret == null || (test != null && test.getCost() < ret.getCost())) {
                ret = test;
            }
        }
        return ret;
    }

    public static void reset() {
        cachedPaths.clear();
    }

    public static class CachedPath {

        private List<Coord4D> path;
        private double cost;

        public CachedPath(List<Coord4D> path, double cost) {
            this.path = path;
            this.cost = cost;
        }

        public List<Coord4D> getPath() {
            return path;
        }

        public double getCost() {
            return cost;
        }
    }

    public static class PathData {

        private final Coord4D startTransporter;
        private final Coord4D end;
        private final Direction endSide;
        private final int hash;

        public PathData(Coord4D s, Coord4D e, Direction es) {
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
            if (obj instanceof PathData) {
                PathData data = (PathData) obj;
                return data.startTransporter.equals(startTransporter) && data.end.equals(end) && data.endSide.equals(endSide);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}