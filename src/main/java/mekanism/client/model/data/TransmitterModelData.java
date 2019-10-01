package mekanism.client.model.data;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class TransmitterModelData implements IModelData {

    private final Map<Direction, ConnectionType> connections = new EnumMap<>(Direction.class);

    public void setConnectionData(Direction direction, ConnectionType connectionType) {
        connections.put(direction, connectionType);
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        Direction direction = getDirection(prop);
        return direction != null && connections.containsKey(direction);
    }

    @Nullable
    @Override
    public <T> T getData(ModelProperty<T> prop) {
        Direction direction = getDirection(prop);
        return direction == null ? null : (T) connections.get(direction);
    }

    @Nullable
    @Override
    public <T> T setData(ModelProperty<T> prop, T data) {
        Direction direction = getDirection(prop);
        if (direction != null) {
            if (data == null) {
                connections.remove(direction);
            } else {
                connections.put(direction, (ConnectionType) data);
            }
        }
        return data;
    }

    @Nullable
    private <T> Direction getDirection(ModelProperty<T> prop) {
        Direction direction = null;
        if (prop == ModelProperties.DOWN_CONNECTION) {
            direction = Direction.DOWN;
        } else if (prop == ModelProperties.UP_CONNECTION) {
            direction = Direction.UP;
        } else if (prop == ModelProperties.NORTH_CONNECTION) {
            direction = Direction.NORTH;
        } else if (prop == ModelProperties.SOUTH_CONNECTION) {
            direction = Direction.SOUTH;
        } else if (prop == ModelProperties.WEST_CONNECTION) {
            direction = Direction.WEST;
        } else if (prop == ModelProperties.EAST_CONNECTION) {
            direction = Direction.EAST;
        }
        return direction;
    }

    //TODO: Re-evaluate, currently used as a marker type
    public static class Diversion extends TransmitterModelData {

    }

    public static class Colorable extends TransmitterModelData {

        @Nullable
        private EnumColor color;

        public void setColor(@Nullable EnumColor color) {
            this.color = color;
        }

        @Override
        public boolean hasProperty(ModelProperty<?> prop) {
            if (prop == ModelProperties.COLOR) {
                return color != null;
            }
            return super.hasProperty(prop);
        }

        @Nullable
        @Override
        public <T> T getData(ModelProperty<T> prop) {
            if (prop == ModelProperties.COLOR) {
                return (T) color;
            }
            return super.getData(prop);
        }

        @Nullable
        @Override
        public <T> T setData(ModelProperty<T> prop, T data) {
            if (prop == ModelProperties.COLOR) {
                color = (EnumColor) data;
                return data;
            }
            return super.setData(prop, data);
        }
    }
}