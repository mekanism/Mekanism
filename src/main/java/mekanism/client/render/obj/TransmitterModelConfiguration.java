package mekanism.client.render.obj;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.model.data.ModelProperties;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.IModelData;

public class TransmitterModelConfiguration extends VisibleModelConfiguration {

    @Nonnull
    private IModelData modelData;
    @Nullable
    private EnumColor color;

    public TransmitterModelConfiguration(IModelConfiguration internal, List<String> visibleGroups, @Nonnull IModelData modelData, @Nullable EnumColor color) {
        super(internal, visibleGroups);
        this.modelData = modelData;
        this.color = color;
    }

    @Nullable
    private static Direction directionForPiece(@Nonnull String piece) {
        switch (piece) {
            case "#center_down":
                return Direction.DOWN;
            case "#center_up":
                return Direction.UP;
            case "#center_north":
                return Direction.NORTH;
            case "#center_south":
                return Direction.SOUTH;
            case "#center_east":
                return Direction.EAST;
            case "#center_west":
                return Direction.WEST;
        }
        return null;
    }

    private String adjustTextureName(String name) {
        Direction dir = directionForPiece(name);
        if (dir != null) {
            return getOverrideTexture(name, dir);
        } else if (MekanismConfig.client.opaqueTransmitters.get() && name.equals("#side")) {
            //If we have opaque transmitters set to true, then replace our texture with the given reference
            return "#side_opaque";
        }
        return name;
    }

    //TODO: Use??
    public float[] getOverrideColor() {
        if (color != null && MinecraftForgeClient.getRenderLayer() == RenderType.func_228645_f_()) {
            return new float[]{color.getColor(0), color.getColor(1), color.getColor(2), 1};
        }
        return null;
    }

    public String getOverrideTexture(String name, Direction direction) {
        boolean sideIconOverride = getIconStatus(direction) > 0;
        if (MinecraftForgeClient.getRenderLayer() == RenderType.func_228645_f_()) {
            if (!sideIconOverride && name.startsWith("#center")) {
                if (color != null) {
                    //TODO: 1.15 - Fix this
                    //return transporter_center_color[opaqueVal];
                }
                return MekanismConfig.client.opaqueTransmitters.get() ? "#center_opaque" : name;
            }
            if (color != null) {
                //TODO: 1.15 - Fix this
                //return transporter_side_color[opaqueVal];
            }
            return MekanismConfig.client.opaqueTransmitters.get() ? "#side_opaque" : "#side";
        } else if (sideIconOverride) {
            return MekanismConfig.client.opaqueTransmitters.get() ? "#side_opaque" : "#side";
        }
        if (MekanismConfig.client.opaqueTransmitters.get()) {
            //If we have opaque transmitters set to true, then replace our texture with the given reference
            if (name.equals("#side")) {
                return "#side_opaque";
            } else if (name.startsWith("#center")) {
                return "#center_opaque";
            }
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