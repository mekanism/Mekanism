package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockStructuralGlass extends BlockTileDrops implements IHasModel, IHasTileEntity<TileEntityStructuralGlass> {

    public BlockStructuralGlass() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
        setRegistryName(new ResourceLocation(Mekanism.MODID, "structural_glass"));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
            if (tileEntity instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tileEntity).doUpdate();
            }
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

    @Override
    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public boolean shouldSideBeRendered(BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, Direction side) {
        //Not structural glass
        return world.getBlockState(pos.offset(side)).getBlock() != this;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityStructuralGlass();
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getLightOpacity(BlockState state, IBlockReader world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntityStructuralGlass tileEntity = (TileEntityStructuralGlass) MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity != null) {
            if (world.isRemote) {
                return true;
            }
            return tileEntity.onActivate(player, hand, player.getHeldItem(hand));
        }
        return false;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityStructuralGlass> getTileClass() {
        return TileEntityStructuralGlass.class;
    }
}