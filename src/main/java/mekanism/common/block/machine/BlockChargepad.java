package mekanism.common.block.machine;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.TileEntityChargepad;
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
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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

//TODO: Evaluate IStateActive here, is used for animateTick. There might be a better way to do this without requiring it to have a state
public class BlockChargepad extends BlockMekanism implements IBlockElectric, IHasModel, IStateFacing, IHasTileEntity<TileEntityChargepad>, IBlockSound, IStateActive {

    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.machine.chargepad"));
    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape chargepad = VoxelShapeUtils.combine(
              makeCuboidShape(10, 1, 11.99, 12, 8, 13.99),//pillar1
              makeCuboidShape(4, 1, 11.99, 6, 8, 13.99),//pillar2
              makeCuboidShape(0, 0, 0, 16, 1, 16),//base
              makeCuboidShape(5, 5, 14, 11, 11, 15),//connector
              makeCuboidShape(5, 1, 13, 11, 11, 14),//stand
              makeCuboidShape(7, 4, 11, 9, 5, 13),//plug
              makeCuboidShape(4, 4, 15, 12, 12, 16),//port
              makeCuboidShape(5, 5, 15, 11, 11, 16)//port_ring
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(chargepad, side);
        }
    }

    public BlockChargepad() {
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
        return tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS;
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
        return 25;
    }

    @Override
    public double getConfigStorage() {
        return MekanismConfig.storage.chargePad.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public TileEntityType<TileEntityChargepad> getTileType() {
        return MekanismTileEntityTypes.CHARGEPAD.getTileEntityType();
    }
}