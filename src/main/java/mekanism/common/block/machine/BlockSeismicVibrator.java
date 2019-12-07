package mekanism.common.block.machine;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsRedstone;
import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.SeismicVibratorContainer;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.base.MekanismTileEntityTypes;
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
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockSeismicVibrator extends BlockMekanism implements IBlockElectric, IHasModel, IHasGui<TileEntitySeismicVibrator>, IStateFacing, IStateActive,
      IHasInventory, IHasSecurity, ISupportsRedstone, IHasTileEntity<TileEntitySeismicVibrator> {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape vibrator = VoxelShapeUtils.combine(
              makeCuboidShape(0, 30, 0, 16, 32, 16),//top
              makeCuboidShape(5, 25, 1, 11, 29, 11),//motor
              makeCuboidShape(4, 4, 0, 12, 12, 1),//port
              makeCuboidShape(6, 5, 0, 10, 30, 2),//conduit
              makeCuboidShape(0, 0, 13, 16, 5, 16),//baseFront
              makeCuboidShape(0, 0, 3, 3, 5, 13),//baseRight
              makeCuboidShape(13, 0, 3, 16, 5, 13),//baseLeft
              makeCuboidShape(0, 0, 0, 16, 5, 3),//baseBack
              makeCuboidShape(7, 3, 7, 9, 18, 9),//shaft1
              makeCuboidShape(6.5, 18, 6.5, 9.5, 29, 9.5),//shaft2
              makeCuboidShape(3, 29, 1, 13, 30, 13),//plate1
              makeCuboidShape(6, 1, 6, 10, 3, 10),//plate2
              makeCuboidShape(4, 0, 4, 12, 2, 12),//plate3
              makeCuboidShape(6.5, 15, 2, 9.5, 17, 6),//arm1
              makeCuboidShape(6, 15, 6, 10, 17, 10),//arm2
              makeCuboidShape(7, 15, 1, 9, 17, 5),//arm3
              makeCuboidShape(14.5, 5, 14.5, 15.5, 30, 15.5),//pole1
              makeCuboidShape(14.5, 5, 0.5, 15.5, 30, 1.5),//pole2
              makeCuboidShape(0.5, 5, 14.5, 1.5, 30, 15.5),//pole3
              makeCuboidShape(0, 5, 0.5, 1.5, 30, 1.5),//pole4
              makeCuboidShape(0.51, 11, 14.5, 1.51, 30, 15.5),//frameRight1
              makeCuboidShape(0.51, 11, -0.5, 1.51, 30, 1.5),//frameRight2
              makeCuboidShape(0.5, 17, 1.5, 1.5, 18, 14.5),//frameRight3
              makeCuboidShape(0.51, -2, -0.5, 1.51, 17, 1.5),//frameRight4
              makeCuboidShape(0.51, -2, 14.5, 1.51, 17, 15.5),//frameRight5
              makeCuboidShape(14.49, 11, -0.5, 15.49, 30, 1.5),//frameLeft1
              makeCuboidShape(14.49, 11, 14.5, 15.49, 30, 15.5),//frameLeft2
              makeCuboidShape(14.5, 17, 1.5, 15.5, 18, 14.5),//frameLeft3
              makeCuboidShape(14.49, -2, -0.5, 15.49, 17, 1.5),//frameLeft4
              makeCuboidShape(14.49, -2, 14.5, 15.49, 17, 15.5),//frameLeft5
              makeCuboidShape(-0.5, 11, 0.51, 1.5, 30, 1.51),//frameBack1
              makeCuboidShape(14.5, 11, 0.51, 15.5, 30, 1.51),//frameBack2
              makeCuboidShape(1.5, 17, 0.5, 14.5, 18, 1.5),//frameBack3
              makeCuboidShape(14.5, -2, 0.51, 15.5, 17, 1.51),//frameBack4
              makeCuboidShape(-0.5, -2, 0.51, 1.5, 17, 1.51),//frameBack5
              makeCuboidShape(11.5, 28.5, 11.5, 12.5, 29.5, 12.5),//rivet1
              makeCuboidShape(11.5, 28.5, 9.5, 12.5, 29.5, 10.5),//rivet2
              makeCuboidShape(11.5, 28.5, 7.5, 12.5, 29.5, 8.5),//rivet3
              makeCuboidShape(11.5, 28.5, 5.5, 12.5, 29.5, 6.5),//rivet4
              makeCuboidShape(11.5, 28.5, 3.5, 12.5, 29.5, 4.5),//rivet5
              makeCuboidShape(3.5, 28.5, 11.5, 4.5, 29.5, 12.5),//rivet6
              makeCuboidShape(3.5, 28.5, 9.5, 4.5, 29.5, 10.5),//rivet7
              makeCuboidShape(3.5, 28.5, 7.5, 4.5, 29.5, 8.5),//rivet8
              makeCuboidShape(3.5, 28.5, 5.5, 4.5, 29.5, 6.5),//rivet9
              makeCuboidShape(3.5, 28.5, 3.5, 4.5, 29.5, 4.5)//rivet10
        );
        vibrator = VoxelShapeUtils.getSeismicSlope();
        //TODO: VoxelShapes: Fix the angled bars, and also fix snow not being able to be placed on top.
        // That part is due to not returning a full VoxelShape from getShape, can we somehow get the default a different way?
        //TODO: Do we want to have 8 instead of 4, so that when it is active the center bar is different? (Probably not worth the effort)
        /*setRotation(arm3, -0.3665191F, 0F, 0F);
        setRotation(frameRight1, 0.837758F, 0F, 0F);
        setRotation(frameRight2, -0.837758F, 0F, 0F);
        setRotation(frameRight4, -0.837758F, 0F, 0F);
        setRotation(frameRight5, 0.837758F, 0F, 0F);
        setRotation(frameLeft1, -0.837758F, 0F, 0F);
        setRotation(frameLeft2, 0.837758F, 0F, 0F);
        setRotation(frameLeft4, -0.837758F, 0F, 0F);
        setRotation(frameLeft5, 0.837758F, 0F, 0F);
        setRotation(frameBack1, 0F, 0F, 0.837758F);
        setRotation(frameBack2, 0F, 0F, -0.837758F);
        setRotation(frameBack4, 0F, 0F, -0.837758F);
        setRotation(frameBack5, 0F, 0F, 0.837758F);*/
        //vibrator = VoxelShapeUtils.rotate(vibrator, Rotation.CLOCKWISE_180);
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(vibrator, side);
        }
    }

    public BlockSeismicVibrator() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tileEntity = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tileEntity != null && MekanismUtils.isActive(world, pos) && ((IActiveState) tileEntity).renderUpdate() && MekanismConfig.client.machineEffects.get()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            Direction side = tileEntity.getDirection();

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
    public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos);
            if (tileEntity instanceof IActiveState && ((IActiveState) tileEntity).lightUpdate() && ((IActiveState) tileEntity).wasActiveRecently()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tileEntity == null) {
            return false;
        }
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        return tileEntity.openGui(player);
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
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
        return bounds[getDirection(state).ordinal() - 2];
    }

    @Override
    public double getUsage() {
        return MekanismConfig.usage.seismicVibrator.get();
    }

    @Override
    public double getConfigStorage() {
        return MekanismConfig.storage.seismicVibrator.get();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntitySeismicVibrator tile) {
        return new ContainerProvider("mekanism.container.seismic_vibrator", (i, inv, player) -> new SeismicVibratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntitySeismicVibrator> getTileType() {
        return MekanismTileEntityTypes.SEISMIC_VIBRATOR.getTileEntityType();
    }
}