package mekanism.generators.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.inventory.container.HeatGeneratorContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockHeatGenerator extends BlockMekanismContainer implements IHasGui<TileEntityHeatGenerator>, IBlockElectric, IStateFacing, IHasInventory, IHasSecurity, IBlockSound,
      IHasTileEntity<TileEntityHeatGenerator>, ISupportsComparator {

    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.gen.heat"));
    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape generator = MultipartUtils.combine(
              Block.makeCuboidShape(0.0, 6.5, 6.5, 16.0, 15.5, 15.5),//drum
              Block.makeCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),//base
              Block.makeCuboidShape(0.0, 6.0, 2.0, 16.0, 16.0, 6.0),//back
              Block.makeCuboidShape(4.0, 6.0, 0.0, 12.0, 12.0, 2.0),//plate
              Block.makeCuboidShape(3.0, 6.0, 1.0, 5.0, 15.0, 2.0),//bar1
              Block.makeCuboidShape(11.0, 6.0, 1.0, 13.0, 15.0, 2.0),//bar2
              Block.makeCuboidShape(3.0, 6.0, 6.0, 5.0, 16.0, 16.0),//ring1
              Block.makeCuboidShape(11.0, 6.0, 6.0, 13.0, 16.0, 16.0),//ring2
              Block.makeCuboidShape(0.0, 11.0, 0.0, 4.0, 12.0, 2.0),//fin1
              Block.makeCuboidShape(0.0, 9.0, 0.0, 4.0, 10.0, 2.0),//fin2
              Block.makeCuboidShape(0.0, 7.0, 0.0, 4.0, 8.0, 2.0),//fin3
              Block.makeCuboidShape(12.0, 11.0, 0.0, 16.0, 12.0, 2.0),//fin4
              Block.makeCuboidShape(12.0, 9.0, 0.0, 16.0, 10.0, 2.0),//fin5
              Block.makeCuboidShape(12.0, 7.0, 0.0, 16.0, 8.0, 2.0),//fin6
              Block.makeCuboidShape(0.0, 13.0, 0.0, 16.0, 14.0, 2.0),//fin7
              Block.makeCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 2.0)//fin8
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = MultipartUtils.rotateHorizontal(generator, side);
        }
    }

    public BlockHeatGenerator() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
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

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile != null && MekanismUtils.isActive(world, pos)) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            if (tile.getDirection() == Direction.WEST) {
                world.addParticle(ParticleTypes.SMOKE, xRandom + iRandom, yRandom, zRandom - jRandom, 0.0D, 0.0D, 0.0D);
                world.addParticle(ParticleTypes.FLAME, xRandom + iRandom, yRandom, zRandom - jRandom, 0.0D, 0.0D, 0.0D);
            } else if (tile.getDirection() == Direction.EAST) {
                world.addParticle(ParticleTypes.SMOKE, xRandom + iRandom, yRandom + 0.5F, zRandom - jRandom, 0.0D, 0.0D, 0.0D);
                world.addParticle(ParticleTypes.FLAME, xRandom + iRandom, yRandom + 0.5F, zRandom - jRandom, 0.0D, 0.0D, 0.0D);
            } else if (tile.getDirection() == Direction.NORTH) {
                world.addParticle(ParticleTypes.SMOKE, xRandom - jRandom, yRandom + 0.5F, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                world.addParticle(ParticleTypes.FLAME, xRandom - jRandom, yRandom + 0.5F, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
            } else if (tile.getDirection() == Direction.SOUTH) {
                world.addParticle(ParticleTypes.SMOKE, xRandom - jRandom, yRandom + 0.5F, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                world.addParticle(ParticleTypes.FLAME, xRandom - jRandom, yRandom + 0.5F, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
            }
        }
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

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
    }

    @Override
    public double getStorage() {
        return 160000;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityHeatGenerator tile) {
        return new ContainerProvider("mekanismgenerators.container.heat_generator", (i, inv, player) -> new HeatGeneratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityHeatGenerator> getTileType() {
        return GeneratorsTileEntityTypes.HEAT_GENERATOR.getTileEntityType();
    }
}