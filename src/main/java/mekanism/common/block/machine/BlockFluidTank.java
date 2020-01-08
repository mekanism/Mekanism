package mekanism.common.block.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IColoredBlock;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILangEntry;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.interfaces.IUpgradeableBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.FluidTankContainer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BlockFluidTank extends BlockMekanism implements IHasModel, IHasGui<TileEntityFluidTank>, IColoredBlock, IStateActive, ITieredBlock<FluidTankTier>,
      IHasInventory, IHasTileEntity<TileEntityFluidTank>, ISupportsComparator, IHasSecurity, IStateWaterLogged, IHasDescription, IUpgradeableBlock {

    private static final VoxelShape bounds = makeCuboidShape(2, 0, 2, 14, 16, 14);

    private final FluidTankTier tier;

    public BlockFluidTank(FluidTankTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
        this.tier = tier;
    }

    @Override
    public FluidTankTier getTier() {
        return tier;
    }

    @Override
    public int getLightValue(BlockState state, ILightReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).wasActiveRecently()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
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
        //Handle filling fluid tank
        if (!player.func_225608_bj_()) {
            if (SecurityUtils.canAccess(player, tile)) {
                ItemStack stack = player.getHeldItem(hand);
                if (!stack.isEmpty() && FluidContainerUtils.isFluidContainer(stack) && manageInventory(player, (TileEntityFluidTank) tile, hand, stack)) {
                    player.inventory.markDirty();
                    return ActionResultType.SUCCESS;
                }
            } else {
                SecurityUtils.displayNoAccess(player);
                return ActionResultType.SUCCESS;
            }
        }
        return tile.openGui(player);
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        //TODO: This is how it was before, but should it be divided by 5 like in Block.java
        return blockResistance;
    }

    private boolean manageInventory(PlayerEntity player, TileEntityFluidTank tile, Hand hand, ItemStack itemStack) {
        ItemStack copyStack = StackUtils.size(itemStack.copy(), 1);
        return new LazyOptionalHelper<>(FluidUtil.getFluidHandler(copyStack)).getIfPresentElse(
              handler -> new LazyOptionalHelper<>(FluidUtil.getFluidContained(copyStack)).getIfPresentElseDo(
                    itemFluid -> {
                        int needed = tile.getCurrentNeeded();
                        if (!tile.fluidTank.getFluid().isEmpty() && !tile.fluidTank.getFluid().isFluidEqual(itemFluid)) {
                            return false;
                        }
                        boolean filled = false;
                        FluidStack drained = handler.drain(needed, player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                        ItemStack container = handler.getContainer();
                        if (container.getCount() == 0) {
                            container = ItemStack.EMPTY;
                        }
                        if (!drained.isEmpty()) {
                            if (player.isCreative()) {
                                filled = true;
                            } else if (!container.isEmpty()) {
                                if (container.getCount() == 1) {
                                    player.setHeldItem(hand, container);
                                    filled = true;
                                } else if (player.inventory.addItemStackToInventory(container)) {
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
                                int toFill = tile.fluidTank.getCapacity() - tile.fluidTank.getFluidAmount();
                                if (tile.tier != FluidTankTier.CREATIVE) {
                                    toFill = Math.min(toFill, drained.getAmount());
                                }
                                tile.fluidTank.fill(PipeUtils.copy(drained, toFill), FluidAction.EXECUTE);
                                if (drained.getAmount() - toFill > 0) {
                                    tile.pushUp(PipeUtils.copy(itemFluid, drained.getAmount() - toFill), FluidAction.EXECUTE);
                                }
                                return true;
                            }
                        }
                        return false;
                    },
                    () -> {
                        if (!tile.fluidTank.getFluid().isEmpty()) {
                            int filled = handler.fill(tile.fluidTank.getFluid(), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                            ItemStack container = handler.getContainer();
                            if (filled > 0) {
                                if (itemStack.getCount() == 1) {
                                    player.setHeldItem(hand, container);
                                } else if (itemStack.getCount() > 1 && player.inventory.addItemStackToInventory(container)) {
                                    itemStack.shrink(1);
                                } else {
                                    player.dropItem(container, false, true);
                                    itemStack.shrink(1);
                                }
                                if (tile.tier != FluidTankTier.CREATIVE) {
                                    tile.fluidTank.drain(filled, FluidAction.EXECUTE);
                                }
                                return true;
                            }
                        }
                        return false;
                    }
              ),
              false
        );
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

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds;
    }

    @Override
    public EnumColor getColor() {
        return getTier().getBaseTier().getColor();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityFluidTank tile) {
        return new ContainerProvider(TextComponentUtil.translate(getTranslationKey()), (i, inv, player) -> new FluidTankContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityFluidTank> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_FLUID_TANK.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_FLUID_TANK.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_FLUID_TANK.getTileEntityType();
            case CREATIVE:
                return MekanismTileEntityTypes.CREATIVE_FLUID_TANK.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_FLUID_TANK.getTileEntityType();
        }
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_FLUID_TANK;
    }

    @Nonnull
    @Override
    public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        switch (tier) {
            case BASIC:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_FLUID_TANK.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_FLUID_TANK.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_FLUID_TANK.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_FLUID_TANK.getBlock().getDefaultState());
            case CREATIVE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.CREATIVE_FLUID_TANK.getBlock().getDefaultState());
        }
        return current;
    }
}