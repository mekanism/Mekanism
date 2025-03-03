package mekanism.common.tile.qio;

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
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.RelativeSide;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase.PathCalculator;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOFrequency.QIOItemTypeData;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.ItemData;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler implements IAdvancedTransportEjector {

    private static final EfficientEjector<Object2LongMap.Entry<HashedItem>> FILTER_EJECTOR = new EfficientEjector<>(Entry::getKey, e -> MathUtils.clampToInt(e.getLongValue()),
          (exporter, freq) -> exporter.getFilterEjectMap(freq).object2LongEntrySet());
    private static final EfficientEjector<Map.Entry<HashedItem, QIOItemTypeData>> FILTERLESS_EJECTOR =
          new EfficientEjector<>(Entry::getKey, e -> MathUtils.clampToInt(e.getValue().getCount()), (exporter, freq) -> freq.getItemDataMap().entrySet());
    private static final int MAX_DELAY = MekanismUtils.TICKS_PER_HALF_SECOND;

    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> backInventory;
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
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        //TODO - 1.20.4: Re-evaluate the internal inventory slot and why do we even have a slot on the exporter
        // I think it is so that transporters can connect, but it seems a bit silly
        builder.addSlot(InternalInventorySlot.create(listener), RelativeSide.BACK);
        return builder.build();
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
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
        IItemHandler backHandler = backInventory.getCapability();
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

    private Object2LongMap<HashedItem> getFilterEjectMap(QIOFrequency freq) {
        Object2LongMap<HashedItem> map = new Object2LongOpenHashMap<>();
        for (QIOFilter<?> filter : getFilterManager().getEnabledFilters()) {
            if (filter instanceof QIOItemStackFilter itemFilter) {
                if (itemFilter.fuzzyMode) {
                    map.putAll(freq.getStacksByItem(itemFilter.getItemStack().getItem()));
                } else {
                    HashedItem type = HashedItem.create(itemFilter.getItemStack());
                    map.put(type, freq.getStoredByHash(type));
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
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        SidedBlockPos rrTarget = getRoundRobinTarget();
        if (rrTarget != null) {
            nbtTags.put(SerializationConstants.ROUND_ROBIN_TARGET, rrTarget.serialize());
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        if (nbt.contains(SerializationConstants.ROUND_ROBIN_TARGET, Tag.TAG_COMPOUND)) {
            setRoundRobinTarget(SidedBlockPos.deserialize(nbt.getCompound(SerializationConstants.ROUND_ROBIN_TARGET)));
        }
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(SerializationConstants.ROUND_ROBIN_TARGET);
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.writeSustainedData(provider, dataMap);
        dataMap.putBoolean(SerializationConstants.AUTO, exportWithoutFilter);
        dataMap.putBoolean(SerializationConstants.ROUND_ROBIN, roundRobin);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag dataMap) {
        super.readSustainedData(provider, dataMap);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.AUTO, value -> exportWithoutFilter = value);
        roundRobin = dataMap.getBoolean(SerializationConstants.ROUND_ROBIN);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.AUTO, exportWithoutFilter);
        builder.set(MekanismDataComponents.ROUND_ROBIN, roundRobin);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
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
    public boolean canSendHome(@NotNull ItemStack stack) {
        QIOFrequency frequency = getQIOFrequency();
        return frequency != null && frequency.massInsert(stack, stack.getCount(), Action.SIMULATE) > 0;
    }

    @NotNull
    @Override
    public TransitRequest.TransitResponse sendHome(@NotNull TransitRequest request) {
        if (request.isEmpty()) {//Short circuit if our request is empty
            return request.getEmptyResponse();
        }
        QIOFrequency frequency = getQIOFrequency();
        if (frequency != null) {
            for (ItemData data : request) {
                ItemStack origInsert = StackUtils.size(data.getStack(), data.getTotalCount());
                ItemStack remainder = frequency.addItem(origInsert);
                if (TransporterManager.didEmit(origInsert, remainder)) {
                    return request.createResponse(TransporterManager.getToUse(origInsert, remainder), data);
                }
            }
        }
        return request.getEmptyResponse();
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
    private record EfficientEjector<T>(Function<T, HashedItem> typeSupplier, ToIntFunction<T> countSupplier,
                                       BiFunction<TileEntityQIOExporter, QIOFrequency, Collection<T>> ejectMapCalculator) {

        private static final double MAX_EJECT_ATTEMPTS = 100;

        private void eject(TileEntityQIOExporter exporter, QIOFrequency freq, IItemHandler inventory) {
            int slots = inventory.getSlots();
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
            if (inventory instanceof CursedTransporterItemHandler cursed) {
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
            Object2IntMap<HashedItem> removed = new Object2IntOpenHashMap<>();
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
                HashedItem type = typeSupplier.apply(obj);
                int amountToInsert = Math.min(maxCount - amountRemoved, countSupplier.applyAsInt(obj));
                ItemStack origInsert = type.createStack(amountToInsert);
                int toUse;
                if (transporter == null) {
                    ItemStack toInsert = origInsert.copy();
                    for (int i = 0; i < slots; i++) {
                        // Do insert, this will handle validating the item is valid for the inventory
                        toInsert = inventory.insertItem(i, toInsert, false);
                        // If empty, end
                        if (toInsert.isEmpty()) {
                            break;
                        }
                    }
                    toUse = TransporterManager.getToUse(origInsert, toInsert).getCount();
                } else {
                    //Note: We just simplify the logic that we would have when sending to a transporter via the handler
                    // and add support for also performing round-robin distribution. We don't just use a custom transit request
                    // as we want to be able to send multiple types at once, which is not that straightforward to do when trying
                    // to re-use where we currently are in the iteration. Without that extra handling we can easily do a custom
                    // transit request similar to https://gist.github.com/pupnewfster/d0dac2098a2755dc60220f89873ff461,
                    // but it means we may not properly respect the maxTypes and maxCount
                    TransitRequest request = TransitRequest.simple(origInsert);
                    //TODO: Technically if we still have more of the same item input, we want to allow trying to insert it into different transport
                    // destinations, which this doesn't do as it only checks once, rather than trying to check again if we still have some that we
                    // are able to insert
                    //Note: We don't use transporter#insertMaybeRR so that we only have to validate the transporter once
                    TransitResponse response = transporter.insertUnchecked(exporter, request, transporter.getColor(), true, 1, pathCalculator);
                    toUse = response.getSendingAmount();
                }
                if (toUse > 0) {
                    amountRemoved += toUse;
                    removed.mergeInt(type, toUse, Integer::sum);
                }
            }
            // actually remove the items from the QIO frequency
            for (ObjectIterator<Object2IntMap.Entry<HashedItem>> iterator = Object2IntMaps.fastIterator(removed); iterator.hasNext(); ) {
                Object2IntMap.Entry<HashedItem> entry = iterator.next();
                int amount = entry.getIntValue();
                ItemStack ret = freq.removeByType(entry.getKey(), amount);
                if (ret.getCount() != amount) {
                    Mekanism.logger.error("QIO ejection item removal didn't line up with prediction: removed {}, expected {}", ret.getCount(), amount);
                }
            }
        }
    }
}
