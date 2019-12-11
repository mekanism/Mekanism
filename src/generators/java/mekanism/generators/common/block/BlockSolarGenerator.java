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
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.generators.common.inventory.container.SolarGeneratorContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
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

public class BlockSolarGenerator extends BlockMekanism implements IHasGui<TileEntitySolarGenerator>, IBlockElectric, IHasInventory, IHasSecurity, IBlockSound,
      IHasTileEntity<TileEntitySolarGenerator> {

    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.gen.solar"));

    private static final VoxelShape bounds = VoxelShapeUtils.combine(
          makeCuboidShape(0, 9, 0, 16, 11, 16),//solarPanel
          makeCuboidShape(1, 8, 1, 15, 9, 15),//solarPanelBottom
          makeCuboidShape(4, 0, 4, 12, 1, 12),//solarPanelPort
          makeCuboidShape(6, 7, 6, 10, 9, 10),//solarPanelConnector
          makeCuboidShape(6, 1, 6, 10, 2, 10),//solarPanelPipeBase
          makeCuboidShape(6.5, 3, 6.5, 9.5, 6, 9.5),//solarPanelPipeConnector
          makeCuboidShape(7, 5, 7, 9, 8, 9),//solarPanelRod1
          makeCuboidShape(7, 3, 7, 9, 5, 9)//solarPanelRod2
    );

    public BlockSolarGenerator() {
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
        return bounds;
    }

    @Override
    public double getStorage() {
        return 96000;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntitySolarGenerator tile) {
        return new ContainerProvider("mekanismgenerators.container.solar_generator", (i, inv, player) -> new SolarGeneratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntitySolarGenerator> getTileType() {
        return GeneratorsTileEntityTypes.SOLAR_GENERATOR.getTileEntityType();
    }
}