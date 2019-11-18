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
import mekanism.common.base.IActiveState;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.FluidTankContainer;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BlockFluidTank extends BlockMekanismContainer implements IHasModel, IHasGui<TileEntityFluidTank>, IColoredBlock, IStateActive, ITieredBlock<FluidTankTier>,
      IHasInventory, IHasTileEntity<TileEntityFluidTank>, ISupportsComparator, IHasSecurity, IStateWaterLogged {

    private static final VoxelShape TANK_BOUNDS = VoxelShapes.create(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);

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
    public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos);
            if (tileEntity instanceof IActiveState && ((IActiveState) tileEntity).lightUpdate() && ((IActiveState) tileEntity).wasActiveRecently()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
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
                return true;
            }
        }
        return tileEntity.openGui(player);
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
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

    private boolean manageInventory(PlayerEntity player, TileEntityFluidTank tileEntity, Hand hand, ItemStack itemStack) {
        ItemStack copyStack = StackUtils.size(itemStack.copy(), 1);
        return new LazyOptionalHelper<>(FluidUtil.getFluidHandler(copyStack)).getIfPresentElse(
              handler -> new LazyOptionalHelper<>(FluidUtil.getFluidContained(copyStack)).getIfPresentElseDo(
                    itemFluid -> {
                        int needed = tileEntity.getCurrentNeeded();
                        if (!tileEntity.fluidTank.getFluid().isEmpty() && !tileEntity.fluidTank.getFluid().isFluidEqual(itemFluid)) {
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
                                int toFill = tileEntity.fluidTank.getCapacity() - tileEntity.fluidTank.getFluidAmount();
                                if (tileEntity.tier != FluidTankTier.CREATIVE) {
                                    toFill = Math.min(toFill, drained.getAmount());
                                }
                                tileEntity.fluidTank.fill(PipeUtils.copy(drained, toFill), FluidAction.EXECUTE);
                                if (drained.getAmount() - toFill > 0) {
                                    tileEntity.pushUp(PipeUtils.copy(itemFluid, drained.getAmount() - toFill), FluidAction.EXECUTE);
                                }
                                return true;
                            }
                        }
                        return false;
                    },
                    () -> {
                        if (!tileEntity.fluidTank.getFluid().isEmpty()) {
                            int filled = handler.fill(tileEntity.fluidTank.getFluid(), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
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
                                if (tileEntity.tier != FluidTankTier.CREATIVE) {
                                    tileEntity.fluidTank.drain(filled, FluidAction.EXECUTE);
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
        return TANK_BOUNDS;
    }

    @Override
    public EnumColor getColor() {
        return getTier().getBaseTier().getColor();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityFluidTank tile) {
        return new ContainerProvider("mekanism.container.fluid_tank", (i, inv, player) -> new FluidTankContainer(i, inv, tile));
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
}