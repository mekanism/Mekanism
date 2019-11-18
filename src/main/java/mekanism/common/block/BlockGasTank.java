package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.block.ISupportsRedstone;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.GasTankContainer;
import mekanism.common.item.block.ItemBlockGasTank;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockGasTank extends BlockMekanismContainer implements IHasGui<TileEntityGasTank>, IStateFacing, ITieredBlock<GasTankTier>, IHasInventory, IHasSecurity,
      ISupportsRedstone, IHasTileEntity<TileEntityGasTank>, ISupportsComparator, IStateWaterLogged {

    private static final VoxelShape TANK_BOUNDS = VoxelShapes.create(0.1875F, 0.0F, 0.1875F, 0.8125F, 1.0F, 0.8125F);

    private final GasTankTier tier;

    public BlockGasTank(GasTankTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
        this.tier = tier;
    }

    @Override
    public GasTankTier getTier() {
        return tier;
    }

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityGasTank) {
            TileEntityGasTank gasTank = (TileEntityGasTank) tile;
            gasTank.gasTank.setCapacity(gasTank.tier.getStorage());
            gasTank.gasTank.setStack(((ItemBlockGasTank) stack.getItem()).getGas(stack));
        }
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

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return TANK_BOUNDS;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityGasTank tile) {
        return new ContainerProvider("mekanism.container.gas_tank", (i, inv, player) -> new GasTankContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityGasTank> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_GAS_TANK.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_GAS_TANK.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_GAS_TANK.getTileEntityType();
            case CREATIVE:
                return MekanismTileEntityTypes.CREATIVE_GAS_TANK.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_GAS_TANK.getTileEntityType();
        }
    }
}