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
              Block.makeCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),//base
              Block.makeCuboidShape(1.5, 4.0, 1.5, 14.5, 5.0, 14.5),//baseStand
              Block.makeCuboidShape(3.0, 4.0, 3.0, 13.0, 16.0, 13.0),//center
              Block.makeCuboidShape(12.0, 5.0, 12.0, 15.0, 14.0, 15.0),//pillar1
              Block.makeCuboidShape(1.0, 5.0, 12.0, 4.0, 14.0, 15.0),//pillar2
              Block.makeCuboidShape(12.0, 5.0, 1.0, 15.0, 14.0, 4.0),//pillar3
              Block.makeCuboidShape(1.0, 5.0, 1.0, 4.0, 14.0, 4.0),//pillar4
              Block.makeCuboidShape(4.0, 4.0, 15.0, 12.0, 12.0, 16.0),//port1
              Block.makeCuboidShape(15.0, 4.0, 4.0, 16.0, 12.0, 12.0),//port2
              Block.makeCuboidShape(4.0, 4.0, 0.0, 12.0, 12.0, 1.0),//port3
              Block.makeCuboidShape(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),//port4
              Block.makeCuboidShape(4.0, 10.0, 13.5, 12.0, 11.0, 15.5),//connector1
              Block.makeCuboidShape(13.0, 12.0, 4.0, 14.0, 13.0, 12.0),//connector2
              Block.makeCuboidShape(2.0, 12.0, 4.0, 3.0, 13.0, 12.0),//connector3
              Block.makeCuboidShape(4.0, 12.0, 2.0, 12.0, 13.0, 3.0)//connector4
              //Block.makeCuboidShape(4.0, 9.5, 12.0, 12.0, 10.5, 13.5),//connectorAngle1
              //Block.makeCuboidShape(13.0, 11.0, 4.0, 14.0, 13.0, 12.0),//connectorAngle2
              //Block.makeCuboidShape(1.0, 11.0, 4.0, 3.0, 13.0, 12.0),//connectorAngle3
              //Block.makeCuboidShape(4.0, 11.0, 1.0, 12.0, 13.0, 3.0)//connectorAngle4
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