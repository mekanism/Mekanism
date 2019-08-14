package mekanism.common.block;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.block.interfaces.ISupportsRedstone;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.item.block.ItemBlockGasTank;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.gas_tank.TileEntityAdvancedGasTank;
import mekanism.common.tile.gas_tank.TileEntityBasicGasTank;
import mekanism.common.tile.gas_tank.TileEntityCreativeGasTank;
import mekanism.common.tile.gas_tank.TileEntityEliteGasTank;
import mekanism.common.tile.gas_tank.TileEntityGasTank;
import mekanism.common.tile.gas_tank.TileEntityUltimateGasTank;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockGasTank extends BlockMekanismContainer implements IHasGui, IStateFacing, ITieredBlock<GasTankTier>, IHasInventory, IHasSecurity, ISupportsRedstone,
      IHasTileEntity<TileEntityGasTank>, ISupportsComparator {

    private static final VoxelShape TANK_BOUNDS = VoxelShapes.create(0.1875F, 0.0F, 0.1875F, 0.8125F, 1.0F, 0.8125F);

    private final GasTankTier tier;

    public BlockGasTank(GasTankTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
        this.tier = tier;
        setRegistryName(new ResourceLocation(Mekanism.MODID, tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_gas_tank"));
    }

    @Override
    public GasTankTier getTier() {
        return tier;
    }

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityGasTank) {
            TileEntityGasTank gasTank = (TileEntityGasTank) tile;
            gasTank.gasTank.setMaxGas(gasTank.tier.getStorage());
            gasTank.gasTank.setGas(((ItemBlockGasTank) stack.getItem()).getGas(stack));
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
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
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        if (tileEntity.openGui(player)) {
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return TANK_BOUNDS;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicGasTank();
            case ADVANCED:
                return new TileEntityAdvancedGasTank();
            case ELITE:
                return new TileEntityEliteGasTank();
            case ULTIMATE:
                return new TileEntityUltimateGasTank();
            case CREATIVE:
                return new TileEntityCreativeGasTank();
        }
        return null;
    }

    @Override
    public int getGuiID() {
        return 10;
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityGasTank> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicGasTank.class;
            case ADVANCED:
                return TileEntityAdvancedGasTank.class;
            case ELITE:
                return TileEntityEliteGasTank.class;
            case ULTIMATE:
                return TileEntityUltimateGasTank.class;
            case CREATIVE:
                return TileEntityCreativeGasTank.class;
        }
        return null;
    }
}