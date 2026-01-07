package mekanism.client.render.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Math;
import org.joml.Vector3fc;

public class Outlines {

    public static List<Line> extract(Level level, BlockPos pos, BlockState state, BlockStateModel model) {
        Set<Line> lines = new HashSet<>();
        for (BlockModelPart part : model.collectParts(level, pos, state, level.random)) {
            for (Direction direction : EnumUtils.DIRECTIONS) {
                for (BakedQuad quad : part.getQuads(direction)) {
                    unpackLines(quad, lines);
                }
            }

            for (BakedQuad quad : part.getQuads(null)) {
                unpackLines(quad, lines);
            }
        }
        return new ArrayList<>(lines);
    }

    private static void unpackLines(BakedQuad quad, Set<Line> lines) {
        lines.add(Line.from(quad.position0(), quad.position1()));
        lines.add(Line.from(quad.position1(), quad.position2()));
        lines.add(Line.from(quad.position2(), quad.position3()));
        lines.add(Line.from(quad.position3(), quad.position0()));
    }

    public record Line(float x1, float y1, float z1, float x2, float y2, float z2, float nX, float nY, float nZ, int hash) {

        public static Line from(Vector3fc v1, Vector3fc v2) {
            // normalise by the distance between the points
            float nX = v2.x() - v1.x();
            float nY = v2.y() - v1.y();
            float nZ = v2.z() - v1.z();
            float scalar = Math.invsqrt(Math.fma(nX, nX, Math.fma(nY, nY, nZ * nZ)));
            nX = nX * scalar;
            nY = nY * scalar;
            nZ = nZ * scalar;
            return new Line(v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), nX, nY, nZ, calculateHash(v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z()));
        }

        private static int calculateHash(float x1, float y1, float z1, float x2, float y2, float z2) {
            //Supports up to a scale of 0.005 in the json (which the miner uses for LEDs)
            int result = Long.hashCode((long) Math.min(x1, x2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.min(y1, y2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.min(z1, z2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.max(x1, x2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.max(x1, x2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.max(x1, x2) * 3_200);
            return result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || obj.getClass() != Line.class) {
                return false;
            }
            Line other = (Line) obj;
            return (Mth.equal(x1, other.x1) && Mth.equal(y1, other.y1) && Mth.equal(z1, other.z1) && Mth.equal(x2, other.x2) && Mth.equal(y2, other.y2) && Mth.equal(z2, other.z2)) ||
                   (Mth.equal(x1, other.x2) && Mth.equal(y1, other.y2) && Mth.equal(z1, other.z2) && Mth.equal(x2, other.x1) && Mth.equal(y2, other.y1) && Mth.equal(z2, other.z1));
        }
    }
}
