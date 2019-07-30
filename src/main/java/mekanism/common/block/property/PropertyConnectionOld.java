package mekanism.common.block.property;

import java.util.Arrays;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraftforge.common.property.IUnlistedProperty;

//TODO: Search for name before delete
public class PropertyConnectionOld implements IUnlistedProperty<PropertyConnectionOld> {

    public static PropertyConnectionOld INSTANCE = new PropertyConnectionOld();

    public byte connectionByte;
    public byte transmitterConnections;
    public ConnectionType[] connectionTypes;
    public boolean renderCenter;

    public PropertyConnectionOld() {
    }

    public PropertyConnectionOld(byte b, byte b1, ConnectionType[] types, boolean center) {
        connectionByte = b;
        transmitterConnections = b1;
        connectionTypes = types;
        renderCenter = center;
    }

    @Override
    public String getName() {
        return "connection";
    }

    @Override
    public boolean isValid(PropertyConnectionOld value) {
        return true;
    }

    @Override
    public Class<PropertyConnectionOld> getType() {
        return PropertyConnectionOld.class;
    }

    @Override
    public String valueToString(PropertyConnectionOld value) {
        return value.connectionByte + "_" + value.transmitterConnections + "_" + Arrays.toString(value.connectionTypes) + "_" + value.renderCenter;
    }
}