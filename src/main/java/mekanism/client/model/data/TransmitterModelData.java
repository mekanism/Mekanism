package mekanism.client.model.data;

import java.util.EnumMap;
import java.util.Map;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.util.Direction;

public class TransmitterModelData {

    private final Map<Direction, ConnectionType> connections = new EnumMap<>(Direction.class);
    private boolean hasColor;

    public void setConnectionData(Direction direction, ConnectionType connectionType) {
        connections.put(direction, connectionType);
    }

    public Map<Direction, ConnectionType> getConnectionsMap() {
        return connections;
    }

    public ConnectionType getConnectionType(Direction side) {
        return connections.get(side);
    }

    public void setHasColor(boolean hasColor) {
        this.hasColor = hasColor;
    }

    public boolean getHasColor() {
        return hasColor;
    }

    public static class Diversion extends TransmitterModelData {}
}