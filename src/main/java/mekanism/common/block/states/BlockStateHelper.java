package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IColor;
import mekanism.api.text.EnumColor;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.property.PropertyConnection;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStateHelper {

    public static final DirectionProperty facingProperty = DirectionProperty.create("facing");
    public static final DirectionProperty horizontalFacingProperty = DirectionProperty.create("facing", Plane.HORIZONTAL);
    public static final BooleanProperty activeProperty = BooleanProperty.create("active");
    //NOTE: This currently is only using the set of colors the transporter supports as it is the only thing that needs this
    // There is a method to create this supporting all colors but it is currently unused
    public static final PropertyColor colorProperty = PropertyColor.createTransporter("color");
    //Sided pipe properties
    public static final PropertyConnection downConnectionProperty = PropertyConnection.create("down");
    public static final PropertyConnection upConnectionProperty = PropertyConnection.create("up");
    public static final PropertyConnection northConnectionProperty = PropertyConnection.create("north");
    public static final PropertyConnection southConnectionProperty = PropertyConnection.create("south");
    public static final PropertyConnection westConnectionProperty = PropertyConnection.create("west");
    public static final PropertyConnection eastConnectionProperty = PropertyConnection.create("east");
    //Cardboard Box storage
    public static final BooleanProperty storageProperty = BooleanProperty.create("storage");

    public static void fillBlockStateContainer(Block block, StateContainer.Builder<Block, BlockState> builder) {
        List<IProperty> properties = new ArrayList<>();
        if (block instanceof IStateFacing) {
            if (((IStateFacing) block).supportsAll()) {
                properties.add(facingProperty);
            } else {
                properties.add(horizontalFacingProperty);
            }
        }
        if (block instanceof IStateActive) {
            properties.add(activeProperty);
        }
        if (block instanceof IStateColor) {
            properties.add(colorProperty);
        }
        if (block instanceof IStateStorage) {
            properties.add(storageProperty);
        }
        if (block instanceof IStateConnection) {
            properties.add(downConnectionProperty);
            properties.add(upConnectionProperty);
            properties.add(northConnectionProperty);
            properties.add(southConnectionProperty);
            properties.add(westConnectionProperty);
            properties.add(eastConnectionProperty);
        }
        if (!properties.isEmpty()) {
            builder.add(properties.toArray(new IProperty[0]));
        }
    }

    public static BlockState getActualState(@Nonnull Block block, @Nonnull BlockState state, @Nonnull TileEntity tile) {
        //TODO: Make the block's actual state update when things change if needed
        if (block instanceof IStateFacing) {
            Direction facing = getFacing(tile);
            if (facing != null) {
                if (((IStateFacing) block).supportsAll()) {
                    state = state.with(facingProperty, facing);
                } else if (facing != Direction.DOWN && facing != Direction.UP) {
                    state = state.with(horizontalFacingProperty, facing);
                }
            }
        }
        if (block instanceof IStateActive) {
            state = state.with(activeProperty, ((IStateActive) block).isActive(tile));
        }
        if (block instanceof IStateColor) {
            state = state.with(BlockStateHelper.colorProperty, getColor(tile));
        }
        if (block instanceof IStateStorage) {
            state = state.with(storageProperty, isStoring(tile));
        }
        if (block instanceof IStateConnection) {
            //Add all the different connection types
            state = state.with(downConnectionProperty, getStateConnection(tile, Direction.DOWN));
            state = state.with(upConnectionProperty, getStateConnection(tile, Direction.UP));
            state = state.with(northConnectionProperty, getStateConnection(tile, Direction.NORTH));
            state = state.with(southConnectionProperty, getStateConnection(tile, Direction.SOUTH));
            state = state.with(westConnectionProperty, getStateConnection(tile, Direction.WEST));
            state = state.with(eastConnectionProperty, getStateConnection(tile, Direction.EAST));
        }
        return state;
    }

    @Nullable
    public static BlockState getStateForPlacement(Block block, @Nullable BlockState state, BlockItemUseContext context) {
        if (state == null) {
            return null;
        }
        //TODO: I don't know if there is a tile entity yet so this stuff may not really matter
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (block instanceof IStateFacing) {
            //TODO: Figure out the direction it should be
            Direction facing = getFacing(tile);
            if (facing != null) {
                if (((IStateFacing) block).supportsAll()) {
                    state = state.with(facingProperty, facing);
                } else if (facing != Direction.DOWN && facing != Direction.UP) {
                    state = state.with(horizontalFacingProperty, facing);
                }
            }
        }
        if (block instanceof IStateActive) {
            //TODO: False by default??
            state = state.with(activeProperty, ((IStateActive) block).isActive(tile));
        }
        if (block instanceof IStateColor) {
            //TODO: move this to https://github.com/MinecraftForge/MinecraftForge/pull/5564
            state = state.with(BlockStateHelper.colorProperty, getColor(tile));
        }
        if (block instanceof IStateStorage) {
            //TODO:
            state = state.with(storageProperty, isStoring(tile));
        }
        if (block instanceof IStateConnection) {
            //TODO: Move this to https://github.com/MinecraftForge/MinecraftForge/pull/5564
            //Add all the different connection types
            state = state.with(downConnectionProperty, getStateConnection(tile, Direction.DOWN));
            state = state.with(upConnectionProperty, getStateConnection(tile, Direction.UP));
            state = state.with(northConnectionProperty, getStateConnection(tile, Direction.NORTH));
            state = state.with(southConnectionProperty, getStateConnection(tile, Direction.SOUTH));
            state = state.with(westConnectionProperty, getStateConnection(tile, Direction.WEST));
            state = state.with(eastConnectionProperty, getStateConnection(tile, Direction.EAST));
        }
        return state;
    }

    @Nullable
    private static Direction getFacing(@Nonnull TileEntity tile) {
        //TODO: Make Glow Panel implement ITileDirectional
        if (tile instanceof ITileDirectional) {
            ITileDirectional directional = (ITileDirectional) tile;
            if (directional.isDirectional()) {
                return directional.getDirection();
            }
        } else if (tile instanceof TileEntityGlowPanel) {
            return ((TileEntityGlowPanel) tile).side;
        }
        return null;
    }

    @Nonnull
    private static IColor getColor(@Nonnull TileEntity tile) {
        EnumColor color = null;
        if (tile instanceof TileEntitySidedPipe) {
            color = ((TileEntitySidedPipe) tile).getRenderColor();
        }
        return color == null ? EnumColor.NONE : color;
    }

    private static boolean isStoring(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntityCardboardBox) {
            return ((TileEntityCardboardBox) tile).storedData != null;
        }
        return false;
    }

    @Nonnull
    private static ConnectionType getStateConnection(@Nonnull TileEntity tile, @Nonnull Direction side) {
        if (tile instanceof TileEntitySidedPipe) {
            return ((TileEntitySidedPipe) tile).getConnectionType(side);
        }
        return ConnectionType.NONE;
    }
}