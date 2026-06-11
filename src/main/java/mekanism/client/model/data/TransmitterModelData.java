package mekanism.client.model.data;

import java.util.Arrays;
import mekanism.client.model.data.TransmitterModelData.Diversion;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public sealed class TransmitterModelData permits Diversion {

    private final VisualConnectionStatus[] connections = new VisualConnectionStatus[EnumUtils.DIRECTIONS.length];
    private boolean hasColor;

    public void setConnectionData(ConnectionType[] connectionsIn) {
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            connections[i] = getVisualStatus(EnumUtils.DIRECTIONS[i], connectionsIn[i], connectionsIn);
        }
    }

    public VisualConnectionStatus getConnectionType(int side) {
        return connections[side];
    }

    public VisualConnectionStatus getConnectionType(Direction side) {
        return connections[side.ordinal()];
    }

    public void setHasColor(boolean hasColor) {
        this.hasColor = hasColor;
    }

    public boolean getHasColor() {
        return hasColor;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        return o instanceof TransmitterModelData other && hasColor == other.hasColor && Arrays.equals(connections, other.connections);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(connections);
        result = 31 * result + Boolean.hashCode(hasColor);
        return result;
    }

    private VisualConnectionStatus getVisualStatus(Direction side, ConnectionType sideConnection, ConnectionType[] allConnections) {
        if (sideConnection != ConnectionType.NONE || this instanceof Diversion) {
            return VisualConnectionStatus.from(sideConnection);
        }
        return getVisualStatus(
              side,
              allConnections[Direction.NORTH.ordinal()],
              allConnections[Direction.SOUTH.ordinal()],
              allConnections[Direction.EAST.ordinal()],
              allConnections[Direction.WEST.ordinal()],
              allConnections[Direction.UP.ordinal()],
              allConnections[Direction.DOWN.ordinal()]
        );
    }

    public static VisualConnectionStatus getVisualStatus(Direction side, ConnectionType northConnection, ConnectionType southConnection, ConnectionType eastConnection, ConnectionType westConnection, ConnectionType upConnection, ConnectionType downConnection) {
        //If we don't have a connection coming out of this side
        return switch (side) {
            case DOWN, UP -> getStatus(northConnection, southConnection, eastConnection, westConnection);
            case NORTH, SOUTH -> getStatus(upConnection, downConnection, eastConnection, westConnection);
            case WEST, EAST -> getStatus(upConnection, downConnection, northConnection, southConnection);
        };
    }

    private static VisualConnectionStatus getStatus(ConnectionType connectionA, ConnectionType connectionB, ConnectionType connectionC, ConnectionType connectionD) {
        boolean hasA = connectionA != ConnectionType.NONE;
        boolean hasB = connectionB != ConnectionType.NONE;
        boolean hasC = connectionC != ConnectionType.NONE;
        boolean hasD = connectionD != ConnectionType.NONE;
        //If we don't have a connection coming out of one side, but have one coming out of the perpendicular one
        if ((hasA || hasB) != (hasC || hasD)) {
            if (hasA && hasB) {
                return VisualConnectionStatus.NONE_CONTIGUOUS;
            } else if (hasC && hasD) {
                return VisualConnectionStatus.NONE_CONTIGUOUS_ROTATED;
            }
        }
        return VisualConnectionStatus.NONE;
    }

    public enum VisualConnectionStatus {
        NORMAL,
        PUSH,
        PULL,
        NONE,
        NONE_CONTIGUOUS,
        NONE_CONTIGUOUS_ROTATED;

        public String partName() {
            return switch (this) {
                case NORMAL -> "NORMAL";
                case PUSH -> "PUSH";
                case PULL -> "PULL";
                case NONE, NONE_CONTIGUOUS, NONE_CONTIGUOUS_ROTATED -> "NONE";
            };
        }

        public static final float CONTIGUOUS_ROTATION = 270 * Mth.DEG_TO_RAD;

        public static VisualConnectionStatus from(ConnectionType raw) {
            return switch (raw) {
                case NORMAL -> NORMAL;
                case PUSH -> PUSH;
                case PULL -> PULL;
                case NONE -> NONE;
            };
        }
    }

    public static final class Diversion extends TransmitterModelData {

        @Override
        public void setHasColor(boolean hasColor) {
            //Don't allow setting has color for diversion model data
        }
    }
}