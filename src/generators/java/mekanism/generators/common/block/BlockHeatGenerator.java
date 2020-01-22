package mekanism.generators.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.inventory.container.HeatGeneratorContainer;
import mekanism.generators.common.registries.GeneratorsSounds;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockHeatGenerator extends BlockMekanism implements IHasGui<TileEntityHeatGenerator>, IBlockElectric, IStateFacing, IHasInventory, IHasSecurity, IBlockSound,
      IHasTileEntity<TileEntityHeatGenerator>, ISupportsComparator, IStateWaterLogged, IStateActive, IHasDescription {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape generator = VoxelShapeUtils.combine(
              makeCuboidShape(0, 6.5, 6.5, 16, 15.5, 15.5),//drum
              makeCuboidShape(0, 0, 0, 16, 6, 16),//base
              makeCuboidShape(0, 6, 2, 16, 16, 6),//back
              makeCuboidShape(4, 6, 0, 12, 12, 2),//plate
              makeCuboidShape(3, 6, 1, 5, 15, 2),//bar1
              makeCuboidShape(11, 6, 1, 13, 15, 2),//bar2
              makeCuboidShape(3, 6, 6, 5, 16, 16),//ring1
              makeCuboidShape(11, 6, 6, 13, 16, 16),//ring2
              makeCuboidShape(0, 11, 0, 4, 12, 2),//fin1
              makeCuboidShape(0, 9, 0, 4, 10, 2),//fin2
              makeCuboidShape(0, 7, 0, 4, 8, 2),//fin3
              makeCuboidShape(12, 11, 0, 16, 12, 2),//fin4
              makeCuboidShape(12, 9, 0, 16, 10, 2),//fin5
              makeCuboidShape(12, 7, 0, 16, 8, 2),//fin6
              makeCuboidShape(0, 13, 0, 16, 14, 2),//fin7
              makeCuboidShape(0, 15, 0, 16, 16, 2)//fin8
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(generator, side);
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

    /**
     * @inheritDoc
     * @apiNote Only called on the client side
     */
    @Override
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
        return tile.openGui(player);
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
        return GeneratorsSounds.HEAT_GENERATOR.getSoundEvent();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityHeatGenerator tile) {
        return new ContainerProvider(TextComponentUtil.translate(getTranslationKey()), (i, inv, player) -> new HeatGeneratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityHeatGenerator> getTileType() {
        return GeneratorsTileEntityTypes.HEAT_GENERATOR.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return GeneratorsLang.DESCRIPTION_HEAT_GENERATOR;
    }
}