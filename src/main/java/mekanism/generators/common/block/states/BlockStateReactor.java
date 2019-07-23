package mekanism.generators.common.block.states;

import javax.annotation.Nonnull;
import mekanism.common.block.interfaces.IBlockActiveTextured;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateReactor extends ExtendedBlockState {

    public static final PropertyBool activeProperty = PropertyBool.create("active");

    public BlockStateReactor(Block block) {
        super(block, new IProperty[]{activeProperty}, new IUnlistedProperty[]{});
    }

    public static class ReactorBlockStateMapper extends StateMapperBase {

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
            if (builder.length() == 0) {
                builder.append("normal");
            }
            ResourceLocation baseLocation = block.getRegistryName();
            return new ModelResourceLocation(baseLocation, builder.toString());
        }
    }
}