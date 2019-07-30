package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.interfaces.IBlockActiveTextured;
import mekanism.common.block.interfaces.IRotatableBlock;
import mekanism.common.block.property.PropertyColor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateHelper {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing");
    public static final PropertyBool activeProperty = PropertyBool.create("active");

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
            unlistedProperties.add(PropertyColor.INSTANCE);
        }
        if (properties.isEmpty() && unlistedProperties.isEmpty()) {
            return new BlockStateContainer(block);
        }
        return new ExtendedBlockState(block, properties.toArray(new IProperty[0]), unlistedProperties.toArray(new IUnlistedProperty[0]));
    }

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
                EnumFacing facing = state.getValue(BlockStateHelper.facingProperty);

                if (!((IRotatableBlock) block).canRotateTo(facing)) {
                    facing = EnumFacing.NORTH;
                }

                if (builder.length() > 0) {
                    builder.append(",");
                }

                builder.append(BlockStateHelper.facingProperty.getName());
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