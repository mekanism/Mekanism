package mekanism.common.capabilities.item;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@NothingNullByDefault
public class TransporterItemHandler implements ResourceHandler<ItemResource> {

    private final LogisticalTransporterBase transporter;
    private final long fromPos;

    public TransporterItemHandler(LogisticalTransporterBase transporter, long fromPos) {
        this.transporter = transporter;
        this.fromPos = fromPos;
    }

    @Override
    public int size() {
        //Note: With transactions being able to be used for accurate "simulations" of if an inventory can accept all the contents,
        // we no longer require pretending that we have more slots than we actually have as mods like RS no need to do such heuristics
        return 1;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return 0;
    }

    public LogisticalTransporterBase getTransporter() {
        return transporter;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !transporter.hasTransmitterNetwork()) {
            return 0;
        }
        amount = Math.min(amount, getCapacityAsInt(0, resource));
        TransitRequest request = TransitRequest.simple(resource, amount);
        TransporterStack stack = transporter.createInsertStack(fromPos, transporter.getColor());
        //TODO - 26.1: Is there any other validation that we need to do?
        TransitResponse response = transporter.insert(null, request, stack, 1, transaction, TransporterStack::recalculatePath);
        return response.sendingAmount();
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
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
        Objects.checkIndex(index, size());
        return transporter.tier.getPullAmount();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmpty(resource);
        //Always valid
        return true;
    }
}