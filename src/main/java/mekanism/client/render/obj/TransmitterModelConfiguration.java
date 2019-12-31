package mekanism.client.render.obj;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.model.data.ModelProperties;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.IModelData;

public class TransmitterModelConfiguration extends VisibleModelConfiguration {

    @Nonnull
    private IModelData modelData;

    public TransmitterModelConfiguration(IModelConfiguration internal, List<String> visibleGroups, @Nonnull IModelData modelData) {
        super(internal, visibleGroups);
        this.modelData = modelData;
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
            if (getIconStatus(direction) > 0) {
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

    public boolean shouldRotate(Direction direction) {
        return getIconStatus(direction) == 2;
    }

    public byte getIconStatus(Direction side) {
        if (modelData instanceof TransmitterModelData.Diversion) {
            return (byte) 0;
        }
        boolean hasDown = modelData.getData(ModelProperties.DOWN_CONNECTION) != ConnectionType.NONE;
        boolean hasUp = modelData.getData(ModelProperties.UP_CONNECTION) != ConnectionType.NONE;
        boolean hasNorth = modelData.getData(ModelProperties.NORTH_CONNECTION) != ConnectionType.NONE;
        boolean hasSouth = modelData.getData(ModelProperties.SOUTH_CONNECTION) != ConnectionType.NONE;
        boolean hasWest = modelData.getData(ModelProperties.WEST_CONNECTION) != ConnectionType.NONE;
        boolean hasEast = modelData.getData(ModelProperties.EAST_CONNECTION) != ConnectionType.NONE;
        boolean hasConnection = false;
        if (side == Direction.DOWN) {
            hasConnection = hasDown;
        } else if (side == Direction.UP) {
            hasConnection = hasUp;
        } else if (side == Direction.NORTH) {
            hasConnection = hasNorth;
        } else if (side == Direction.SOUTH) {
            hasConnection = hasSouth;
        } else if (side == Direction.WEST) {
            hasConnection = hasWest;
        } else if (side == Direction.EAST) {
            hasConnection = hasEast;
        }
        if (!hasConnection) {
            //If we don't have a connection coming out of this side
            boolean hasUpDown = hasDown || hasUp;
            boolean hasNorthSouth = hasNorth || hasSouth;
            boolean hasEastWest = hasEast || hasWest;
            switch (side) {
                case DOWN:
                case UP:
                    if (hasNorthSouth && !hasEastWest || !hasNorthSouth && hasEastWest) {
                        if (hasNorth && hasSouth) {
                            return (byte) 1;
                        } else if (hasEast && hasWest) {
                            return (byte) 2;
                        }
                    }
                    break;
                case NORTH:
                case SOUTH:
                    if (hasUpDown && !hasEastWest || !hasUpDown && hasEastWest) {
                        if (hasUp && hasDown) {
                            return (byte) 1;
                        } else if (hasEast && hasWest) {
                            return (byte) 2;
                        }
                    }
                    break;
                case WEST:
                case EAST:
                    if (hasUpDown && !hasNorthSouth || !hasUpDown && hasNorthSouth) {
                        if (hasUp && hasDown) {
                            return (byte) 1;
                        } else if (hasNorth && hasSouth) {
                            return (byte) 2;
                        }
                    }
                    break;
            }
        }
        return (byte) 0;
    }

    @Override
    public boolean isTexturePresent(@Nonnull String name) {
        return internal.isTexturePresent(adjustTextureName(name));
    }

    @Nonnull
    @Override
    public Material resolveTexture(@Nonnull String name) {
        return internal.resolveTexture(adjustTextureName(name));
    }
}