package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public final class FluidUtils {

    private FluidUtils() {
    }

    public static void emit(IExtendedFluidTank tank, TileEntity from) {
        emit(EnumSet.allOf(Direction.class), tank, from);
    }

    public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, TileEntity from) {
        emit(outputSides, tank, from, tank.getCapacity());
    }

    public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, TileEntity from, int maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0) {
            tank.extract(emit(outputSides, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits fluid from a central block by splitting the received stack among the sides given.
     *
     * @param sides - the list of sides to output from
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     *
     * @return the amount of fluid emitted
     */
    public static int emit(Set<Direction> sides, @Nonnull FluidStack stack, TileEntity from) {
        if (stack.isEmpty() || sides.isEmpty()) {
            return 0;
        }
        FluidStack toSend = stack.copy();
        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        FluidHandlerTarget target = new FluidHandlerTarget(stack);
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {
            //Insert to access side
            Direction accessSide = side.getOpposite();
            //Collect cap
            CapabilityUtils.getCapability(acceptor, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, accessSide).ifPresent(handler -> {
                if (canFill(handler, toSend)) {
                    target.addHandler(accessSide, handler);
                }
            });
        });

        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<FluidHandlerTarget> targets = new ObjectOpenHashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.getAmount(), toSend);
        }
        return 0;
    }

    public static boolean canFill(IFluidHandler handler, @Nonnull FluidStack stack) {
        return handler.fill(stack, FluidAction.SIMULATE) > 0;
    }

    public static boolean handleTankInteraction(PlayerEntity player, Hand hand, ItemStack itemStack, IExtendedFluidTank fluidTank) {
        ItemStack copyStack = StackUtils.size(itemStack, 1);
        Optional<IFluidHandlerItem> fluidHandlerItem = FluidUtil.getFluidHandler(copyStack).resolve();
        if (fluidHandlerItem.isPresent()) {
            IFluidHandlerItem handler = fluidHandlerItem.get();
            FluidStack fluidInItem;
            if (fluidTank.isEmpty()) {
                //If we don't have a fluid stored try draining in general
                fluidInItem = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            } else {
                //Otherwise try draining the same type of fluid we have stored
                // We do this to better support multiple tanks in case the fluid we have stored we could pull out of a block's
                // second tank but just asking to drain a specific amount
                fluidInItem = handler.drain(new FluidStack(fluidTank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE);
            }
            if (fluidInItem.isEmpty()) {
                if (!fluidTank.isEmpty()) {
                    int filled = handler.fill(fluidTank.getFluid(), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
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
                        fluidTank.extract(filled, Action.EXECUTE, AutomationType.MANUAL);
                        return true;
                    }
                }
            } else {
                FluidStack simulatedRemainder = fluidTank.insert(fluidInItem, Action.SIMULATE, AutomationType.MANUAL);
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
                            fluidTank.insert(drained, Action.EXECUTE, AutomationType.MANUAL);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}