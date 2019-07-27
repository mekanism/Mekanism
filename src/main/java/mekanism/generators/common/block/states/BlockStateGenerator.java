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
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateGenerator extends ExtendedBlockState {

    public BlockStateGenerator(Block block) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty}, new IUnlistedProperty[]{});
    }

    public enum GeneratorType implements IBlockType {
        HEAT_GENERATOR(160000, true),
        SOLAR_GENERATOR(96000, true),
        GAS_GENERATOR(-1/*uses config, set after generators config loaded*/, true),
        BIO_GENERATOR(160000, true),
        ADVANCED_SOLAR_GENERATOR(200000, true),
        WIND_GENERATOR(200000, true),
        TURBINE_ROTOR,
        ROTATIONAL_COMPLEX,
        ELECTROMAGNETIC_COIL,
        TURBINE_CASING,
        TURBINE_VALVE,
        TURBINE_VENT,
        SATURATING_CONDENSER;

        public double maxEnergy;
        public boolean hasSecurity;
        private boolean notGenerator;

        GeneratorType() {
            this(-1, false);
            //This constructor is not actually used by "generators"
            notGenerator = true;
        }

        GeneratorType(double energy, boolean model) {
            maxEnergy = energy;
            hasSecurity = model;
        }

        public static List<GeneratorType> getGeneratorsForConfig() {
            return Arrays.stream(values()).filter(type -> !type.notGenerator).collect(Collectors.toList());
        }

        public static GeneratorType get(Block block, int meta) {
            return null;
        }

        public static GeneratorType get(ItemStack stack) {
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
            ResourceLocation baseLocation = block.getRegistryName();
            return new ModelResourceLocation(baseLocation, builder.toString());
        }
    }
}