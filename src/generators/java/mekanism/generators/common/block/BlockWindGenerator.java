package mekanism.generators.common.block;

import javax.annotation.Nonnull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.registries.GeneratorsSounds;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
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

public class BlockWindGenerator extends BlockMekanism implements IHasGui<TileEntityWindGenerator>, IBlockElectric, IStateFacing, IHasInventory, IHasSecurity, IBlockSound,
      IHasTileEntity<TileEntityWindGenerator>, IStateActive, IStateFluidLoggable, IHasDescription {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape generator = VoxelShapeUtils.combine(
              makeCuboidShape(4.5, 68.5, 4, 11.5, 75.5, 13),
              makeCuboidShape(5, 5, 1, 11, 11, 11),
              makeCuboidShape(6, 3, 2.5, 10, 5, 4.5),
              makeCuboidShape(4, 4, 0, 12, 12, 1),
              makeCuboidShape(2, 1, 2, 14, 3, 14),
              makeCuboidShape(0, 0, 0, 16, 2, 16),
              makeCuboidShape(5.5, 68.5, 13, 10.5, 74.82, 14.3),
              makeCuboidShape(5.5, 68.75, 14.3, 10.5, 74.1, 14.9),
              makeCuboidShape(6.5, 68.8, 14.9, 9.5, 73.8, 15.3),
              makeCuboidShape(6.5, 69, 15.3, 9.5, 72, 15.6),
              makeCuboidShape(6.5, 69, 15.6, 9.5, 70.3, 16),
              makeCuboidShape(5.25, 67, 5.25, 10.75, 70, 10.75),
              makeCuboidShape(5, 59, 5, 11, 67, 11),
              makeCuboidShape(4.75, 51, 4.75, 11.25, 59, 11.25),
              makeCuboidShape(4.5, 43, 4.5, 11.5, 51, 11.5),
              makeCuboidShape(4.25, 35, 4.25, 11.75, 43, 11.75),
              makeCuboidShape(4, 27, 4, 12, 35, 12),
              makeCuboidShape(3.75, 19, 3.75, 12.25, 27, 12.25),
              makeCuboidShape(3.5, 11, 3.5, 12.5, 19, 12.5),
              makeCuboidShape(3.25, 15, 3.25, 12.75, 19, 12.75),
              makeCuboidShape(3, 3, 3, 13, 15, 13)
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(generator, side);
        }
    }

    public BlockWindGenerator() {
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
        return 200_000;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return GeneratorsSounds.WIND_GENERATOR.getSoundEvent();
    }

    @Override
    public ContainerTypeRegistryObject<MekanismTileContainer<TileEntityWindGenerator>> getContainerType() {
        return GeneratorsContainerTypes.WIND_GENERATOR;
    }

    @Override
    public TileEntityType<TileEntityWindGenerator> getTileType() {
        return GeneratorsTileEntityTypes.WIND_GENERATOR.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return GeneratorsLang.DESCRIPTION_WIND_GENERATOR;
    }
}