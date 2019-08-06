package mekanism.common.content.transporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Coord4D;
import net.minecraft.util.Direction;

public class PathfinderCache {

    private static Map<PathData, List<Coord4D>> cachedPaths = new HashMap<>();

    public static void onChanged(Coord4D location) {
        reset();
    }

    public static void addCachedPath(PathData data, List<Coord4D> coords) {
        cachedPaths.put(data, coords);
    }

    public static List<Coord4D> getCache(Coord4D start, Coord4D end, Set<Direction> sides) {
        List<Coord4D> ret = null;
        for (Direction side : sides) {
            List<Coord4D> test = cachedPaths.get(new PathData(start, end, side));
            if (ret == null || (test != null && test.size() < ret.size())) {
                ret = test;
            }
        }
        return ret;
    }

    public static void reset() {
        cachedPaths.clear();
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