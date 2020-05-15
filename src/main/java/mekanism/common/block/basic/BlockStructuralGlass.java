package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.Attributes.AttributeMultiblock;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockType.BlockTypeBuilder;
import mekanism.common.multiblock.IMultiblockBase.UpdateType;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.World;

public class BlockStructuralGlass extends BlockMekanism implements IHasTileEntity<TileEntityStructuralGlass>, IHasDescription, ITypeBlock {

    // TODO: clean this up at some point
    private static BlockType type;

    public BlockStructuralGlass() {
        super(Block.Properties.create(Material.GLASS).hardnessAndResistance(5F, 10F).notSolid());
    }

    @Override
    public BlockType getType() {
        if (type == null) {
            type = BlockTypeBuilder.createBlock(MekanismLang.EMPTY).with(new AttributeMultiblock()).build();
        }
        return type;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tile).onNeighborChange(neighborBlock);
            }
            if (tile instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tile).requestUpdate(neighborPos, UpdateType.NORMAL);
            }
        }
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, ILightReader world, BlockPos pos, IFluidState fluidState) {
        return true;
    }

    @Override
    @Deprecated
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        //Not structural glass
        return adjacentBlockState.getBlock() == this;
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntityStructuralGlass tile = MekanismUtils.getTileEntity(TileEntityStructuralGlass.class, world, pos);
        if (tile != null) {
            if (world.isRemote) {
                return ActionResultType.SUCCESS;
            }
            return tile.onActivate(player, hand, player.getHeldItem(hand));
        }
        return ActionResultType.PASS;
    }

    @Override
    public TileEntityType<TileEntityStructuralGlass> getTileType() {
        return MekanismTileEntityTypes.STRUCTURAL_GLASS.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_STRUCTURAL_GLASS;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Override
    @Deprecated
    public boolean causesSuffocation(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isNormalCube(BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }
}