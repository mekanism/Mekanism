package mekanism.generators.common.block.states;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.common.base.IBlockType;
import mekanism.common.block.interfaces.IRotatableBlock;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.config.MekanismConfig;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateGenerator extends ExtendedBlockState {

    public BlockStateGenerator(Block block) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty}, new IUnlistedProperty[]{});
    }

    public enum GeneratorType implements IBlockType {
        HEAT_GENERATOR(false),
        SOLAR_GENERATOR(false),
        GAS_GENERATOR(false),
        BIO_GENERATOR(false),
        ADVANCED_SOLAR_GENERATOR(false),
        WIND_GENERATOR(false),

        TURBINE_ROTOR(true),
        ROTATIONAL_COMPLEX(true),
        ELECTROMAGNETIC_COIL(true),
        TURBINE_CASING(true),
        TURBINE_VALVE(true),
        TURBINE_VENT(true),
        SATURATING_CONDENSER(true);

        private boolean notGenerator;

        GeneratorType(boolean notGenerator) {
            this.notGenerator = notGenerator;
        }

        public static List<GeneratorType> getGeneratorsForConfig() {
            return Arrays.stream(values()).filter(type -> !type.notGenerator).collect(Collectors.toList());
        }

        public static GeneratorType get(Block block, int meta) {
            return null;
        }

        @Override
        public String getBlockName() {
            return "";
        }

        @Override
        public boolean isEnabled() {
            return notGenerator || MekanismConfig.current().generators.generatorsManager.isEnabled(this);
        }
    }

    public static class GeneratorBlockStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            Block block = state.getBlock();
            StringBuilder builder = new StringBuilder();

            if (block instanceof IRotatableBlock) {
                EnumFacing facing = state.getValue(BlockStateFacing.facingProperty);
                if (!((IRotatableBlock) block).canRotateTo(facing)) {
                    facing = EnumFacing.NORTH;
                }
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(BlockStateFacing.facingProperty.getName());
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