package mekanism.common.block.machine;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismLang;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

//TODO: Evaluate IStateActive here, is used for animateTick. There might be a better way to do this without requiring it to have a state
public class BlockLaser extends BlockMekanism implements IBlockElectric, IHasModel, IStateFacing, IHasTileEntity<TileEntityLaser>, IBlockSound, IStateActive,
      IStateFluidLoggable, IHasDescription {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.DIRECTIONS.length];

    static {
        VoxelShape laser = VoxelShapeUtils.combine(
              makeCuboidShape(5, 2, 4, 11, 9, 12),//body
              makeCuboidShape(8.5, 8, 7.5, 9.49, 13.99, 8.5),//wire
              makeCuboidShape(6.5, 9, 6.5, 9.5, 11, 9.5),//shaft
              makeCuboidShape(7, 1, 7, 9, 16, 9),//center
              makeCuboidShape(4, 0, 4, 12, 1, 12),//port
              makeCuboidShape(5, 1, 5, 11, 2, 11),//connector
              makeCuboidShape(6, 13, 6, 10, 14, 10),//ring1
              makeCuboidShape(6, 11, 6, 10, 12, 10),//ring2
              makeCuboidShape(4.2, 3, 9, 5.2, 8, 10),//rod1
              makeCuboidShape(4.2, 3, 6, 5.2, 8, 7),//rod2
              makeCuboidShape(10.8, 3, 9, 11.8, 8, 10),//rod3
              makeCuboidShape(10.8, 3, 6, 11.8, 8, 7),//rod4
              makeCuboidShape(4, 2, 4, 5, 3, 12),//fin1
              makeCuboidShape(4, 4, 4, 5, 5, 12),//fin2
              makeCuboidShape(4, 6, 4, 5, 7, 12),//fin3
              makeCuboidShape(4, 8, 4, 5, 9, 12),//fin4
              makeCuboidShape(11, 8, 4, 12, 9, 12),//fin5
              makeCuboidShape(11, 6, 4, 12, 7, 12),//fin6
              makeCuboidShape(11, 4, 4, 12, 5, 12),//fin7
              makeCuboidShape(11, 2, 4, 12, 3, 12)//fin8
        );
        for (Direction side : EnumUtils.DIRECTIONS) {
            bounds[side.ordinal()] = VoxelShapeUtils.rotate(laser, side.getOpposite());
        }
    }

    public BlockLaser() {
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
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
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
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
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
        return ActionResultType.PASS;
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
        return bounds[getDirection(state).ordinal()];
    }

    @Override
    public double getUsage() {
        return MekanismConfig.usage.laser.get();
    }

    @Override
    public double getStorage() {
        return MekanismConfig.storage.laser.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return MekanismSounds.LASER.getSoundEvent();
    }

    @Override
    public TileEntityType<TileEntityLaser> getTileType() {
        return MekanismTileEntityTypes.LASER.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_LASER;
    }
}