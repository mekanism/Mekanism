package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.IColor;
import mekanism.common.base.IActiveState;
import mekanism.common.block.interfaces.IBlockActiveTextured;
import mekanism.common.block.interfaces.IRotatableBlock;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.property.PropertyConnection;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateHelper {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing");
    public static final PropertyBool activeProperty = PropertyBool.create("active");
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

    public static BlockStateContainer getBlockState(Block block) {
        List<IProperty> properties = new ArrayList<>();
        if (block instanceof IStateFacing) {
            properties.add(facingProperty);
        }
        if (block instanceof IStateActive) {
            properties.add(activeProperty);
        }
        if (block instanceof IStateColor) {
            properties.add(colorProperty);
        }
        if (block instanceof IStateConnection) {
            properties.add(downConnectionProperty);
            properties.add(upConnectionProperty);
            properties.add(northConnectionProperty);
            properties.add(southConnectionProperty);
            properties.add(westConnectionProperty);
            properties.add(eastConnectionProperty);
        }
        if (properties.isEmpty()) {
            return new BlockStateContainer(block);
        }
        return new ExtendedBlockState(block, properties.toArray(new IProperty[0]), new IUnlistedProperty[0]);
    }

    public static IBlockState getActualState(@Nonnull Block block, @Nonnull IBlockState state, @Nonnull TileEntity tile) {
        if (block instanceof IStateFacing) {
            EnumFacing facing = getFacing(tile);
            if (facing != null) {
                state = state.withProperty(facingProperty, facing);
            }
        }
        if (block instanceof IStateActive) {
            state = state.withProperty(activeProperty, getActive(tile));
        }
        if (block instanceof IStateColor) {
            state = state.withProperty(BlockStateHelper.colorProperty, getColor(tile));
        }
        if (block instanceof IStateConnection) {
            //Add all the different connection types
            state = state.withProperty(downConnectionProperty, getStateConnection(tile, EnumFacing.DOWN));
            state = state.withProperty(upConnectionProperty, getStateConnection(tile, EnumFacing.UP));
            state = state.withProperty(northConnectionProperty, getStateConnection(tile, EnumFacing.NORTH));
            state = state.withProperty(southConnectionProperty, getStateConnection(tile, EnumFacing.SOUTH));
            state = state.withProperty(westConnectionProperty, getStateConnection(tile, EnumFacing.WEST));
            state = state.withProperty(eastConnectionProperty, getStateConnection(tile, EnumFacing.EAST));
        }
        return state;
    }

    @Nullable
    private static EnumFacing getFacing(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntityBasicBlock) {
            return ((TileEntityBasicBlock) tile).facing;
        } else if (tile instanceof TileEntityGlowPanel) {
            return ((TileEntityGlowPanel) tile).side;
        }
        return null;
    }

    private static boolean getActive(@Nonnull TileEntity tile) {
        if (tile instanceof IActiveState) {
            return ((IActiveState) tile).getActive();
        } else if (tile instanceof TileEntitySuperheatingElement) {
            TileEntitySuperheatingElement heating = (TileEntitySuperheatingElement) tile;
            if (heating.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(heating.multiblockUUID) != null) {
                return SynchronizedBoilerData.clientHotMap.get(heating.multiblockUUID);
            }
        }
        return false;
    }

    @Nonnull
    private static IColor getColor(@Nonnull TileEntity tile) {
        EnumColor color = null;
        if (tile instanceof TileEntitySidedPipe) {
            color = ((TileEntitySidedPipe) tile).getRenderColor();
        }
        return color == null ? EnumColor.NONE : color;
    }

    @Nonnull
    private static ConnectionType getStateConnection(@Nonnull TileEntity tile, @Nonnull EnumFacing side) {
        if (tile instanceof TileEntitySidedPipe) {
            return ((TileEntitySidedPipe) tile).getConnectionType(side);
        }
        return ConnectionType.NONE;
    }

    //TODO: Does having this actually do anything, or does it grab stuff by default anyways (I think this can be used on the transmitters to fix them needing all the variants)
    public static class MekanismBlockStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            Block block = state.getBlock();
            StringBuilder builder = new StringBuilder();

            if (block instanceof IBlockActiveTextured) {
                builder.append(activeProperty.getName());
                builder.append("=");
                builder.append(state.getValue(activeProperty));
            }

            if (block instanceof IRotatableBlock) {
                EnumFacing facing = state.getValue(facingProperty);

                if (!((IRotatableBlock) block).canRotateTo(facing)) {
                    facing = EnumFacing.NORTH;
                }

                if (builder.length() > 0) {
                    builder.append(",");
                }

                builder.append(facingProperty.getName());
                builder.append("=");
                builder.append(facing.getName());
            }

            if (builder.length() == 0) {
                builder.append("normal");
            }
            return new ModelResourceLocation(block.getRegistryName(), builder.toString());
        }
    }
}