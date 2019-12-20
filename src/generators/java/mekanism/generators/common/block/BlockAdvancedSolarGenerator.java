package mekanism.generators.common.block;

import javax.annotation.Nonnull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.generators.common.inventory.container.SolarGeneratorContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockAdvancedSolarGenerator extends BlockMekanism implements IHasGui<TileEntityAdvancedSolarGenerator>, IBlockElectric, IStateFacing, IHasInventory,
      IHasSecurity, IBlockSound, IHasTileEntity<TileEntityAdvancedSolarGenerator>, IStateWaterLogged {

    private static final SoundEvent SOUND_EVENT = new SoundEvent(Mekanism.rl("tile.gen.solar"));
    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape generator = VoxelShapeUtils.combine(
              makeCuboidShape(4, 4, 15, 12, 12, 16),//port
              makeCuboidShape(5, 5, 5, 11, 11, 15),//portBase
              makeCuboidShape(4, 38, 5, 12, 44, 11),//jointBox
              makeCuboidShape(6, 0, 6, 10, 40, 10),//verticalBar
              makeCuboidShape(-12, 40, 7, 28, 42, 9),//crossBar
              makeCuboidShape(5, 36, 2, 7, 38, 14),//sideBar1
              makeCuboidShape(9, 36, 2, 11, 38, 14),//sideBar2
              makeCuboidShape(5.5, 37.5, 4.5, 6.5, 44.5, 11.5),//wire1
              makeCuboidShape(9.5, 37.5, 4.5, 10.5, 44.5, 11.5),//wire2
              makeCuboidShape(-16, 42, -16, 2, 43, 32),//panel1Top
              makeCuboidShape(14, 42, -16, 32, 43, 32),//panel2Top
              makeCuboidShape(-15, 41, -14, 1, 42, 31),//panel1Bottom
              makeCuboidShape(15, 41, -14, 31, 42, 31),//panel2Bottom
              makeCuboidShape(0, 0, 0, 16, 2, 16),//base1
              makeCuboidShape(3, 1, 3, 13, 3, 13),//base2
              makeCuboidShape(4, 2, 4, 12, 10, 12)//base3
        );
        generator = VoxelShapeUtils.rotate(generator, Rotation.CLOCKWISE_180);
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(generator, side);
        }
    }

    public BlockAdvancedSolarGenerator() {
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

    @Override
    public TileEntityType<TileEntityAdvancedSolarGenerator> getTileType() {
        return GeneratorsTileEntityTypes.ADVANCED_SOLAR_GENERATOR.getTileEntityType();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityAdvancedSolarGenerator tile) {
        //TODO: Should this be advanced solar generator and stuff
        return new ContainerProvider("mekanism.container.solar_generator", (i, inv, player) -> new SolarGeneratorContainer(i, inv, tile));
    }
}