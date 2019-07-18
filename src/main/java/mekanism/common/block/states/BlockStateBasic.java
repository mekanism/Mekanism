package mekanism.common.block.states;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.BlockBasic;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateBasic extends ExtendedBlockState {

    public static final PropertyBool activeProperty = PropertyBool.create("active");

    public BlockStateBasic(BlockBasic block) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty, activeProperty}, new IUnlistedProperty[]{});
    }

    public enum BasicBlock {
        BASIC_BLOCK_1,
        BASIC_BLOCK_2;

        private PropertyEnum<BasicBlockType> predicatedProperty;

        public PropertyEnum<BasicBlockType> getProperty() {
            if (predicatedProperty == null) {
                predicatedProperty = PropertyEnum.create("type", BasicBlockType.class, it -> Objects.requireNonNull(it).blockType == this);
            }
            return predicatedProperty;
        }
    }

    public enum BasicBlockType {
        OSMIUM_BLOCK,
        BRONZE_BLOCK,
        REFINED_OBSIDIAN,
        COAL_BLOCK,
        REFINED_GLOWSTONE,
        STEEL_BLOCK,
        BIN,
        TELEPORTER_FRAME,
        STEEL_CASING,
        DYNAMIC_TANK,
        STRUCTURAL_GLASS,
        DYNAMIC_VALVE,
        COPPER_BLOCK,
        TIN_BLOCK,
        THERMAL_EVAPORATION_CONTROLLER,
        THERMAL_EVAPORATION_VALVE,
        THERMAL_EVAPORATION_BLOCK,
        INDUCTION_CASING,
        INDUCTION_PORT,
        INDUCTION_CELL,
        INDUCTION_PROVIDER,
        SUPERHEATING_ELEMENT,
        PRESSURE_DISPERSER,
        BOILER_CASING,
        BOILER_VALVE,
        SECURITY_DESK;

        @Nullable
        public static BasicBlockType get(IBlockState state) {
            if (state.getBlock() instanceof BlockBasic) {
                return state.getValue(((BlockBasic) state.getBlock()).getTypeProperty());
            }
            return null;
        }

        @Nullable
        public static BasicBlockType get(ItemStack stack) {
            return get(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
        }
    }

    public static class BasicBlockStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            BlockBasic block = (BlockBasic) state.getBlock();
            StringBuilder builder = new StringBuilder();

            if (block.hasActiveTexture()) {
                builder.append(activeProperty.getName());
                builder.append("=");
                builder.append(state.getValue(activeProperty));
            }

            if (block.hasRotations()) {
                EnumFacing facing = state.getValue(BlockStateFacing.facingProperty);

                if (!block.canRotateTo(facing)) {
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