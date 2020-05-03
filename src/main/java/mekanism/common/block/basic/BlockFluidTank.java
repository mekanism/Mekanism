package mekanism.common.block.basic;

import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.block.IColoredBlock;
import mekanism.api.inventory.AutomationType;
import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.machine.prefab.BlockTile.BlockTileModel;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class BlockFluidTank extends BlockTileModel<TileEntityFluidTank, Machine<TileEntityFluidTank>> implements IColoredBlock {

    public BlockFluidTank(Machine<TileEntityFluidTank> type) {
        super(type, Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        int ambientLight = 0;
        TileEntityFluidTank tile = MekanismUtils.getTileEntity(TileEntityFluidTank.class, world, pos);
        if (tile != null) {
            if (MekanismConfig.client.enableAmbientLighting.get() && tile.lightUpdate() && tile.getActive()) {
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
        if (!player.isSneaking()) {
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
                        tile.fluidTank.extract(filled, Action.EXECUTE, AutomationType.MANUAL);
                        return true;
                    }
                }
            } else {
                FluidStack simulatedRemainder = tile.fluidTank.insert(fluidInItem, Action.SIMULATE, AutomationType.MANUAL);
                int remainder = simulatedRemainder.getAmount();
                int storedAmount = fluidInItem.getAmount();
                if (remainder < storedAmount) {
                    boolean filled = false;
                    FluidStack drained = handler.drain(new FluidStack(fluidInItem, storedAmount - remainder), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        ItemStack container = handler.getContainer();
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
                            tile.fluidTank.insert(drained, Action.EXECUTE, AutomationType.MANUAL);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public EnumColor getColor() {
        return Attribute.getBaseTier(this).getColor();
    }
}