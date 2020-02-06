package mekanism.generators.common.block;

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
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.inventory.container.GasBurningGeneratorContainer;
import mekanism.generators.common.registries.GeneratorsSounds;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
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

//TODO: Figure out if there is another way to do this than having this be IStateActive?
public class BlockGasBurningGenerator extends BlockMekanism implements IHasGui<TileEntityGasGenerator>, IBlockElectric, IStateFacing, IHasInventory, IHasSecurity,
      IBlockSound, IHasTileEntity<TileEntityGasGenerator>, ISupportsComparator, IStateFluidLoggable, IHasDescription, IStateActive {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape generator = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16),//base
              makeCuboidShape(1.5, 4, 1.5, 14.5, 5, 14.5),//baseStand
              makeCuboidShape(3, 4, 3, 13, 16, 13),//center
              makeCuboidShape(12, 5, 12, 15, 14, 15),//pillar1
              makeCuboidShape(1, 5, 12, 4, 14, 15),//pillar2
              makeCuboidShape(12, 5, 1, 15, 14, 4),//pillar3
              makeCuboidShape(1, 5, 1, 4, 14, 4),//pillar4
              makeCuboidShape(4, 4, 15, 12, 12, 16),//port1
              makeCuboidShape(15, 4, 4, 16, 12, 12),//port2
              makeCuboidShape(4, 4, 0, 12, 12, 1),//port3
              makeCuboidShape(0, 4, 4, 1, 12, 12),//port4
              makeCuboidShape(4, 12.5, 12.5, 12, 13, 14.5),//connector1a
              makeCuboidShape(4, 12, 12, 12, 12.5, 12.5),//connector1b
              makeCuboidShape(13, 12.5, 4, 14.5, 13, 12),//connector2a
              makeCuboidShape(14.5, 12, 4, 15, 12.5, 12),//connector2b
              makeCuboidShape(1.5, 12.5, 4, 3, 13, 12),//connector3a
              makeCuboidShape(1, 12, 4, 1.5, 12.5, 12),//connector3b
              makeCuboidShape(4, 11.75, 2.75, 12, 12, 3),//connector4a
              makeCuboidShape(4, 11.25, 2.5, 12, 11.75, 2.75),//connector4b
              makeCuboidShape(4, 11, 2.25, 12, 11.25, 2.5)//connector4c
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(generator, side);
        }
    }

    public BlockGasBurningGenerator() {
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
        return 100 * MekanismConfig.general.FROM_H2.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return GeneratorsSounds.GAS_BURNING_GENERATOR.getSoundEvent();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityGasGenerator tile) {
        return new ContainerProvider(TextComponentUtil.translate(getTranslationKey()), (i, inv, player) -> new GasBurningGeneratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityGasGenerator> getTileType() {
        return GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return GeneratorsLang.DESCRIPTION_GAS_BURNING_GENERATOR;
    }
}