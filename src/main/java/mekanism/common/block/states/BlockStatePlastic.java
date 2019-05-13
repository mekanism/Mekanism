package mekanism.common.block.states;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockPlastic;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

public class BlockStatePlastic extends BlockStateContainer {

    public static PropertyEnum<EnumDyeColor> colorProperty = PropertyEnum.create("color", EnumDyeColor.class);

    public BlockStatePlastic(BlockPlastic block) {
        super(block, colorProperty);
    }

    public enum PlasticBlockType implements IStringSerializable {
        PLASTIC,
        SLICK,
        GLOW,
        REINFORCED,
        ROAD;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static class PlasticBlockStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            PlasticBlockType type = ((BlockPlastic) state.getBlock()).type;
            String property = "type=" + type.getName();
            ResourceLocation baseLocation = new ResourceLocation(Mekanism.MODID, "plastic_block");
            return new ModelResourceLocation(baseLocation, property);
        }
    }
}