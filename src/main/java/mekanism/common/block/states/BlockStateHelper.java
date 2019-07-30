package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.base.IActiveState;
import mekanism.common.block.interfaces.IBlockActiveTextured;
import mekanism.common.block.interfaces.IRotatableBlock;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.property.PropertyConnection;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateHelper {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing");
    public static final PropertyBool activeProperty = PropertyBool.create("active");
    //NOTE: This currently is only using the set of colors the transporter supports as it is the only thing that needs this
    // There is a method to create this supporting all colors but it is currently unused
    public static final PropertyColor colorProperty = PropertyColor.createTransporter("color");

    public static BlockStateContainer getBlockState(Block block) {
        List<IUnlistedProperty> unlistedProperties = new ArrayList<>();
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
        if (block instanceof IStateOBJ) {
            unlistedProperties.add(OBJProperty.INSTANCE);
        }
        if (block instanceof IStateConnection) {
            //TODO: Make the connection be fully qualified into the blockstate
            unlistedProperties.add(PropertyConnection.INSTANCE);
        }
        if (properties.isEmpty() && unlistedProperties.isEmpty()) {
            return new BlockStateContainer(block);
        }
        return new ExtendedBlockState(block, properties.toArray(new IProperty[0]), unlistedProperties.toArray(new IUnlistedProperty[0]));
    }

    public static IBlockState getActualState(@Nonnull Block block, @Nonnull IBlockState state, @Nonnull TileEntityBasicBlock tile) {
        if (block instanceof IStateFacing && tile.facing != null) {
            state = state.withProperty(facingProperty, tile.facing);
        }
        if (block instanceof IStateActive && tile instanceof IActiveState) {
            state = state.withProperty(activeProperty, ((IActiveState) tile).getActive());
        }
        return state;
    }

    //TODO: Does having this actually do anything, or does it grab stuff by default anyways
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