package mekanism.generators.common.block;

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
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.inventory.container.GasBurningGeneratorContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
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

public class BlockGasBurningGenerator extends BlockMekanismContainer implements IHasGui<TileEntityGasGenerator>, IBlockElectric, IStateFacing, IHasInventory, IHasSecurity, IBlockSound,
      IHasTileEntity<TileEntityGasGenerator>, ISupportsComparator {

    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.gen.gas"));
    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape generator = MultipartUtils.combine(
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
              makeCuboidShape(4, 10, 13.5, 12, 11, 15.5),//connector1
              makeCuboidShape(13, 12, 4, 14, 13, 12),//connector2
              makeCuboidShape(2, 12, 4, 3, 13, 12),//connector3
              makeCuboidShape(4, 12, 2, 12, 13, 3)//connector4
              //makeCuboidShape(4, 9.5, 12, 12, 10.5, 13.5),//connectorAngle1
              //makeCuboidShape(13, 11, 4, 14, 13, 12),//connectorAngle2
              //makeCuboidShape(1, 11, 4, 3, 13, 12),//connectorAngle3
              //makeCuboidShape(4, 11, 1, 12, 13, 3)//connectorAngle4
        );
        //TODO: VoxelShapes, Figure out best way of handling the "angled connector pieces"
        //setRotation(connectorAngle1, 0.986111F, 0F, 0F);
        //setRotation(connectorAngle2, 0F, 0F, 0.7941248F);
        //setRotation(connectorAngle3, 0F, 0F, -0.7941248F);
        //setRotation(connectorAngle4, 0.7941248F, 0F, 0F);
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = MultipartUtils.rotateHorizontal(generator, side);
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
        return 100 * MekanismConfig.general.FROM_H2.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityGasGenerator tile) {
        return new ContainerProvider("mekanismgenerators.container.gas_burning_generator", (i, inv, player) -> new GasBurningGeneratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityGasGenerator> getTileType() {
        return GeneratorsTileEntityTypes.GAS_BURNING_GENERATOR.getTileEntityType();
    }
}