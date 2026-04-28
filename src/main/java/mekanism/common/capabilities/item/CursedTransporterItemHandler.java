package mekanism.common.capabilities.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@NothingNullByDefault
public class CursedTransporterItemHandler implements ResourceHandler<ItemResource> {

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
    public int size() {
        //Pretend we have nine slots as we will short circuit if repeat calls are made, and some mods validate the total number
        // of slots before sending things (for example refined storage's crafters)
        return 9;
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return 0;
    }

    public LogisticalTransporterBase getTransporter() {
        return transporter;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {//TODO - 26.1: Re-evaluate this entire method
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        //Note: This method currently doesn't allow for one insert call to be split among multiple destinations
        // but for now that is fine
        if (amount == 0 || !transporter.hasTransmitterNetwork()) {
            return 0;
        }
        /*long currentTick = currentTickSupplier.getAsLong();
        if (currentTick != lastTick) {
            seenStacks.clear();
            seenExecutedStacks.clear();
            simulatedFlowingStacks.clear();
            lastTick = currentTick;
        }*/
        //TODO - 26.1: Implement support for transactions with how items transfer
        return 0;
        /*amount = Math.min(amount, getCapacityAsInt(index, resource));
        TransitResponse response;
        if (simulate) {
            if (seenExecutedStacks.contains(itemStack) || !seenStacks.add(itemStack)) {
                //Failed because we have already seen it this tick (tried to add it OR it was one of the remainders we returned that couldn't be inserted further)
                // or if we already actually executed it
                return 0;
            }
            TransitRequest request = TransitRequest.simple(resource.toStack(amount));
            TransporterStack stack = transporter.createInsertStack(fromPos, transporter.getColor());
            response = stack.recalculatePath(request, transporter, 1, simulatedFlowingStacks);
            if (response.isEmpty()) {
                return 0;
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
                return 0;
            }
            //Note: We clear both the seen and simulated stacks if we are actually inserting as technically all simulations become
            // invalid once an insertion has happened so rather than trying to sort of update our simulated flowing stacks, we just
            // nuke it all
            seenStacks.clear();
            simulatedFlowingStacks.clear();

            TransitRequest request = TransitRequest.simple(resource.toStack(amount));

            response = transporter.insertUnchecked(fromPos, request, transporter.getColor(), true, 1);
            if (response.isEmpty()) {
                return 0;
            }
        }
        //Return the actual accepted amount calculated in the transit response
        return response.getSendingAmount();*/
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        //TODO - 26.1: Implement a more efficient variant of this as the slots only really exist for faking things
        return ResourceHandler.super.insert(resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return transporter.tier.getPullAmount();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        //Always valid
        return true;
    }
}