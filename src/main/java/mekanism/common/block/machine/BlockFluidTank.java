package mekanism.common.block.machine;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.fluid_tank.TileEntityAdvancedFluidTank;
import mekanism.common.tile.fluid_tank.TileEntityBasicFluidTank;
import mekanism.common.tile.fluid_tank.TileEntityCreativeFluidTank;
import mekanism.common.tile.fluid_tank.TileEntityEliteFluidTank;
import mekanism.common.tile.fluid_tank.TileEntityFluidTank;
import mekanism.common.tile.fluid_tank.TileEntityUltimateFluidTank;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockFluidTank extends BlockMekanismContainer implements IHasModel, IHasGui, IColoredBlock, IStateFacing, IStateActive, ITieredBlock<FluidTankTier>,
      IHasInventory, IHasTileEntity<TileEntityFluidTank> {

    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);

    private final FluidTankTier tier;

    public BlockFluidTank(FluidTankTier tier) {
        super(Material.IRON);
        this.tier = tier;
        setHardness(3.5F);
        setResistance(16F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_fluid_tank"));
    }

    @Override
    public FluidTankTier getTier() {
        return tier;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState getActualState(@Nonnull BlockState state, IWorldReader world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    public int getLightValue(BlockState state, IWorldReader world, BlockPos pos) {
        if (MekanismConfig.current().client.enableAmbientLighting.val()) {
            TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
            if (tileEntity instanceof IActiveState && ((IActiveState) tileEntity).lightUpdate() && ((IActiveState) tileEntity).wasActiveRecently()) {
                return MekanismConfig.current().client.ambientLightingLevel.val();
            }
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, () -> new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos)) != WrenchResult.PASS) {
            return true;
        }
        //Handle filling fluid tank
        if (!player.isSneaking()) {
            if (SecurityUtils.canAccess(player, tileEntity)) {
                ItemStack stack = player.getHeldItem(hand);
                if (!stack.isEmpty() && FluidContainerUtils.isFluidContainer(stack) && manageInventory(player, (TileEntityFluidTank) tileEntity, hand, stack)) {
                    player.inventory.markDirty();
                    return true;
                }
            } else {
                SecurityUtils.displayNoAccess(player);
            }
            return true;
        }
        if (tileEntity.openGui(player)) {
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicFluidTank();
            case ADVANCED:
                return new TileEntityAdvancedFluidTank();
            case ELITE:
                return new TileEntityEliteFluidTank();
            case ULTIMATE:
                return new TileEntityUltimateFluidTank();
            case CREATIVE:
                return new TileEntityCreativeFluidTank();
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        //TODO: This is how it was before, but should it be divided by 5 like in Block.java
        return blockResistance;
    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IComparatorSupport) {
            return ((IComparatorSupport) tileEntity).getRedstoneLevel();
        }
        return 0;
    }

    private boolean manageInventory(PlayerEntity player, TileEntityFluidTank tileEntity, Hand hand, ItemStack itemStack) {
        ItemStack copyStack = StackUtils.size(itemStack.copy(), 1);
        if (FluidContainerUtils.isFluidContainer(itemStack)) {
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(copyStack);
            if (FluidUtil.getFluidContained(copyStack) == null) {
                if (tileEntity.fluidTank.getFluid() != null) {
                    int filled = handler.fill(tileEntity.fluidTank.getFluid(), !player.isCreative());
                    copyStack = handler.getContainer();
                    if (filled > 0) {
                        if (itemStack.getCount() == 1) {
                            player.setHeldItem(hand, copyStack);
                        } else if (itemStack.getCount() > 1 && player.inventory.addItemStackToInventory(copyStack)) {
                            itemStack.shrink(1);
                        } else {
                            player.dropItem(copyStack, false, true);
                            itemStack.shrink(1);
                        }
                        if (tileEntity.tier != FluidTankTier.CREATIVE) {
                            tileEntity.fluidTank.drain(filled, true);
                        }
                        return true;
                    }
                }
            } else {
                FluidStack itemFluid = FluidUtil.getFluidContained(copyStack);
                int needed = tileEntity.getCurrentNeeded();
                if (tileEntity.fluidTank.getFluid() != null && !tileEntity.fluidTank.getFluid().isFluidEqual(itemFluid)) {
                    return false;
                }
                boolean filled = false;
                FluidStack drained = handler.drain(needed, !player.isCreative());
                copyStack = handler.getContainer();
                if (copyStack.getCount() == 0) {
                    copyStack = ItemStack.EMPTY;
                }
                if (drained != null) {
                    if (player.isCreative()) {
                        filled = true;
                    } else if (!copyStack.isEmpty()) {
                        if (itemStack.getCount() == 1) {
                            player.setHeldItem(hand, copyStack);
                            filled = true;
                        } else if (player.inventory.addItemStackToInventory(copyStack)) {
                            itemStack.shrink(1);

                            filled = true;
                        }
                    } else {
                        itemStack.shrink(1);
                        if (itemStack.getCount() == 0) {
                            player.setHeldItem(hand, ItemStack.EMPTY);
                        }
                        filled = true;
                    }

                    if (filled) {
                        int toFill = tileEntity.fluidTank.getCapacity() - tileEntity.fluidTank.getFluidAmount();
                        if (tileEntity.tier != FluidTankTier.CREATIVE) {
                            toFill = Math.min(toFill, drained.amount);
                        }
                        tileEntity.fluidTank.fill(PipeUtils.copy(drained, toFill), true);
                        if (drained.amount - toFill > 0) {
                            tileEntity.pushUp(PipeUtils.copy(itemFluid, drained.amount - toFill), true);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(BlockState state, IWorldReader world, BlockPos pos) {
        return TANK_BOUNDS;
    }

    @Override
    public int getGuiID() {
        return 41;
    }

    @Override
    public EnumColor getColor() {
        return getTier().getBaseTier().getColor();
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityFluidTank> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicFluidTank.class;
            case ADVANCED:
                return TileEntityAdvancedFluidTank.class;
            case ELITE:
                return TileEntityEliteFluidTank.class;
            case ULTIMATE:
                return TileEntityUltimateFluidTank.class;
            case CREATIVE:
                return TileEntityCreativeFluidTank.class;
        }
        return null;
    }
}