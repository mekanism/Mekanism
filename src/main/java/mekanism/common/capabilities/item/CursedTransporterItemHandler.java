package mekanism.common.capabilities.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class CursedTransporterItemHandler implements IItemHandler {

    private final Map<GlobalPos, Set<TransporterStack>> simulatedFlowingStacks = new Object2ObjectOpenHashMap<>();
    //Note: We keep track of stacks that insert as simulate has seen and also stacks that actually inserting has seen
    // this is because if a stack is simulated it is likely the same stack may be used for actually inserting, but we
    // want to make sure that if a mod is just trying to insert without simulating across the different slots that we
    // can short circuit if we couldn't insert it
    private final Set<ItemStack> seenStacks = new ReferenceOpenHashSet<>();
    private final Set<ItemStack> seenExecutedStacks = new ReferenceOpenHashSet<>();
    private final LogisticalTransporterBase transporter;
    private final LongSupplier currentTickSupplier;
    private final long fromPos;
    private long lastTick;

    public CursedTransporterItemHandler(LogisticalTransporterBase transporter, long fromPos, LongSupplier currentTickSupplier) {
        this.transporter = transporter;
        this.fromPos = fromPos;
        this.currentTickSupplier = currentTickSupplier;
    }

    @Override
    public int getSlots() {
        //Pretend we have nine slots as we will short circuit if repeat calls are made, and some mods validate the total number
        // of slots before sending things (for example refined storage's crafters)
        return 9;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    private TransitRequest getRequest(int limit, ItemStack stack) {
        //If the stack is already the correct size skip copying it and resizing by using the source stack
        // as our simple transit request won't have the stack get mutated
        if (stack.getCount() <= limit) {
            return TransitRequest.simple(stack);
        }
        return TransitRequest.simple(stack.copyWithCount(limit));
    }

    public LogisticalTransporterBase getTransporter() {
        return transporter;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack itemStack, boolean simulate) {
        //Note: This method currently doesn't allow for one insert call to be split among multiple destinations
        // but for now that is fine
        if (itemStack.isEmpty() || !transporter.hasTransmitterNetwork()) {
            return itemStack;
        }
        long currentTick = currentTickSupplier.getAsLong();
        if (currentTick != lastTick) {
            seenStacks.clear();
            seenExecutedStacks.clear();
            simulatedFlowingStacks.clear();
            lastTick = currentTick;
        }

        int limit = getSlotLimit(slot);
        TransitResponse response;
        if (simulate) {
            if (seenExecutedStacks.contains(itemStack) || !seenStacks.add(itemStack)) {
                //Failed because we have already seen it this tick (tried to add it OR it was one of the remainders we returned that couldn't be inserted further)
                // or if we already actually executed it
                return itemStack;
            }
            TransitRequest request = getRequest(limit, itemStack);
            TransporterStack stack = transporter.createInsertStack(fromPos, transporter.getColor());
            response = stack.recalculatePath(request, transporter, 1, simulatedFlowingStacks);
            if (response.isEmpty()) {
                return itemStack;
            }
            //Just setting the transporter stack's stack is equivalent to LogisticalTransporterBase#updateTransit when simulating
            // as we already know the response is not empty
            stack.itemStack = response.getStack();
            if (stack.getPathType().hasTarget()) {
                //If the stack actually has a path add that simulated insert to a list of locally simulated flowing stacks so that
                // if the mod simulates against the next slot as well we can give a more accurate result
                simulatedFlowingStacks.computeIfAbsent(GlobalPos.of(transporter.getLevel().dimension(), BlockPos.of(stack.getDest())), k -> new ObjectOpenHashSet<>()).add(stack);
            }
        } else {
            if (!seenExecutedStacks.add(itemStack)) {
                //Failed because we have already seen it this tick (tried to add it OR it was one of the remainders we returned that couldn't be inserted further)
                return itemStack;
            }
            //Note: We clear both the seen and simulated stacks if we are actually inserting as technically all simulations become
            // invalid once an insertion has happened so rather than trying to sort of update our simulated flowing stacks, we just
            // nuke it all
            seenStacks.clear();
            simulatedFlowingStacks.clear();

            TransitRequest request = getRequest(limit, itemStack);

            response = transporter.insertUnchecked(fromPos, request, transporter.getColor(), true, 1);
            if (response.isEmpty()) {
                return itemStack;
            }
        }
        ItemStack remainder = response.getRejected();

        if (itemStack.getCount() > limit) {
            //If we used a smaller stack due to our transporter's limit we need to make sure we include the amount we skipped
            // in the remainder
            int extra = itemStack.getCount() - limit;
            if (remainder.isEmpty()) {
                //Everything we tried to fit was accepted. Create a remainder out of the part we skipped
                remainder = itemStack.copyWithCount(extra);
            } else {
                //Note: It is a new stack, so we can safely modify it, so add back the extra that we skipped attempting to insert
                remainder.grow(extra);
            }
        }

        if (!remainder.isEmpty()) {
            //Add what we couldn't insert so if it gets given to us again we can see we already processed it as best as we can
            if (simulate) {
                seenStacks.add(remainder);
            } else {
                seenExecutedStacks.add(remainder);
            }
            //TODO: Should this only be marked as seen if we have a single destination? Because if we have multiple in theory then them inserting
            // the remainder into a later slot may allow it to go to that other destination, and if not then we can mark it as seen then when
            // we try to insert it
        }
        //Return the actual rejected/remainder calculated in the transit response
        return remainder;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return transporter.tier.getPullAmount();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        //Always valid
        return true;
    }
}