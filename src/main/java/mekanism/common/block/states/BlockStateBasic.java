package mekanism.common.block.states;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.BaseTier;
import mekanism.common.block.BlockBasic;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.TileEntityThermalEvaporationValve;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateBasic extends ExtendedBlockState {

    public static final PropertyBool activeProperty = PropertyBool.create("active");
    public static final PropertyEnum<BaseTier> tierProperty = PropertyEnum.create("tier", BaseTier.class);

    public BlockStateBasic(BlockBasic block, PropertyEnum<BasicBlockType> typeProperty) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty, typeProperty, activeProperty, tierProperty},
              new IUnlistedProperty[]{});
    }


    public enum BasicBlock {
        BASIC_BLOCK_1,
        BASIC_BLOCK_2;

        private PropertyEnum<BasicBlockType> predicatedProperty;

        public PropertyEnum<BasicBlockType> getProperty() {
            if (predicatedProperty == null) {
                predicatedProperty = PropertyEnum.create("type", BasicBlockType.class, new BasicBlockPredicate(this));
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
                    return null;
            }
        }
    }

    public enum BasicBlockType implements IStringSerializable {
        OSMIUM_BLOCK(BasicBlock.BASIC_BLOCK_1, 0, "OsmiumBlock", null, false, Predicates.alwaysFalse(), false, false,
              true),
        BRONZE_BLOCK(BasicBlock.BASIC_BLOCK_1, 1, "BronzeBlock", null, false, Predicates.alwaysFalse(), false, false,
              true),
        REFINED_OBSIDIAN(BasicBlock.BASIC_BLOCK_1, 2, "RefinedObsidian", null, false, Predicates.alwaysFalse(), false,
              false, true),
        COAL_BLOCK(BasicBlock.BASIC_BLOCK_1, 3, "CharcoalBlock", null, false, Predicates.alwaysFalse(), false, false,
              false),
        REFINED_GLOWSTONE(BasicBlock.BASIC_BLOCK_1, 4, "RefinedGlowstone", null, false, Predicates.alwaysFalse(), false,
              false, true),
        STEEL_BLOCK(BasicBlock.BASIC_BLOCK_1, 5, "SteelBlock", null, false, Predicates.alwaysFalse(), false, false,
              true),
        BIN(BasicBlock.BASIC_BLOCK_1, 6, "Bin", TileEntityBin.class, true, Plane.HORIZONTAL, true, true, false, true),
        TELEPORTER_FRAME(BasicBlock.BASIC_BLOCK_1, 7, "TeleporterFrame", null, true, Predicates.alwaysFalse(), false,
              false, false),
        STEEL_CASING(BasicBlock.BASIC_BLOCK_1, 8, "SteelCasing", null, true, Predicates.alwaysFalse(), false, false,
              false),
        DYNAMIC_TANK(BasicBlock.BASIC_BLOCK_1, 9, "DynamicTank", TileEntityDynamicTank.class, true,
              Predicates.alwaysFalse(), false, false, false),
        STRUCTURAL_GLASS(BasicBlock.BASIC_BLOCK_1, 10, "StructuralGlass", TileEntityStructuralGlass.class, true,
              Predicates.alwaysFalse(), false, false, false),
        DYNAMIC_VALVE(BasicBlock.BASIC_BLOCK_1, 11, "DynamicValve", TileEntityDynamicValve.class, true,
              Predicates.alwaysFalse(), false, false, false),
        COPPER_BLOCK(BasicBlock.BASIC_BLOCK_1, 12, "CopperBlock", null, false, Predicates.alwaysFalse(), false, false,
              true),
        TIN_BLOCK(BasicBlock.BASIC_BLOCK_1, 13, "TinBlock", null, false, Predicates.alwaysFalse(), false, false, true),
        THERMAL_EVAPORATION_CONTROLLER(BasicBlock.BASIC_BLOCK_1, 14, "ThermalEvaporationController",
              TileEntityThermalEvaporationController.class, true, Plane.HORIZONTAL, true, false, false),
        THERMAL_EVAPORATION_VALVE(BasicBlock.BASIC_BLOCK_1, 15, "ThermalEvaporationValve",
              TileEntityThermalEvaporationValve.class, true, Predicates.alwaysFalse(), false, false, false),
        THERMAL_EVAPORATION_BLOCK(BasicBlock.BASIC_BLOCK_2, 0, "ThermalEvaporationBlock",
              TileEntityThermalEvaporationBlock.class, true, Predicates.alwaysFalse(), false, false, false),
        INDUCTION_CASING(BasicBlock.BASIC_BLOCK_2, 1, "InductionCasing", TileEntityInductionCasing.class, true,
              Predicates.alwaysFalse(), false, false, false),
        INDUCTION_PORT(BasicBlock.BASIC_BLOCK_2, 2, "InductionPort", TileEntityInductionPort.class, true,
              Predicates.alwaysFalse(), true, false, false),
        INDUCTION_CELL(BasicBlock.BASIC_BLOCK_2, 3, "InductionCell", TileEntityInductionCell.class, true,
              Predicates.alwaysFalse(), false, true, false),
        INDUCTION_PROVIDER(BasicBlock.BASIC_BLOCK_2, 4, "InductionProvider", TileEntityInductionProvider.class, true,
              Predicates.alwaysFalse(), false, true, false),
        SUPERHEATING_ELEMENT(BasicBlock.BASIC_BLOCK_2, 5, "SuperheatingElement", TileEntitySuperheatingElement.class,
              true, Predicates.alwaysFalse(), false, false, false),
        PRESSURE_DISPERSER(BasicBlock.BASIC_BLOCK_2, 6, "PressureDisperser", TileEntityPressureDisperser.class, true,
              Predicates.alwaysFalse(), false, false, false),
        BOILER_CASING(BasicBlock.BASIC_BLOCK_2, 7, "BoilerCasing", TileEntityBoilerCasing.class, true,
              Predicates.alwaysFalse(), false, false, false),
        BOILER_VALVE(BasicBlock.BASIC_BLOCK_2, 8, "BoilerValve", TileEntityBoilerValve.class, true,
              Predicates.alwaysFalse(), false, false, false),
        SECURITY_DESK(BasicBlock.BASIC_BLOCK_2, 9, "SecurityDesk", TileEntitySecurityDesk.class, true, Plane.HORIZONTAL,
              false, false, false);

        public BasicBlock blockType;
        public int meta;
        public String name;
        public Class<? extends TileEntity> tileEntityClass;
        public boolean isElectric;
        public boolean hasDescription;
        public Predicate<EnumFacing> facingPredicate;
        public boolean activable;
        public boolean tiers;
        public boolean isBeaconBase;
        public boolean hasRedstoneOutput = false;

        BasicBlockType(BasicBlock block, int metadata, String s, Class<? extends TileEntity> tileClass, boolean hasDesc,
              Predicate<EnumFacing> facingAllowed, boolean activeState, boolean t, boolean beaconBase) {
            blockType = block;
            meta = metadata;
            name = s;
            tileEntityClass = tileClass;
            hasDescription = hasDesc;
            facingPredicate = facingAllowed;
            activable = activeState;
            tiers = t;
            isBeaconBase = beaconBase;
        }

        BasicBlockType(BasicBlock block, int metadata, String s, Class<? extends TileEntity> tileClass, boolean hasDesc,
              Predicate<EnumFacing> facingAllowed, boolean activeState, boolean t, boolean beaconBase, boolean hasRedstoneOutput) {
            blockType = block;
            meta = metadata;
            name = s;
            tileEntityClass = tileClass;
            hasDescription = hasDesc;
            facingPredicate = facingAllowed;
            activable = activeState;
            tiers = t;
            isBeaconBase = beaconBase;
            this.hasRedstoneOutput = hasRedstoneOutput;
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

        public TileEntity create() {
            try {
                return tileEntityClass.newInstance();
            } catch (Exception e) {
                Mekanism.logger.error("Unable to indirectly create tile entity.");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String getDescription() {
            return LangUtils.localize("tooltip." + name);
        }

        public ItemStack getStack(int amount) {
            return new ItemStack(blockType.getBlock(), amount, meta);
        }

        public boolean canRotateTo(EnumFacing side) {
            return facingPredicate.apply(side);
        }

        public boolean hasRotations() {
            return !facingPredicate.equals(Predicates.alwaysFalse());
        }

        public boolean hasActiveTexture() {
            return activable;
        }
    }

    public static class BasicBlockPredicate implements Predicate<BasicBlockType> {

        public BasicBlock basicBlock;

        public BasicBlockPredicate(BasicBlock type) {
            basicBlock = type;
        }

        @Override
        public boolean apply(BasicBlockType input) {
            return input.blockType == basicBlock;
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

            if (type.hasActiveTexture()) {
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

            if (type.tiers) {
                BaseTier tier = state.getValue(tierProperty);

                if (tier == BaseTier.CREATIVE && type != BasicBlockType.BIN) {
                    tier = BaseTier.ULTIMATE;
                }

                nameOverride = type.getName() + "_" + tier.getName();
            }

            if (builder.length() == 0) {
                builder.append("normal");
            }

            ResourceLocation baseLocation = new ResourceLocation("mekanism",
                  nameOverride != null ? nameOverride : type.getName());
            return new ModelResourceLocation(baseLocation, builder.toString());
        }
    }
}
