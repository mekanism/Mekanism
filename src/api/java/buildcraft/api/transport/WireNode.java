package buildcraft.api.transport;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class WireNode {
    public final BlockPos pos;
    public final EnumWirePart part;
    private final int hash;

    public WireNode(BlockPos pos, EnumWirePart part) {
        this.pos = pos;
        this.part = part;
        hash = pos.hashCode() * 31 + part.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        WireNode other = (WireNode) obj;
        return part == other.part //
            && pos.equals(other.pos);
    }

    @Override
    public String toString() {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", " + part + ")";
    }

    public WireNode offset(EnumFacing face) {
        int nx = (part.x == AxisDirection.POSITIVE ? 1 : 0) + face.getFrontOffsetX();
        int ny = (part.y == AxisDirection.POSITIVE ? 1 : 0) + face.getFrontOffsetY();
        int nz = (part.z == AxisDirection.POSITIVE ? 1 : 0) + face.getFrontOffsetZ();
        EnumWirePart nPart = EnumWirePart.get(nx, ny, nz);
        if (nx < 0 || ny < 0 || nz < 0 || nx > 1 || ny > 1 || nz > 1) {
            return new WireNode(pos.offset(face), nPart);
        } else {
            return new WireNode(pos, nPart);
        }
    }

    public Map<EnumFacing, WireNode> getAllPossibleConnections() {
        Map<EnumFacing, WireNode> map = new EnumMap<>(EnumFacing.class);

        for (EnumFacing face : EnumFacing.VALUES) {
            map.put(face, offset(face));
        }
        return map;
    }
}
