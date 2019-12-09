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
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.inventory.container.WindGeneratorContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityWindGenerator;
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
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockWindGenerator extends BlockMekanism implements IHasGui<TileEntityWindGenerator>, IBlockElectric, IStateFacing, IHasInventory, IHasSecurity, IBlockSound,
      IHasTileEntity<TileEntityWindGenerator>, IStateActive {

    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.gen.wind"));
    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    //TODO: VoxelShapes: FIXME
    static {
        VoxelShape generator = VoxelShapeUtils.combine(
              makeCuboidShape(1, 65, 3, 11.5, 75.5, 12),//head
              makeCuboidShape(4, 4, 15, 12, 12, 16),//plate
              //makeCuboidShape(6, 70, 7, 9, 73, 16),//bladeCap
              //makeCuboidShape(4, 68, 5, 10, 74, 15),//bladeCenter
              makeCuboidShape(2, 1, 2, 14, 3, 14),//baseRim
              makeCuboidShape(0, 0, 0, 16, 2, 16),//base
              makeCuboidShape(6, 5, 7.5, 9, 70, 10.6),//wire
              makeCuboidShape(6, 3, 11.5, 10, 5, 13.5),//plateConnector
              makeCuboidShape(5, 5, 5, 11, 11, 15),//plateConnector2
              makeCuboidShape(3, 62.5, 1, 10.5, 74.5, 4),//rearPlate1
              makeCuboidShape(5, 64, -1, 9.5, 74, 2),//rearPlate2
              makeCuboidShape(3, 2, 3, 10.5, 70, 10.5),//post1a
              makeCuboidShape(3, 2, 3, 10.5, 70, 10.5),//post1b
              makeCuboidShape(3, 2, 3, 10.5, 70, 10.5),//post1c
              makeCuboidShape(3, 2, 3, 10.5, 70, 10.5)//post1d
        );
        /*setRotation(wire, -0.0349066F, 0F, 0F);
        setRotation(rearPlate1, 0.122173F, 0F, 0F);
        setRotation(rearPlate2, 0.2094395F, 0F, 0F);
        setRotation(post1a, -0.0349066F, 0F, 0.0349066F);
        setRotation(post1b, 0.0349066F, 0F, -0.0349066F);
        setRotation(post1c, 0.0347321F, 0F, 0.0347321F);
        setRotation(post1d, -0.0347321F, 0F, -0.0347321F);*/
        generator = VoxelShapeUtils.rotate(generator, Rotation.CLOCKWISE_180);
        ModelWindGenerator model = new ModelWindGenerator();
        /*generator = VoxelShapeUtils.getShapeFromModel(model.head, model.plateConnector2, model.plateConnector, model.plate,
              model.baseRim, model.base, model.rearPlate1, model.rearPlate2, model.wire, model.post1a, model.post1b, model.post1c, model.post1d);//*/
        generator = VoxelShapeUtils.getShapeFromModel(model.rearPlate1);//, model.rearPlate2);
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
        return 200_000;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityWindGenerator tile) {
        return new ContainerProvider("mekanismgenerators.container.wind_generator", (i, inv, player) -> new WindGeneratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityWindGenerator> getTileType() {
        return GeneratorsTileEntityTypes.WIND_GENERATOR.getTileEntityType();
    }
}