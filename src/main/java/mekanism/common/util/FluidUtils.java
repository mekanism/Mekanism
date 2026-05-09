package mekanism.common.util;

import java.util.Collection;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FluidUtils {

    private FluidUtils() {
    }

    public static ItemStack getFilledVariant(Holder<Item> toFill, Holder<Fluid> fluid) {
        return getFilledVariant(new ItemStack(toFill), fluid);
    }

    public static ItemStack getFilledVariant(ItemStack toFill, Holder<Fluid> fluid) {
        IMekanismFluidHandler attachment = ContainerType.FLUID.createHandler(toFill);
        if (attachment != null) {
            FluidResource fluidType = FluidResource.of(fluid);
            for (IFluidTank fluidTank : attachment.getContainers()) {
                fluidTank.setContents(fluidType, fluidTank.getLimit(fluidType));
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    public static int getRGBDurabilityForDisplay(ItemStack stack) {
        return getRGBDurabilityForDisplay(StorageUtils.getFirstFluidFromAttachment(stack));
    }

    public static int getRGBDurabilityForDisplay(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0xFFFFFFFF;
        }
        //TODO: Technically doesn't support things where the color is part of the texture such as lava
        // for chemicals it is supported via allowing people to override getColorRepresentation in their
        // chemicals
        if (stack.getFluid().isSame(Fluids.LAVA)) {//Special case lava
            return 0xFFDB6B19;
        } else if (FMLEnvironment.getDist().isClient()) {
            //Note: We can only return an accurate result on the client side. This method should never be called from the server
            // but in case it is make sure we only run on the client side
            return MekanismRenderer.getColorARGB(stack);
        }
        return 0xFFFFFFFF;
    }

    public static void emit(Collection<BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> targets, IFluidTank tank) {
        emit(targets, tank, tank.getCurrentLimit());
    }

    public static void emit(Collection<BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> targets, IFluidTank tank, int maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0 && !targets.isEmpty()) {
            tank.extract(emit(targets, FluidStack.EMPTY, tank, maxOutput), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits fluid from a central block by splitting the received stack among the sides given.
     *
     * @param targets - the list of capabilities to output to
     * @param stack   - the stack to output
     *
     * @return the amount of fluid emitted
     */
    public static int emit(Collection<BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> targets, @NotNull FluidStack stack) {
        return emit(targets, stack, null, Integer.MAX_VALUE);
    }

    private static int emit(Collection<BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> targets, @NotNull FluidStack stack, IFluidTank tank,
          int maxOutput) {
        if (stack.isEmpty() && tank == null) {
            //Something went wrong in calling this method
            return 0;
        } else if (targets.isEmpty()) {
            return 0;
        }
        FluidStack toSend = stack.copy();
        FluidHandlerTarget target = null;
        for (BlockCapabilityCache<ResourceHandler<FluidResource>, Direction> capability : targets) {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            ResourceHandler<FluidResource> handler = capability.getCapability();
            if (handler != null) {
                //If we weren't given a stack by the caller, then we want to lazily try to extract from the tank to see how much we are trying to emit
                // so that we don't have to attempt an extraction if all our targets are actually not currently fluid handlers
                if (stack.isEmpty()) {
                    stack = tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
                    if (stack.isEmpty()) {
                        //If we failed to extract from it, just exit early
                        return 0;
                    }
                    //Note: We need to copy and use toSend instead of just passing stack to canFill, as the docs for IFluidHandler don't specify
                    // whether it is safe to modify the passed in stack
                    toSend = stack.copy();
                }
                if (canFill(handler, toSend)) {
                    if (target == null) {
                        target = new FluidHandlerTarget(targets.size());
                    }
                    target.addHandler(handler);
                }
            }
        }
        return EmitUtils.sendToAcceptors(target, stack.amount(), toSend);
    }

    public static boolean canFill(ResourceHandler<FluidResource> handler, @NotNull FluidStack stack) {
        //TODO - 26.1: Check callers as they might be in a transaction context
        try (Transaction simulation = Transaction.openRoot()) {
            return handler.insert(FluidResource.of(stack), stack.amount(), simulation) > 0;
        }
    }

    //TODO - 26.1: Do we want to just replace this with FluidUtil#interactWithFluidHandler?
    public static boolean handleTankInteraction(Player player, InteractionHand hand, ItemStack itemStack, IFluidTank fluidTank) {
        //TODO - 26.1: Figure out whether there are cases where we would want it without the oneByOne access
        // And if we should get the item access from the player's interaction hand or from the passed stack.
        // I think the adding back to inventory part needs the player interaction item access?
        ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(ItemAccess.forPlayerInteraction(player, hand).oneByOne());
        if (handler == null) {
            return false;
        }
        FluidResource fluidType;
        if (fluidTank.isEmpty()) {
            //If we don't have a fluid stored try draining in general a fluid that will be able to be inserted into the tank
            fluidType = ResourceHandlerUtil.findExtractableResource(handler, type -> fluidTank.isValidForInsertion(type, AutomationType.MANUAL), null);
            if (fluidType == null) {
                //Nothing extractable and tank is empty so can't go from tank to handler
                return false;
            }
        } else {
            //Otherwise, try draining the same type of fluid we have stored
            fluidType = fluidTank.getResource();
        }
        int amountInItem;
        try (Transaction simulation = Transaction.openRoot()) {
            amountInItem = handler.extract(fluidType, Integer.MAX_VALUE, simulation);
        }
        if (amountInItem == 0) {
            if (fluidTank.isEmpty()) {
                return false;
            }
            int spaceInItem;
            try (Transaction simulation = Transaction.openRoot()) {
                spaceInItem = handler.insert(fluidTank.getResource(), fluidTank.amount(), simulation);
                if (spaceInItem == 0) {
                    return false;
                }
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = fluidTank.extract(fluidType, spaceInItem, transaction, AutomationType.MANUAL);
                if (extracted == 0) {
                    return false;
                }
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    int inserted = handler.insert(fluidTank.getResource(), extracted, subTransaction);
                    if (inserted < extracted) {
                        return false;
                    } else if (!player.isCreative()) {//TODO - 26.1: Re-evaluate this
                        subTransaction.commit();
                    }
                }
                //TODO - 26.1: I believe the item access is responsible for handling this now
                /*ItemStack container = handler.getContainer();
                if (itemStack.count() == 1) {
                    player.setItemInHand(hand, container);
                } else if (itemStack.count() > 1 && player.getInventory().add(container)) {
                    itemStack.shrink(1);
                } else {
                    player.drop(container, false, true);
                    itemStack.shrink(1);
                }*/
                transaction.commit();
                return true;
            }
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = fluidTank.insert(fluidType, amountInItem, transaction, AutomationType.MANUAL);
            if (inserted == 0) {
                return false;
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                int extracted = handler.extract(fluidType, inserted, subTransaction);
                if (extracted < inserted) {
                    return false;
                } else if (!player.isCreative()) {//TODO - 26.1: Re-evaluate this
                    subTransaction.commit();
                }
            }
            //TODO - 26.1: I believe the item access is responsible for handling this now
            /*boolean filled = false;
            ItemStack container = handler.getContainer();
            if (player.isCreative()) {
                filled = true;
            } else if (!container.isEmpty()) {
                if (itemStack.count() == 1) {
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
            }*/
            transaction.commit();
            return true;
        }
    }
}