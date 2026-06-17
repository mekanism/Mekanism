package mekanism.common.capabilities.item;

import java.util.Objects;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TransporterItemHandler implements ResourceHandler<ItemResource> {

    private final LogisticalTransporterBase transporter;
    private final long fromPos;

    TransporterItemHandler(LogisticalTransporterBase transporter, long fromPos) {
        this.transporter = transporter;
        this.fromPos = fromPos;
    }

    public LogisticalTransporterBase getTransporter() {
        return transporter;
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

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return transporter.tier.getPullAmount();
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
        Level level = transporter.getLevel();
        if (level == null) {
            return 0;
        }
        //Note: We skip checking Transmitter#canConnectMutual, as transporters should never end up using this path,
        // so then it would just fall back to having checked Transmitter#canConnect, which is already covered by
        // the check to LogisticalTransporterBase#exposesInsertCap in TransporterCapabilityResolver
        return transporter.insertUnchecked(level, fromPos, resource, Math.min(amount, transporter.tier.getPullAmount()), transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        return extract(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmpty(resource);
        //Always valid
        return true;
    }
}