package mekanism.common.block.states;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockCardboardBox;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

public class BlockStateCardboardBox extends BlockStateContainer {

    public static PropertyBool storageProperty = PropertyBool.create("storage");

    public BlockStateCardboardBox(BlockCardboardBox block) {
        super(block, storageProperty);
    }

    public static class CardboardBoxStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            String property = "storage=" + state.getValue(storageProperty);
            return new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "cardboard_box"), property);
        }
    }
}