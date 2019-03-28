package mekanism.common.content.transporter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import net.minecraft.util.EnumFacing;

public class PathfinderCache {

    public static Map<PathData, List<Coord4D>> cachedPaths = new HashMap<>();

    public static void onChanged(Coord4D location) {
        reset();
    }

    public static List<Coord4D> getCache(Coord4D start, Coord4D end, EnumSet<EnumFacing> sides) {
        List<Coord4D> ret = null;

        for (EnumFacing side : sides) {
            PathData data = new PathData(start, end, side);

            List<Coord4D> test = cachedPaths.get(data);

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

        public Coord4D startTransporter;

        public Coord4D end;
        public EnumFacing endSide;

        public PathData(Coord4D s, Coord4D e, EnumFacing es) {
            startTransporter = s;

            end = e;
            endSide = es;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PathData &&
                  ((PathData) obj).startTransporter.equals(startTransporter) &&
                  ((PathData) obj).end.equals(end) &&
                  ((PathData) obj).endSide.equals(endSide);
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + startTransporter.hashCode();
            code = 31 * code + end.hashCode();
            code = 31 * code + endSide.hashCode();
            return code;
        }
    }
}
