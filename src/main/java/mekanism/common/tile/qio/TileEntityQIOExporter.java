package mekanism.common.tile.qio;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.item.TransporterItemHandler;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler implements IAdvancedTransportEjector {

    private static final EfficientEjector<Object2LongMap.Entry<ItemResource>> FILTER_EJECTOR = new EfficientEjector<>(Entry::getKey, Object2LongMap.Entry::getLongValue,
          (exporter, freq) -> exporter.getFilterEjectMap(freq).object2LongEntrySet());
    private static final EfficientEjector<Map.Entry<ItemResource, QIOItemTypeData>> FILTERLESS_EJECTOR = new EfficientEjector<>(Entry::getKey,
          e -> e.getValue().getCount(), (_, freq) -> freq.getItemDataMap().entrySet());
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
    protected boolean onUpdateServer(ServerLevel level, @Nullable QIOFrequency frequency) {
        boolean needsUpdate = super.onUpdateServer(level, frequency);
        if (frequency != null && canFunction()) {
            if (delay > 0) {
                delay--;
            } else {
                tryEject(level, frequency);
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

    private void tryEject(ServerLevel level, QIOFrequency freq) {
        if (backInventory == null) {
            Direction direction = getDirection();
            backInventory = Capabilities.ITEM.createCache(level, worldPosition.relative(direction.getOpposite()), direction);
        }
        ResourceHandler<ItemResource> backHandler = backInventory.getCapability();
        if (backHandler != null) {
            if (getFilterManager().hasEnabledFilters()) {
                FILTER_EJECTOR.eject(this, level, freq, backHandler, level.getRandom());
            } else if (exportWithoutFilter) {
                FILTERLESS_EJECTOR.eject(this, level, freq, backHandler, level.getRandom());
            }
        }
    }

    private Object2LongMap<ItemResource> getFilterEjectMap(QIOFrequency freq) {
        Object2LongMap<ItemResource> map = new Object2LongOpenHashMap<>();
        for (QIOFilter<?> filter : getFilterManager().getEnabledFilters()) {
            if (filter instanceof QIOItemStackFilter itemFilter) {
                ItemResource type = itemFilter.getItemType();
                if (itemFilter.fuzzyMode) {
                    map.putAll(freq.getStacksByItem(type.getItem()));
                } else {
                    map.put(type, freq.getStored(type));
                }
            } else if (filter instanceof QIOTagFilter tagFilter) {
                map.putAll(freq.getStacksByTagWildcard(tagFilter.getTagName()));
            } else if (filter instanceof QIOModIDFilter modIDFilter) {
                map.putAll(freq.getStacksByModIDWildcard(modIDFilter.getModID()));
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
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(SerializationConstants.ROUND_ROBIN_TARGET, SidedBlockPos.CODEC, getRoundRobinTarget());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read(SerializationConstants.ROUND_ROBIN_TARGET, SidedBlockPos.CODEC).ifPresent(this::setRoundRobinTarget);
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(SerializationConstants.ROUND_ROBIN_TARGET);
    }

    @Override
    public void writeSustainedData(ValueOutput output) {
        super.writeSustainedData(output);
        output.putBoolean(SerializationConstants.AUTO, exportWithoutFilter);
        output.putBoolean(SerializationConstants.ROUND_ROBIN, roundRobin);
    }

    @Override
    public void readSustainedData(ValueInput input) {
        super.readSustainedData(input);
        exportWithoutFilter = input.getBooleanOr(SerializationConstants.AUTO, exportWithoutFilter);
        //TODO - 26.2: Should the default value be the current round robin value?
        roundRobin = input.getBooleanOr(SerializationConstants.ROUND_ROBIN, false);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.AUTO, exportWithoutFilter);
        builder.set(MekanismDataComponents.ROUND_ROBIN, roundRobin);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
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
    public boolean canSendHome(Level level, ItemResource itemType, int amount, @Nullable TransactionContext transaction) {
        QIOFrequency frequency = getQIOFrequency();
        if (frequency == null) {
            return false;
        }
        try (Transaction simulation = Transaction.open(transaction)) {
            return frequency.massInsert(itemType, amount, simulation) > 0;
        }
    }

    @Override
    public TransitRequest.TransitResponse sendHome(Level level, TransitRequest request, TransactionContext transaction) {
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

    /// An efficient way to handle large (in item type) item ejections from a QIO frequency. Each eject attempt of a certain item type will use a uniform probability
    /// distribution based on a predetermined 'max eject attempt' constant to see if the ejection should take place. This makes sure we will eventually eject each item
    /// type, but not attempt every item in the frequency each operation.
    ///
    /// Abstracting us away from the item map (using the type/count suppliers) allows us to interface directly with the entries of the QIO's item data map when running a
    /// filterless ejection, rather than recreating the whole map each ejection operation.
    ///
    /// Complexity: `O(k * s)`, where `k` is our max eject attempts constant and `s` is the size of the inventory.
    private record EfficientEjector<T>(Function<T, ItemResource> typeSupplier, ToLongFunction<T> countSupplier,
                                       BiFunction<TileEntityQIOExporter, QIOFrequency, Collection<T>> ejectMapCalculator) {

        private static final double MAX_EJECT_ATTEMPTS = 100;

        private void eject(TileEntityQIOExporter exporter, ServerLevel level, QIOFrequency freq, ResourceHandler<ItemResource> inventory, RandomSource random) {
            int slots = inventory.size();
            if (slots == 0) {
                //If the inventory has no slots just exit early and don't even bother calculating the eject map
                return;
            }
            LogisticalTransporterBase transporter = null;
            PathCalculator<TileEntityQIOExporter> pathCalculator = null;
            //Note: on the off chance we are ejecting to a transporter that has no network, we delay calculating the eject map until after we have validated it has a network
            if (inventory instanceof TransporterItemHandler cursed) {
                transporter = cursed.getTransporter();
                if (!transporter.hasTransmitterNetwork()) {//Probably will never happen, but if we don't have a network just skip doing anything
                    return;
                }
                //Note: We don't have to validate if the transporter can accept items from us, as if it can't then the cap wouldn't be exposed to us
                pathCalculator = exporter.getRoundRobin() ? TransporterStack::recalculateRRPath : TransporterStack::recalculatePath;
            }
            Collection<T> ejectMap = ejectMapCalculator.apply(exporter, freq);
            if (ejectMap.isEmpty()) {
                return;
            }
            double ejectChance = Math.min(1, MAX_EJECT_ATTEMPTS / ejectMap.size());
            boolean randomizeEject = ejectChance < 1;
            int maxTypes = exporter.getMaxTransitTypes();
            int maxCount = exporter.getMaxTransitCount();
            Set<ItemResource> removedTypes = new HashSet<>();
            int amountRemoved = 0;
            for (T obj : ejectMap) {
                // break if we've reached our quota
                if (amountRemoved == maxCount || removedTypes.size() == maxTypes) {
                    break;
                } else if (randomizeEject && random.nextDouble() > ejectChance) {
                    // skip randomly based on our eject chance
                    continue;
                }
                try (Transaction transaction = Transaction.openRoot()) {
                    ItemResource type = typeSupplier.apply(obj);
                    int amountToInsert = Math.min(maxCount - amountRemoved, Ints.saturatedCast(countSupplier.applyAsLong(obj)));
                    int toUse;
                    if (transporter == null) {
                        //Insert the item into the resource handler, allowing the handler to decide how it is split among slots
                        toUse = inventory.insert(type, amountToInsert, transaction);
                    } else {
                        //Note: We don't use transporter#insert as we already know the transporter is valid due to it having exposed a capability
                        // We also can't just use the transporter's handler as we want to support round-robin
                        toUse = transporter.insertUnchecked(level, exporter, type, amountToInsert, transaction, pathCalculator);
                    }
                    //Try to remove the item from the frequency
                    if (toUse > 0 && freq.removeByType(type, toUse, transaction) == toUse) {
                        //If we were able to remove it all from the frequency (which theoretically should work as we started with the amount stored,
                        // and don't have extraction rate limits): increase the counter of how much we have removed so far, and mark it as a removed type.
                        // We also then commit the removal and sending so that the changes persist
                        amountRemoved += toUse;
                        removedTypes.add(type);
                        transaction.commit();
                    }
                }
            }
        }
    }
}
