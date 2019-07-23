package mekanism.generators.common.block.states;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.common.base.IBlockType;
import mekanism.common.block.interfaces.IRotatableBlock;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateUtils;
import mekanism.common.config.MekanismConfig;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateGenerator extends ExtendedBlockState {

    public BlockStateGenerator(Block block) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty}, new IUnlistedProperty[]{});
    }

    public enum GeneratorType implements IBlockType {
        HEAT_GENERATOR(160000, true, Plane.HORIZONTAL),
        SOLAR_GENERATOR(96000, true, Plane.HORIZONTAL),
        GAS_GENERATOR(-1/*uses config, set after generators config loaded*/, true, Plane.HORIZONTAL),
        BIO_GENERATOR(160000, true, Plane.HORIZONTAL),
        ADVANCED_SOLAR_GENERATOR(200000, true, Plane.HORIZONTAL),
        WIND_GENERATOR(200000, true, Plane.HORIZONTAL),
        TURBINE_ROTOR(-1, false, BlockStateUtils.NO_ROTATION),
        ROTATIONAL_COMPLEX(-1, false, BlockStateUtils.NO_ROTATION),
        ELECTROMAGNETIC_COIL(-1, false, BlockStateUtils.NO_ROTATION),
        TURBINE_CASING(-1, false, BlockStateUtils.NO_ROTATION),
        TURBINE_VALVE(-1, false, BlockStateUtils.NO_ROTATION),
        TURBINE_VENT(-1, false, BlockStateUtils.NO_ROTATION),
        SATURATING_CONDENSER(-1, false, BlockStateUtils.NO_ROTATION);

        private static final List<GeneratorType> GENERATORS_FOR_CONFIG;

        static {
            GENERATORS_FOR_CONFIG = new ArrayList<>();

            for (GeneratorType type : GeneratorType.values()) {
                if (type.ordinal() <= 5) {
                    GENERATORS_FOR_CONFIG.add(type);
                }
            }
        }

        public int meta;
        public String blockName;
        public double maxEnergy;
        public boolean hasModel;
        public Predicate<EnumFacing> facingPredicate;

        GeneratorType(double energy, boolean model, Predicate<EnumFacing> predicate) {
            maxEnergy = energy;
            hasModel = model;
            facingPredicate = predicate;
        }

        public static List<GeneratorType> getGeneratorsForConfig() {
            return GENERATORS_FOR_CONFIG;
        }

        public static GeneratorType get(Block block, int meta) {
            return null;
        }

        public static GeneratorType get(ItemStack stack) {
            return null;
        }

        @Override
        public String getBlockName() {
            return blockName;
        }

        @Override
        public boolean isEnabled() {
            if (meta > WIND_GENERATOR.meta) {
                return true;
            }
            return MekanismConfig.current().generators.generatorsManager.isEnabled(this);
        }

        public boolean canRotateTo(EnumFacing side) {
            return facingPredicate.test(side);
        }

        public boolean hasRotations() {
            return !facingPredicate.equals(BlockStateUtils.NO_ROTATION);
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