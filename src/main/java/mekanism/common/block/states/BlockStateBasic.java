package mekanism.common.block.states;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockBasic;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateBasic extends ExtendedBlockState {

    public static final PropertyBool activeProperty = PropertyBool.create("active");

    public BlockStateBasic(BlockBasic block, PropertyEnum<BasicBlockType> typeProperty) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty, typeProperty, activeProperty}, new IUnlistedProperty[]{});
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

        public Block getBlock() {
            switch (this) {
                case BASIC_BLOCK_1:
                    return MekanismBlocks.BasicBlock;
                case BASIC_BLOCK_2:
                    return MekanismBlocks.BasicBlock2;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public enum BasicBlockType implements IStringSerializable {
        OSMIUM_BLOCK(BlockStateUtils.NO_ROTATION),
        BRONZE_BLOCK(BlockStateUtils.NO_ROTATION),
        REFINED_OBSIDIAN(BlockStateUtils.NO_ROTATION),
        COAL_BLOCK(BlockStateUtils.NO_ROTATION),
        REFINED_GLOWSTONE(BlockStateUtils.NO_ROTATION),
        STEEL_BLOCK(BlockStateUtils.NO_ROTATION),

        BIN(Plane.HORIZONTAL, false),

        TELEPORTER_FRAME(BlockStateUtils.NO_ROTATION),
        STEEL_CASING(BlockStateUtils.NO_ROTATION),

        DYNAMIC_TANK(BlockStateUtils.NO_ROTATION, false),
        STRUCTURAL_GLASS(BlockStateUtils.NO_ROTATION, false, false),

        DYNAMIC_VALVE(BlockStateUtils.NO_ROTATION),
        COPPER_BLOCK(BlockStateUtils.NO_ROTATION),
        TIN_BLOCK(BlockStateUtils.NO_ROTATION),

        THERMAL_EVAPORATION_CONTROLLER(Plane.HORIZONTAL, false),

        THERMAL_EVAPORATION_VALVE(BlockStateUtils.NO_ROTATION),
        THERMAL_EVAPORATION_BLOCK(BlockStateUtils.NO_ROTATION),
        INDUCTION_CASING(BlockStateUtils.NO_ROTATION),
        INDUCTION_PORT(BlockStateUtils.NO_ROTATION),
        INDUCTION_CELL(BlockStateUtils.NO_ROTATION),
        INDUCTION_PROVIDER(BlockStateUtils.NO_ROTATION),
        SUPERHEATING_ELEMENT(BlockStateUtils.NO_ROTATION),
        PRESSURE_DISPERSER(BlockStateUtils.NO_ROTATION),

        BOILER_CASING(BlockStateUtils.NO_ROTATION, false),

        BOILER_VALVE(BlockStateUtils.NO_ROTATION),

        SECURITY_DESK(Plane.HORIZONTAL, false, false);

        public Predicate<EnumFacing> facingPredicate;
        public boolean isFullBlock;
        public boolean isOpaqueCube;

        BasicBlockType(Predicate<EnumFacing> facingAllowed) {
            this(facingAllowed, true, true);
        }

        BasicBlockType(Predicate<EnumFacing> facingAllowed, boolean opaque) {
            this(facingAllowed, true, opaque);
        }

        BasicBlockType(Predicate<EnumFacing> facingAllowed, boolean fullBlock, boolean opaque) {
            facingPredicate = facingAllowed;
            isFullBlock = fullBlock;
            isOpaqueCube = opaque;
        }

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

        @Nullable
        public static BasicBlockType get(Block block, int meta) {
            if (block instanceof BlockBasic) {
                return get(((BlockBasic) block).getBasicBlock(), meta);
            }
            return null;
        }

        @Nullable
        public static BasicBlockType get(BasicBlock blockType, int metadata) {
            int index = blockType.ordinal() << 4 | metadata;
            if (index < values().length) {
                BasicBlockType firstTry = values()[index];
                if (firstTry.blockType == blockType && firstTry.meta == metadata) {
                    return firstTry;
                }
            }
            for (BasicBlockType type : values()) {
                if (type.blockType == blockType && type.meta == metadata) {
                    return type;
                }
            }
            Mekanism.logger.error("Invalid BasicBlock. type: {}, meta: {}", blockType.ordinal(), metadata);
            return null;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String getDescription() {
            return LangUtils.localize("tooltip." + name);
        }

        public boolean canRotateTo(EnumFacing side) {
            return facingPredicate.test(side);
        }

        public boolean hasRotations() {
            return !facingPredicate.equals(BlockStateUtils.NO_ROTATION);
        }
    }

    public static class BasicBlockStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            BlockBasic block = (BlockBasic) state.getBlock();
            BasicBlockType type = state.getValue(block.getTypeProperty());
            StringBuilder builder = new StringBuilder();
            String nameOverride = null;

            if (block.hasActiveTexture()) {
                builder.append(activeProperty.getName());
                builder.append("=");
                builder.append(state.getValue(activeProperty));
            }

            if (type.hasRotations()) {
                EnumFacing facing = state.getValue(BlockStateFacing.facingProperty);

                if (!type.canRotateTo(facing)) {
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
            ResourceLocation baseLocation = new ResourceLocation(Mekanism.MODID, nameOverride != null ? nameOverride : type.getName());
            return new ModelResourceLocation(baseLocation, builder.toString());
        }
    }
}