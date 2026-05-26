package mekanism.common.tile.qio;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.IContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.capabilities.holder.MekContainerHelper;
import mekanism.common.capabilities.item.TransporterItemHandler;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase.PathCalculator;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOFrequency.QIOItemTypeData;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.ItemData;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler implements IAdvancedTransportEjector {

    private static final EfficientEjector<Object2LongMap.Entry<ItemResource>> FILTER_EJECTOR = new EfficientEjector<>(Entry::getKey, e -> Ints.saturatedCast(e.getLongValue()),
          (exporter, freq) -> exporter.getFilterEjectMap(freq).object2LongEntrySet());
    private static final EfficientEjector<Map.Entry<ItemResource, QIOItemTypeData>> FILTERLESS_EJECTOR =
          new EfficientEjector<>(Entry::getKey, e -> Ints.saturatedCast(e.getValue().getCount()), (exporter, freq) -> freq.getItemDataMap().entrySet());
    private static final int MAX_DELAY = MekanismUtils.TICKS_PER_HALF_SECOND;

    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction> backInventory;
    private int delay = 0;
    private boolean exportWithoutFilter;
    private boolean roundRobin;
    @Nullable
    private SidedBlockPos rrTarget;

    public TileEntityQIOExporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_EXPORTER, pos, state);
    }

    @NotNull
    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        //TODO - 1.20.4: Re-evaluate the internal inventory slot and why do we even have a slot on the exporter
        // I think it is so that transporters can connect, but it seems a bit silly
        builder.addContainer(InternalInventorySlot.create(listener), RelativeSide.BACK);
        return builder.build();
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        //Note: We don't persist items because the slot we have is only actually for the transporters to connect visually
        return type != ContainerType.ITEM && super.persists(type);
    }

    @Override
    protected boolean onUpdateServer(@Nullable QIOFrequency frequency) {
        boolean needsUpdate = super.onUpdateServer(frequency);
        if (frequency != null && canFunction()) {
            if (delay > 0) {
                delay--;
            } else {
                tryEject(frequency);
                delay = MAX_DELAY;
            }
        }
        return needsUpdate;
    }

    @Override
    protected void invalidateDirectionCaches(Direction newDirection) {
        super.invalidateDirectionCaches(newDirection);
        backInventory = null;
    }

    private void tryEject(QIOFrequency freq) {
        if (backInventory == null) {
            Direction direction = getDirection();
            backInventory = Capabilities.ITEM.createCache((ServerLevel) level, worldPosition.relative(direction.getOpposite()), direction);
        }
        ResourceHandler<ItemResource> backHandler = backInventory.getCapability();
        if (backHandler == null) {
            return;
        }
        EfficientEjector<?> ejector;
        if (getFilterManager().hasEnabledFilters()) {
            ejector = FILTER_EJECTOR;
        } else if (exportWithoutFilter) {
            ejector = FILTERLESS_EJECTOR;
        } else {
            return;
        }
        ejector.eject(this, freq, backHandler);
    }

    private Object2LongMap<ItemResource> getFilterEjectMap(QIOFrequency freq) {
        Object2LongMap<ItemResource> map = new Object2LongOpenHashMap<>();
        for (QIOFilter<?> filter : getFilterManager().getEnabledFilters()) {
            if (filter instanceof QIOItemStackFilter itemFilter) {
                if (itemFilter.fuzzyMode) {
                    map.putAll(freq.getStacksByItem(itemFilter.getItemType().getItem()));
                } else {
                    ItemResource type = itemFilter.getItemType();
                    map.put(type, freq.getStored(type));
                }
            } else if (filter instanceof QIOTagFilter tagFilter) {
                String tagName = tagFilter.getTagName();
                map.putAll(freq.getStacksByTagWildcard(tagName));
            } else if (filter instanceof QIOModIDFilter modIDFilter) {
                String modID = modIDFilter.getModID();
                map.putAll(freq.getStacksByModIDWildcard(modID));
            }
        }
        return map;
    }

    @ComputerMethod
    public boolean getExportWithoutFilter() {
        return exportWithoutFilter;
    }

    public void toggleExportWithoutFilter() {
        exportWithoutFilter = !exportWithoutFilter;
        markForSave();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getExportWithoutFilter, value -> exportWithoutFilter = value));
        container.track(SyncableBoolean.create(this::getRoundRobin, value -> roundRobin = value));
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(SerializationConstants.ROUND_ROBIN_TARGET, SidedBlockPos.CODEC, getRoundRobinTarget());
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        input.read(SerializationConstants.ROUND_ROBIN_TARGET, SidedBlockPos.CODEC).ifPresent(this::setRoundRobinTarget);
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(SerializationConstants.ROUND_ROBIN_TARGET);
    }

    @Override
    public void writeSustainedData(@NotNull ValueOutput output) {
        super.writeSustainedData(output);
        output.putBoolean(SerializationConstants.AUTO, exportWithoutFilter);
        output.putBoolean(SerializationConstants.ROUND_ROBIN, roundRobin);
    }

    @Override
    public void readSustainedData(@NotNull ValueInput input) {
        super.readSustainedData(input);
        exportWithoutFilter = input.getBooleanOr(SerializationConstants.AUTO, exportWithoutFilter);
        //TODO - 26.1: Should the default value be the current round robin value?
        roundRobin = input.getBooleanOr(SerializationConstants.ROUND_ROBIN, false);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.AUTO, exportWithoutFilter);
        builder.set(MekanismDataComponents.ROUND_ROBIN, roundRobin);
    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentGetter input) {
        super.applyImplicitComponents(input);
        exportWithoutFilter = input.getOrDefault(MekanismDataComponents.AUTO, exportWithoutFilter);
        roundRobin = input.getOrDefault(MekanismDataComponents.ROUND_ROBIN, roundRobin);
    }

    @Nullable
    @Override
    public SidedBlockPos getRoundRobinTarget() {
        return rrTarget;
    }

    @Override
    public void setRoundRobinTarget(@Nullable SidedBlockPos target) {
        rrTarget = target;
    }

    @Override
    @ComputerMethod(nameOverride = "isRoundRobin")
    public boolean getRoundRobin() {
        return roundRobin;
    }

    @Override
    public void toggleRoundRobin() {
        roundRobin = !roundRobin;
        setRoundRobinTarget((SidedBlockPos) null);
        markForSave();
    }

    @Override
    public boolean canSendHome(@NotNull ItemResource itemType, int amount, @Nullable TransactionContext transaction) {
        QIOFrequency frequency = getQIOFrequency();
        if (frequency == null) {
            return false;
        }
        try (Transaction simulation = Transaction.open(transaction)) {
            return frequency.massInsert(itemType, amount, simulation) > 0;
        }
    }

    @NotNull
    @Override
    public TransitRequest.TransitResponse sendHome(@NotNull TransitRequest request, @NotNull TransactionContext transaction) {
        if (request.isEmpty()) {//Short circuit if our request is empty
            return TransitResponse.EMPTY;
        }
        QIOFrequency frequency = getQIOFrequency();
        if (frequency != null) {
            for (ItemData data : request) {
                int count = data.getTotalCount();
                if (count > 0) {
                    ItemResource itemType = data.getItemType();
                    int inserted = frequency.addItem(itemType, count, transaction);
                    if (inserted > 0) {
                        return request.createResponse(itemType, inserted, data);
                    }
                }
            }
        }
        return TransitResponse.EMPTY;
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void setExportsWithoutFilter(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (exportWithoutFilter != value) {
            toggleExportWithoutFilter();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setRoundRobin(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (roundRobin != value) {
            toggleRoundRobin();
        }
    }
    //End methods IComputerTile

    /**
     * An efficient way to handle large (in item type) item ejections from a QIO frequency. Each eject attempt of a certain item type will use a uniform probability
     * distribution based on a predetermined 'max eject attempt' constant to see if the ejection should take place. This makes sure we will eventually eject each item
     * type, but not attempt every item in the frequency each operation.
     * <p>
     * Abstracting us away from the item map (using the type/count suppliers) allows us to interface directly with the entries of the QIO's item data map when running a
     * filterless ejection, rather than recreating the whole map each ejection operation.
     * <p>
     * Complexity: O(k * s), where 'k' is our max eject attempts constant and 's' is the size of the inventory.
     *
     * @author aidancbrady
     */
    private record EfficientEjector<T>(Function<T, ItemResource> typeSupplier, ToIntFunction<T> countSupplier,
                                       BiFunction<TileEntityQIOExporter, QIOFrequency, Collection<T>> ejectMapCalculator) {

        private static final double MAX_EJECT_ATTEMPTS = 100;

        private void eject(TileEntityQIOExporter exporter, QIOFrequency freq, ResourceHandler<ItemResource> inventory) {
            int slots = inventory.size();
            if (slots == 0) {
                //If the inventory has no slots just exit early and don't even bother calculating the eject map
                return;
            }
            Collection<T> ejectMap = ejectMapCalculator.apply(exporter, freq);
            if (ejectMap.isEmpty()) {
                return;
            }
            LogisticalTransporterBase transporter = null;
            PathCalculator<TileEntityQIOExporter> pathCalculator = null;
            if (inventory instanceof TransporterItemHandler cursed) {
                transporter = cursed.getTransporter();
                if (!transporter.hasTransmitterNetwork()) {//Probably will never happen, but if we don't have a network just skip doing anything
                    return;
                }
                Direction from = exporter.getDirection();
                if (!transporter.canReceiveFrom(from) || !transporter.canConnectMutual(from, exporter)) {
                    //Skip if the transporter can't receive from this position or connect to it
                    return;
                }
                pathCalculator = exporter.getRoundRobin() ? TransporterStack::recalculateRRPath : TransporterStack::recalculatePath;
            }
            RandomSource random = exporter.getLevel().getRandom();
            double ejectChance = Math.min(1, MAX_EJECT_ATTEMPTS / ejectMap.size());
            boolean randomizeEject = ejectChance < 1;
            int maxTypes = exporter.getMaxTransitTypes(), maxCount = exporter.getMaxTransitCount();
            Object2IntMap<ItemResource> removed = new Object2IntOpenHashMap<>();
            int amountRemoved = 0;
            for (T obj : ejectMap) {
                // break if we've reached our quota
                if (amountRemoved == maxCount || removed.size() == maxTypes) {
                    break;
                }
                // skip randomly based on our eject chance
                if (randomizeEject && random.nextDouble() > ejectChance) {
                    continue;
                }
                ItemResource type = typeSupplier.apply(obj);
                int amountToInsert = Math.min(maxCount - amountRemoved, countSupplier.applyAsInt(obj));
                //TODO - 26.1: Validate that the type can't somehow be empty
                int toUse;
                try (Transaction transaction = Transaction.openRoot()) {//TODO - 26.1: Check callers and see if any are already in a transaction context
                    if (transporter == null) {
                        //Insert the item into the resource handler, allowing the handler to decide how it is split among slots
                        toUse = inventory.insert(type, amountToInsert, transaction);
                    } else {
                        //Note: We just simplify the logic that we would have when sending to a transporter via the handler
                        // and add support for also performing round-robin distribution. We don't just use a custom transit request
                        // as we want to be able to send multiple types at once, which is not that straightforward to do when trying
                        // to re-use where we currently are in the iteration. Without that extra handling we can easily do a custom
                        // transit request similar to https://gist.github.com/pupnewfster/d0dac2098a2755dc60220f89873ff461,
                        // but it means we may not properly respect the maxTypes and maxCount
                        TransitRequest request = TransitRequest.simple(type, amountToInsert);
                        //TODO: Technically if we still have more of the same item input, we want to allow trying to insert it into different transport
                        // destinations, which this doesn't do as it only checks once, rather than trying to check again if we still have some that we
                        // are able to insert
                        //Note: We don't use transporter#insertMaybeRR so that we only have to validate the transporter once
                        TransitResponse response = transporter.insertUnchecked(exporter, request, transporter.getColor(), 1, transaction, pathCalculator);
                        toUse = response.sendingAmount();
                    }
                    transaction.commit();
                }
                if (toUse > 0) {
                    amountRemoved += toUse;
                    removed.mergeInt(type, toUse, Integer::sum);
                }
            }
            // actually remove the items from the QIO frequency
            try (Transaction transaction = Transaction.openRoot()) {
                for (ObjectIterator<Object2IntMap.Entry<ItemResource>> iterator = Object2IntMaps.fastIterator(removed); iterator.hasNext(); ) {
                    Object2IntMap.Entry<ItemResource> entry = iterator.next();
                    int amount = entry.getIntValue();
                    int ret = freq.removeByType(entry.getKey(), amount, transaction);
                    if (ret != amount) {//TODO - 26.1: Can we roll back a transaction instead of just logging an error
                        Mekanism.logger.error("QIO ejection item removal didn't line up with prediction: removed {}, expected {}", ret, amount);
                    }
                }
                transaction.commit();
            }
        }
    }
}
