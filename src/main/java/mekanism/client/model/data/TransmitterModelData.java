package mekanism.client.model.data;

import java.util.EnumMap;
import java.util.Map;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
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

    public boolean check(ConnectionType... types) {
        if (types.length != 6) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (connections.get(EnumUtils.DIRECTIONS[i]) != types[i]) {
                return false;
            }
        }
        return true;
    }

    public static class Diversion extends TransmitterModelData {}
}