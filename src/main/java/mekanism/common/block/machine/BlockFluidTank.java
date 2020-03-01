package mekanism.common.block.machine;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IColoredBlock;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.interfaces.IUpgradeableBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class BlockFluidTank extends BlockMekanism implements IHasModel, IHasGui<TileEntityFluidTank>, IColoredBlock, IStateActive, ITieredBlock<FluidTankTier>,
      IHasInventory, IHasTileEntity<TileEntityFluidTank>, ISupportsComparator, IHasSecurity, IStateFluidLoggable, IHasDescription, IUpgradeableBlock {

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
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        int ambientLight = 0;
        TileEntityFluidTank tile = MekanismUtils.getTileEntity(TileEntityFluidTank.class, world, pos);
        if (tile != null) {
            if (MekanismConfig.client.enableAmbientLighting.get() && tile.lightUpdate() && tile.wasActiveRecently()) {
                ambientLight = MekanismConfig.client.ambientLightingLevel.get();
            }
            FluidStack fluid = tile.fluidTank.getFluid();
            if (!fluid.isEmpty()) {
                FluidAttributes fluidAttributes = fluid.getFluid().getAttributes();
                //TODO: Decide if we want to always be using the luminosity of the stack
                ambientLight = Math.max(ambientLight, world instanceof ILightReader ? fluidAttributes.getLuminosity((ILightReader) world, pos)
                                                                                    : fluidAttributes.getLuminosity(fluid));
            }
        }
        return ambientLight;
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        TileEntityFluidTank tile = MekanismUtils.getTileEntity(TileEntityFluidTank.class, world, pos, true);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        //Handle filling fluid tank
        if (!player.isShiftKeyDown()) {
            if (SecurityUtils.canAccess(player, tile)) {
                ItemStack stack = player.getHeldItem(hand);
                if (!stack.isEmpty() && manageInventory(player, tile, hand, stack)) {
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
        ItemStack copyStack = StackUtils.size(itemStack, 1);
        Optional<IFluidHandlerItem> fluidHandlerItem = MekanismUtils.toOptional(FluidUtil.getFluidHandler(copyStack));
        if (fluidHandlerItem.isPresent()) {
            IFluidHandlerItem handler = fluidHandlerItem.get();
            FluidStack fluidInItem;
            if (tile.fluidTank.isEmpty()) {
                //If we don't have a fluid stored try draining in general
                fluidInItem = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            } else {
                //Otherwise try draining the same type of fluid we have stored
                // We do this to better support multiple tanks in case the fluid we have stored we could pull out of a block's
                // second tank but just asking to drain a specific amount
                fluidInItem = handler.drain(new FluidStack(tile.fluidTank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE);
            }
            if (fluidInItem.isEmpty()) {
                if (!tile.fluidTank.isEmpty()) {
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
                        //Note: if our FluidTank is creative it has a special FluidTank impl that will not actually drain it
                        tile.fluidTank.drain(filled, FluidAction.EXECUTE);
                        return true;
                    }
                }
            } else if (tile.fluidTank.isEmpty() || tile.fluidTank.getFluid().isFluidEqual(fluidInItem)) {
                boolean filled = false;
                int needed = tile.getCurrentNeeded();
                FluidStack drained = handler.drain(needed, player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                ItemStack container = handler.getContainer();
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
                        if (itemStack.isEmpty()) {
                            player.setHeldItem(hand, ItemStack.EMPTY);
                        }
                        filled = true;
                    }
                    if (filled) {
                        int toFill = Math.min(tile.fluidTank.getNeeded(), drained.getAmount());
                        //Note: if our FluidTank is creative it has a special FluidTank impl that will properly handle modifying the contents
                        tile.fluidTank.fill(new FluidStack(drained, toFill), FluidAction.EXECUTE);
                        if (drained.getAmount() - toFill > 0) {
                            tile.pushUp(new FluidStack(fluidInItem, drained.getAmount() - toFill), FluidAction.EXECUTE);
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
    public ContainerTypeRegistryObject<MekanismTileContainer<TileEntityFluidTank>> getContainerType() {
        return MekanismContainerTypes.FLUID_TANK;
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