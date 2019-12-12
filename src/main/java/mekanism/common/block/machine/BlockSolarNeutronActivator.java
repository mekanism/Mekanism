package mekanism.common.block.machine;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Upgrade;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.block.ISupportsRedstone;
import mekanism.api.block.ISupportsUpgrades;
import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.SolarNeutronActivatorContainer;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
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

public class BlockSolarNeutronActivator extends BlockMekanism implements IHasModel, IHasGui<TileEntitySolarNeutronActivator>, ISupportsUpgrades, IStateFacing,
      IStateActive, IHasInventory, IHasSecurity, ISupportsRedstone, IHasTileEntity<TileEntitySolarNeutronActivator>, ISupportsComparator, IStateWaterLogged {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape activator = VoxelShapeUtils.combine(
              makeCuboidShape(6, 14, 14, 10, 29, 16),
              makeCuboidShape(4, 4, 0, 12, 12, 1),
              makeCuboidShape(2, 4, 2, 14, 5, 15),
              makeCuboidShape(5, 14, 12, 6, 15, 13),
              makeCuboidShape(5, 15, 10, 11, 16, 11),
              makeCuboidShape(5, 14, 10, 6, 15, 11),
              makeCuboidShape(5, 15, 12, 11, 16, 13),
              makeCuboidShape(10, 14, 12, 11, 15, 13),
              makeCuboidShape(10, 14, 10, 11, 15, 11),
              makeCuboidShape(7, 13.5, 7, 9, 14.5, 14),
              makeCuboidShape(0, 5, 0, 16, 14, 16),
              makeCuboidShape(0, 0, 0, 16, 4, 16),
              makeCuboidShape(0.5, 4, 0.5, 1.5, 5, 1.5),
              makeCuboidShape(0.5, 4, 14.5, 1.5, 5, 15.5),
              makeCuboidShape(0.5, 4, 2.5, 1.5, 5, 3.5),
              makeCuboidShape(0.5, 4, 4.5, 1.5, 5, 5.5),
              makeCuboidShape(0.5, 4, 6.5, 1.5, 5, 7.5),
              makeCuboidShape(0.5, 4, 8.5, 1.5, 5, 9.5),
              makeCuboidShape(0.5, 4, 10.5, 1.5, 5, 11.5),
              makeCuboidShape(0.5, 4, 12.5, 1.5, 5, 13.5),
              makeCuboidShape(14.5, 4, 14.5, 15.5, 5, 15.5),
              makeCuboidShape(14.5, 4, 12.5, 15.5, 5, 13.5),
              makeCuboidShape(14.5, 4, 10.5, 15.5, 5, 11.5),
              makeCuboidShape(14.5, 4, 8.5, 15.5, 5, 9.5),
              makeCuboidShape(14.5, 4, 6.5, 15.5, 5, 7.5),
              makeCuboidShape(14.5, 4, 4.5, 15.5, 5, 5.5),
              makeCuboidShape(14.5, 4, 2.5, 15.5, 5, 3.5),
              makeCuboidShape(14.5, 4, 0.5, 15.5, 5, 1.5),
              makeCuboidShape(5, 4, 1, 11, 5, 2),
              //Rough estimates of slanted things
              makeCuboidShape(6, 14, 1, 7, 14.75, 3),
              makeCuboidShape(9, 14, 1, 10, 14.75, 3),
              makeCuboidShape(5, 14, 3, 11, 15.25, 4.5),
              makeCuboidShape(5, 14, 4.5, 11, 15, 6),
              makeCuboidShape(5, 14, 6, 11, 14.875, 7.5),
              makeCuboidShape(5, 14, 7.5, 11, 14.75, 9),
              makeCuboidShape(6.5, 14, 4.5, 9.5, 16, 5.5),
              makeCuboidShape(6.5, 14, 5.5, 7.5, 16, 6.5),
              makeCuboidShape(8.5, 14, 5.5, 9.5, 16, 6.5),
              makeCuboidShape(6.5, 14, 6.5, 9.5, 16, 7.5),
              //Top center
              makeCuboidShape(7, 26, 10, 9, 26.5, 14),
              makeCuboidShape(7, 26.5, 5.75, 9, 29, 14),
              makeCuboidShape(7.5, 25.75, 6.625, 8.5, 26.5, 7.625),
              makeCuboidShape(5, 29.5, 0, 11, 30.5, 1),
              makeCuboidShape(5, 28.5, 1, 11, 30.5, 4.5),
              makeCuboidShape(5, 28, 4.5, 11, 30, 8),
              makeCuboidShape(5, 27.75, 8, 11, 29.5, 11.5),
              makeCuboidShape(5, 27.25, 11.5, 11, 29, 15),
              //Left Side panel
              makeCuboidShape(11, 30, 0, 12.25, 31, 4),
              makeCuboidShape(11, 29.5, 4, 12.25, 30.5, 8),
              makeCuboidShape(11, 29, 8, 12.25, 30, 12),
              makeCuboidShape(11, 28.5, 12, 12.25, 29.5, 16.1),
              makeCuboidShape(12.25, 30.5, 0, 14.75, 31.5, 4),
              makeCuboidShape(12.25, 30, 4, 14.75, 31, 8),
              makeCuboidShape(12.25, 29.5, 8, 14.75, 30.5, 12),
              makeCuboidShape(12.25, 29, 12, 14.75, 30, 16.1),
              makeCuboidShape(14.75, 31, 0.25, 16.5, 32.25, 4),
              makeCuboidShape(14.75, 30.5, 4, 16.5, 31.5, 8),
              makeCuboidShape(14.75, 30, 8, 16.5, 31, 12),
              makeCuboidShape(14.75, 29.5, 12, 16.5, 30.5, 16.1),
              //Right Side panel
              makeCuboidShape(3.75, 30, 0, 5, 31, 4),
              makeCuboidShape(3.75, 29.5, 4, 5, 30.5, 8),
              makeCuboidShape(3.75, 29, 8, 5, 30, 12),
              makeCuboidShape(3.75, 28.5, 12, 5, 29.5, 16.1),
              makeCuboidShape(1.25, 30.5, 0, 3.75, 31.5, 4),
              makeCuboidShape(1.25, 30, 4, 3.75, 31, 8),
              makeCuboidShape(1.25, 29.5, 8, 3.75, 30.5, 12),
              makeCuboidShape(1.25, 29, 12, 3.75, 30, 16.1),
              makeCuboidShape(-0.5, 31, 0.25, 1.25, 32.25, 4),
              makeCuboidShape(-0.5, 30.5, 4, 1.25, 31.5, 8),
              makeCuboidShape(-0.5, 30, 8, 1.25, 31, 12),
              makeCuboidShape(-0.5, 29.5, 12, 1.25, 30.5, 16.1)
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(activator, side);
        }
    }

    public BlockSolarNeutronActivator() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

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

    /**
     * @inheritDoc
     * @apiNote Only called on the client side
     */
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).wasActiveRecently()) {
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
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return false;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
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
        return bounds[getDirection(state).ordinal() - 2];
    }

    @Override
    public INamedContainerProvider getProvider(TileEntitySolarNeutronActivator tile) {
        return new ContainerProvider("mekanism.container.solar_neutron_activator", (i, inv, player) -> new SolarNeutronActivatorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntitySolarNeutronActivator> getTileType() {
        return MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR.getTileEntityType();
    }

    @Nonnull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED);
    }
}