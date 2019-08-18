package mekanism.generators.common.block.turbine;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.GeneratorsItem;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockTurbineRotor extends BlockMekanismContainer implements IHasTileEntity<TileEntityTurbineRotor> {

    private static final VoxelShape ROTOR_BOUNDS = VoxelShapes.create(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);

    public BlockTurbineRotor() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
        setRegistryName(new ResourceLocation(MekanismGenerators.MODID, "turbine_rotor"));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            final TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!world.isRemote && state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
            TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityTurbineRotor) {
                //TODO: Evaluate
                int amount = ((TileEntityTurbineRotor) tileEntity).getHousedBlades();
                if (amount > 0) {
                    spawnAsEntity(world, pos, GeneratorsItem.TURBINE_BLADE.getItemStack(amount));
                }
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }

        ItemStack stack = player.getHeldItem(hand);
        TileEntityTurbineRotor rod = (TileEntityTurbineRotor) tileEntity;
        if (!player.isSneaking()) {
            if (!stack.isEmpty() && stack.getItem() == GeneratorsItem.TURBINE_BLADE.getItem()) {
                if (rod.addBlade()) {
                    if (!player.isCreative()) {
                        stack.shrink(1);
                        if (stack.getCount() == 0) {
                            player.setHeldItem(hand, ItemStack.EMPTY);
                        }
                    }
                }
                return true;
            }
        } else if (stack.isEmpty()) {
            if (rod.removeBlade()) {
                if (!player.isCreative()) {
                    player.setHeldItem(hand, GeneratorsItem.TURBINE_BLADE.getItemStack());
                    player.inventory.markDirty();
                }
            }
        } else if (stack.getItem() == GeneratorsItem.TURBINE_BLADE.getItem()) {
            if (stack.getCount() < stack.getMaxStackSize()) {
                if (rod.removeBlade()) {
                    if (!player.isCreative()) {
                        stack.grow(1);
                        player.inventory.markDirty();
                    }
                }
            }
        }
        return true;
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
        return ROTOR_BOUNDS;
    }

    @Override
    public TileEntityType<TileEntityTurbineRotor> getTileType() {
        return GeneratorsTileEntityTypes.TURBINE_ROTOR;
    }
}