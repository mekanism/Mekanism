package mekanism.common.block.machine;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.MekanismLang;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.LaserTractorBeamContainer;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockLaserTractorBeam extends BlockMekanism implements IHasModel, IHasGui<TileEntityLaserTractorBeam>, IStateFacing, IStateActive, IHasInventory, IHasSecurity,
      IHasTileEntity<TileEntityLaserTractorBeam>, ISupportsComparator, IStateWaterLogged, IHasDescription {

    private static final VoxelShape bounds = VoxelShapeUtils.combine(
          makeCuboidShape(1, 1, 1, 15, 15, 15),//Base
          makeCuboidShape(0, 3, 3, 1, 13, 13),//S1
          makeCuboidShape(3, 3, 15, 13, 13, 16),//S2
          makeCuboidShape(15, 3, 3, 16, 13, 13),//S3
          makeCuboidShape(3, 0, 3, 13, 1, 13),//S4
          makeCuboidShape(3, 3, 0, 13, 13, 1),//S5
          makeCuboidShape(3, 15, 3, 13, 16, 13)//S6
    );

    public BlockLaserTractorBeam() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

    @Nonnull
    @Override
    public DirectionProperty getFacingProperty() {
        return BlockStateHelper.facingProperty;
    }

    /**
     * @inheritDoc
     * @apiNote Only called on the client side
     */
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile != null && MekanismUtils.isActive(world, pos) && ((IActiveState) tile).renderUpdate() && MekanismConfig.client.machineEffects.get()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            Direction side = tile.getDirection();

            switch (side) {
                case WEST:
                    world.addParticle(ParticleTypes.SMOKE, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, ILightReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).wasActiveRecently()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
    }

    @Nonnull
    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        return tile.openGui(player);
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        //TODO: This is how it was before, but should it be divided by 5 like in Block.java
        return blockResistance;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityLaserTractorBeam tile) {
        return new ContainerProvider(getNameTextComponent(), (i, inv, player) -> new LaserTractorBeamContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityLaserTractorBeam> getTileType() {
        return MekanismTileEntityTypes.LASER_TRACTOR_BEAM.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_LASER_TRACTOR_BEAM;
    }
}