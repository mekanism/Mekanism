package mekanism.client.render.obj;

import java.util.Collections;
import java.util.Objects;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.client.model.data.TransmitterModelData.Diversion;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.ConnectionType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmitterModelConfiguration extends VisibleModelConfiguration {

    @NotNull
    private final IconStatus iconStatus;

    public TransmitterModelConfiguration(IGeometryBakingContext internal, String piece, @NotNull IconStatus iconStatus) {
        super(internal, Collections.singletonList(piece));
        this.iconStatus = Objects.requireNonNull(iconStatus, "Icon status must be present.");
    }

    @Nullable
    private static Direction directionForPiece(@NotNull String piece) {
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
            if (iconStatus != IconStatus.NO_SHOW) {
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

    public static IconStatus getIconStatus(TransmitterModelData modelData, Direction side, ConnectionType connectionType) {
        if (modelData instanceof Diversion || connectionType != ConnectionType.NONE) {
            return IconStatus.NO_SHOW;
        }
        //If we don't have a connection coming out of this side
        return switch (side) {
            case DOWN, UP -> getStatus(modelData, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
            case NORTH, SOUTH -> getStatus(modelData, Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST);
            case WEST, EAST -> getStatus(modelData, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH);
        };
    }

    private static IconStatus getStatus(TransmitterModelData modelData, Direction a, Direction b, Direction c, Direction d) {
        boolean hasA = modelData.getConnectionType(a) != ConnectionType.NONE;
        boolean hasB = modelData.getConnectionType(b) != ConnectionType.NONE;
        boolean hasC = modelData.getConnectionType(c) != ConnectionType.NONE;
        boolean hasD = modelData.getConnectionType(d) != ConnectionType.NONE;
        //If we don't have a connection coming out of one side, but have one coming out of the perpendicular one
        if ((hasA || hasB) != (hasC || hasD)) {
            if (hasA && hasB) {
                return IconStatus.NO_ROTATION;
            } else if (hasC && hasD) {
                return IconStatus.ROTATE_270;
            }
        }
        return IconStatus.NO_SHOW;
    }

    @Override
    public boolean hasMaterial(@NotNull String name) {
        return internal.hasMaterial(adjustTextureName(name));
    }

    @NotNull
    @Override
    public Material getMaterial(@NotNull String name) {
        return internal.getMaterial(adjustTextureName(name));
    }

    public enum IconStatus {
        NO_ROTATION(0),
        ROTATE_270(270),
        NO_SHOW(0);

        private final float angle;

        IconStatus(float angle) {
            this.angle = angle * Mth.DEG_TO_RAD;
        }

        /**
         * Gets the angle in radians
         */
        public float getAngle() {
            return angle;
        }
    }
}