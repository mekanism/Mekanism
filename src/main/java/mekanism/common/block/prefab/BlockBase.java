package mekanism.common.block.prefab;

import java.util.function.UnaryOperator;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeCustomPathType;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockBase<TYPE extends BlockType> extends BlockMekanism implements IHasDescription, ITypeBlock {

    protected final TYPE type;

    public BlockBase(TYPE type, UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
        this(type, propertyModifier.apply(BlockBehaviour.Properties.of().requiresCorrectToolForDrops()));
    }

    public BlockBase(TYPE type, BlockBehaviour.Properties properties) {
        super(hack(type, properties));
        this.type = type;
    }

    // ugly hack but required to have a reference to our block type before setting state info; assumes single-threaded startup
    private static BlockType cacheType;

    private static <TYPE extends BlockType> BlockBehaviour.Properties hack(TYPE type, BlockBehaviour.Properties props) {
        cacheType = type;
        for (Attribute a : type.getAll()) {
            a.adjustProperties(props);
        }
        return props;
    }

    @Override
    public final BlockType getType() {
        return type == null ? cacheType : type;
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return type.getDescription();
    }

    @NotNull
    @Override
    public MutableComponent getName() {
        if (this instanceof IColoredBlock coloredBlock) {
            return TextComponentUtil.build(coloredBlock.getColor(), super.getName());
        }
        BaseTier baseTier = Attribute.getBaseTier(builtInRegistryHolder());
        if (baseTier == null) {
            return super.getName();
        }
        return TextComponentUtil.build(baseTier.getColor(), super.getName());
    }

    @Override
    public float getExplosionResistance(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        AttributeCustomResistance customResistance = type.get(AttributeCustomResistance.class);
        return customResistance == null ? super.getExplosionResistance(state, world, pos, explosion) : customResistance.resistance();
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathType) {
        //If we have a custom shape which means we are not a full block then mark that movement is not
        // allowed through this block it is not a full block. Otherwise, use the normal handling for if movement is allowed
        return !type.has(AttributeCustomShape.class) && super.isPathfindable(state, pathType);
    }

    @Nullable
    @Override
    public PathType getBlockPathType(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @Nullable Mob mob) {
        AttributeCustomPathType customPathType = type.get(AttributeCustomPathType.class);
        if (customPathType != null) {
            PathType pathType = customPathType.pathCheck().getBlockPathType(state, level, pos, mob);
            if (pathType != null) {
                return pathType;
            }
        }
        return super.getBlockPathType(state, level, pos, mob);
    }

    @NotNull
    @Override
    protected VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        AttributeCustomShape customShape = type.get(AttributeCustomShape.class);
        if (customShape != null) {
            VoxelShape[] bounds = customShape.bounds();
            if (bounds.length == 1) {
                //If there is only one voxel shape for this model use it directly regardless of the direction it is facing
                return bounds[0];
            }
            AttributeStateFacing attr = type.get(AttributeStateFacing.class);
            int index = attr == null ? 0 : (attr.getDirection(state).ordinal() - (attr.getFacingProperty() == BlockStateProperties.FACING ? 0 : 2));
            return bounds[index];
        }
        return super.getShape(state, world, pos, context);
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isShiftKeyDown() && MekanismUtils.canUseAsWrench(stack)) {
            //Note: We don't handle checking if it is radioactive here, as the assumption is it doesn't have a tile so won't have that information
            if (!world.isClientSide) {
                WorldUtils.dismantleBlock(state, world, pos, player, stack);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static class BlockBaseModel<BLOCK extends BlockType> extends BlockBase<BLOCK> implements IStateFluidLoggable {

        public BlockBaseModel(BLOCK blockType, UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
            super(blockType, propertyModifier);
        }

        public BlockBaseModel(BLOCK blockType, BlockBehaviour.Properties properties) {
            super(blockType, properties);
        }
    }
}