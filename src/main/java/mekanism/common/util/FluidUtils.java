package mekanism.common.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.OptionalInt;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.providers.IFluidProvider;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public final class FluidUtils {

    private FluidUtils() {
    }

    public static ItemStack getFilledVariant(ItemStack toFill, CachedIntValue capacity, IFluidProvider provider) {
        return getFilledVariant(toFill, capacity.getOrDefault(), provider);
    }

    public static ItemStack getFilledVariant(ItemStack toFill, int capacity, IFluidProvider provider) {
        IExtendedFluidTank dummyTank = BasicFluidTank.create(capacity, null);
        //Manually handle filling it as capabilities are not necessarily loaded yet (at least not on the first call to this, which is made via fillItemGroup)
        dummyTank.setStack(provider.getFluidStack(dummyTank.getCapacity()));
        ItemDataUtils.writeContainers(toFill, NBTConstants.FLUID_TANKS, Collections.singletonList(dummyTank));
        //The item is now filled return it for convenience
        return toFill;
    }

    public static OptionalInt getRGBDurabilityForDisplay(ItemStack stack) {
        return getRGBDurabilityForDisplay(StorageUtils.getStoredFluidFromNBT(stack));
    }

    public static OptionalInt getRGBDurabilityForDisplay(FluidStack stack) {
        if (!stack.isEmpty()) {
            //TODO: Technically doesn't support things where the color is part of the texture such as lava
            // for chemicals it is supported via allowing people to override getColorRepresentation in their
            // chemicals
            if (stack.getFluid().isSame(Fluids.LAVA)) {//Special case lava
                return OptionalInt.of(0xFFDB6B19);
            } else if (FMLEnvironment.dist.isClient()) {
                //Note: We can only return an accurate result on the client side. This method should never be called from the server
                // but in case it is make sure we only run on the client side
                return OptionalInt.of(IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack));
            }
        }
        return OptionalInt.empty();
    }

    public static void emit(IExtendedFluidTank tank, BlockEntity from) {
        emit(EnumSet.allOf(Direction.class), tank, from);
    }

    public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, BlockEntity from) {
        emit(outputSides, tank, from, tank.getCapacity());
    }

    public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, BlockEntity from, int maxOutput) {
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
    public static int emit(Set<Direction> sides, @NotNull FluidStack stack, BlockEntity from) {
        if (stack.isEmpty() || sides.isEmpty()) {
            return 0;
        }
        FluidStack toSend = stack.copy();
        FluidHandlerTarget target = new FluidHandlerTarget(stack, 6);
        EmitUtils.forEachSide(from.getLevel(), from.getBlockPos(), sides, (level, pos, opposite) -> {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            IFluidHandler handler = WorldUtils.getCapability(level, FluidHandler.BLOCK, pos, opposite);
            if (handler != null && canFill(handler, toSend)) {
                target.addHandler(handler);
            }
        });
        if (target.getHandlerCount() > 0) {
            return EmitUtils.sendToAcceptors(target, stack.getAmount(), toSend);
        }
        return 0;
    }

    public static boolean canFill(IFluidHandler handler, @NotNull FluidStack stack) {
        return handler.fill(stack, FluidAction.SIMULATE) > 0;
    }

    public static boolean handleTankInteraction(Player player, InteractionHand hand, ItemStack itemStack, IExtendedFluidTank fluidTank) {
        ItemStack copyStack = itemStack.copyWithCount(1);
        IFluidHandlerItem handler = copyStack.getCapability(FluidHandler.ITEM);
        if (handler != null) {
            FluidStack fluidInItem;
            if (fluidTank.isEmpty()) {
                //If we don't have a fluid stored try draining in general
                fluidInItem = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            } else {
                //Otherwise, try draining the same type of fluid we have stored
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
                            player.setItemInHand(hand, container);
                        } else if (itemStack.getCount() > 1 && player.getInventory().add(container)) {
                            itemStack.shrink(1);
                        } else {
                            player.drop(container, false, true);
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
                            if (itemStack.getCount() == 1) {
                                player.setItemInHand(hand, container);
                                filled = true;
                            } else if (player.getInventory().add(container)) {
                                itemStack.shrink(1);
                                filled = true;
                            }
                        } else {
                            itemStack.shrink(1);
                            if (itemStack.isEmpty()) {
                                player.setItemInHand(hand, ItemStack.EMPTY);
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