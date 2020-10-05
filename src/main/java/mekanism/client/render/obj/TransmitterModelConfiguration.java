package mekanism.client.render.obj;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.client.model.data.TransmitterModelData.Diversion;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.IModelData;

public class TransmitterModelConfiguration extends VisibleModelConfiguration {

    @Nonnull
    private final TransmitterModelData modelData;

    public TransmitterModelConfiguration(IModelConfiguration internal, List<String> visibleGroups, @Nonnull IModelData modelData) {
        super(internal, visibleGroups);
        this.modelData = modelData.getData(TileEntityTransmitter.TRANSMITTER_PROPERTY);
    }

    @Nullable
    private static Direction directionForPiece(@Nonnull String piece) {
        if (piece.endsWith("down")) {
            return Direction.DOWN;
        } else if (piece.endsWith("up")) {
            return Direction.UP;
        } else if (piece.endsWith("north")) {
            return Direction.NORTH;
        } else if (piece.endsWith("south")) {
            return Direction.SOUTH;
        } else if (piece.endsWith("east")) {
            return Direction.EAST;
        } else if (piece.endsWith("west")) {
            return Direction.WEST;
        }
        return null;
    }

    private String adjustTextureName(String name) {
        Direction direction = directionForPiece(name);
        if (direction != null) {
            if (getIconStatus(direction) != IconStatus.NO_SHOW) {
                name = name.contains("glass") ? "#side_glass" : "#side";
            }
            if (MekanismConfig.client.opaqueTransmitters.get()) {
                //If we have opaque transmitters set to true, then replace our texture with the given reference
                if (name.startsWith("#side")) {
                    return name + "_opaque";
                } else if (name.startsWith("#center")) {
                    return name.contains("glass") ? "#center_glass_opaque" : "#center_opaque";
                }
            }
            return name;
        } else if (MekanismConfig.client.opaqueTransmitters.get() && name.startsWith("#side")) {
            //If we have opaque transmitters set to true, then replace our texture with the given reference
            return name + "_opaque";
        }
        return name;
    }

    public IconStatus getIconStatus(Direction side) {
        if (modelData instanceof Diversion) {
            return IconStatus.NO_SHOW;
        }
        boolean hasConnection = modelData.getConnectionType(side) != ConnectionType.NONE;
        Predicate<Direction> has = dir -> modelData.getConnectionType(dir) != ConnectionType.NONE;
        if (!hasConnection) {
            //If we don't have a connection coming out of this side
            boolean hasUpDown = has.test(Direction.DOWN) || has.test(Direction.UP);
            boolean hasNorthSouth = has.test(Direction.NORTH) || has.test(Direction.SOUTH);
            boolean hasEastWest = has.test(Direction.EAST) || has.test(Direction.WEST);
            switch (side) {
                case DOWN:
                case UP:
                    if (hasNorthSouth && !hasEastWest || !hasNorthSouth && hasEastWest) {
                        if (has.test(Direction.NORTH) && has.test(Direction.SOUTH)) {
                            return IconStatus.NO_ROTATION;
                        } else if (has.test(Direction.EAST) && has.test(Direction.WEST)) {
                            return IconStatus.ROTATE_270;
                        }
                    }
                    break;
                case NORTH:
                case SOUTH:
                    if (hasUpDown && !hasEastWest || !hasUpDown && hasEastWest) {
                        if (has.test(Direction.UP) && has.test(Direction.DOWN)) {
                            return IconStatus.NO_ROTATION;
                        } else if (has.test(Direction.EAST) && has.test(Direction.WEST)) {
                            return IconStatus.ROTATE_270;
                        }
                    }
                    break;
                case WEST:
                case EAST:
                    if (hasUpDown && !hasNorthSouth || !hasUpDown && hasNorthSouth) {
                        if (has.test(Direction.UP) && has.test(Direction.DOWN)) {
                            return IconStatus.NO_ROTATION;
                        } else if (has.test(Direction.NORTH) && has.test(Direction.SOUTH)) {
                            return IconStatus.ROTATE_270;
                        }
                    }
                    break;
            }
        }
        return IconStatus.NO_SHOW;
    }

    @Override
    public boolean isTexturePresent(@Nonnull String name) {
        return internal.isTexturePresent(adjustTextureName(name));
    }

    @Nonnull
    @Override
    public RenderMaterial resolveTexture(@Nonnull String name) {
        return internal.resolveTexture(adjustTextureName(name));
    }

    public enum IconStatus {
        NO_ROTATION(0),
        ROTATE_270(270),
        NO_SHOW(0);

        private final float angle;

        IconStatus(float angle) {
            this.angle = angle;
        }

        public float getAngle() {
            return angle;
        }
    }
}