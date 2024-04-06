package mekanism.common.tile.qio;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOFrequency.QIOItemTypeData;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler {

    private static final int MAX_DELAY = MekanismUtils.TICKS_PER_HALF_SECOND;

    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> backInventory;
    private int delay = 0;
    private boolean exportWithoutFilter;

    private final EfficientEjector<Object2LongMap.Entry<HashedItem>> filterEjector = new EfficientEjector<>(Entry::getKey, e -> MathUtils.clampToInt(e.getLongValue()),
          freq -> getFilterEjectMap(freq).object2LongEntrySet());
    private final EfficientEjector<Map.Entry<HashedItem, QIOItemTypeData>> filterlessEjector =
          new EfficientEjector<>(Entry::getKey, e -> MathUtils.clampToInt(e.getValue().getCount()), freq -> freq.getItemDataMap().entrySet());

    public TileEntityQIOExporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_EXPORTER, pos, state);
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
        //TODO - 1.20.4: Optimize exporting into transporters, maybe by checking if it is a cursed transporter handler??
        if (backHandler == null) {
            return;
        }
        EfficientEjector<?> ejector;
        if (getFilterManager().hasEnabledFilters()) {
            ejector = filterEjector;
        } else if (exportWithoutFilter) {
            ejector = filterlessEjector;
        } else {
            return;
        }
        ejector.eject(freq, backHandler);
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
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        super.writeSustainedData(dataMap);
        dataMap.putBoolean(NBTConstants.AUTO, exportWithoutFilter);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        super.readSustainedData(dataMap);
        NBTUtils.setBooleanIfPresent(dataMap, NBTConstants.AUTO, value -> exportWithoutFilter = value);
    }

    @Override
    public Map<String, Holder<AttachmentType<?>>> getTileDataAttachmentRemap() {
        Map<String, Holder<AttachmentType<?>>> remap = super.getTileDataAttachmentRemap();
        remap.put(NBTConstants.AUTO, MekanismAttachmentTypes.AUTO);
        return remap;
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        stack.setData(MekanismAttachmentTypes.AUTO, exportWithoutFilter);
    }

    @Override
    public void readFromStack(ItemStack stack) {
        super.readFromStack(stack);
        exportWithoutFilter = stack.getData(MekanismAttachmentTypes.AUTO);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void setExportsWithoutFilter(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (exportWithoutFilter != value) {
            toggleExportWithoutFilter();
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
    private final class EfficientEjector<T> {

        private static final double MAX_EJECT_ATTEMPTS = 100;

        private final Function<QIOFrequency, Collection<T>> ejectMapCalculator;
        private final Function<T, HashedItem> typeSupplier;
        private final ToIntFunction<T> countSupplier;

        private EfficientEjector(Function<T, HashedItem> typeSupplier, ToIntFunction<T> countSupplier, Function<QIOFrequency, Collection<T>> ejectMapCalculator) {
            this.typeSupplier = typeSupplier;
            this.countSupplier = countSupplier;
            this.ejectMapCalculator = ejectMapCalculator;
        }

        private void eject(QIOFrequency freq, IItemHandler inventory) {
            int slots = inventory.getSlots();
            if (slots == 0) {
                //If the inventory has no slots just exit early and don't even bother calculating the eject map
                return;
            }
            Collection<T> ejectMap = ejectMapCalculator.apply(freq);
            if (ejectMap.isEmpty()) {
                return;
            }
            RandomSource random = getLevel().getRandom();
            double ejectChance = Math.min(1, MAX_EJECT_ATTEMPTS / ejectMap.size());
            int maxTypes = getMaxTransitTypes(), maxCount = getMaxTransitCount();
            Object2IntMap<HashedItem> removed = new Object2IntOpenHashMap<>();
            int amountRemoved = 0;
            for (T obj : ejectMap) {
                // break if we've reached our quota
                if (amountRemoved == maxCount || removed.size() == maxTypes) {
                    break;
                }
                // skip randomly based on our eject chance
                if (random.nextDouble() > ejectChance) {
                    continue;
                }
                HashedItem type = typeSupplier.apply(obj);
                ItemStack origInsert = type.createStack(Math.min(maxCount - amountRemoved, countSupplier.applyAsInt(obj)));
                ItemStack toInsert = origInsert.copy();
                for (int i = 0; i < slots; i++) {
                    // Do insert, this will handle validating the item is valid for the inventory
                    toInsert = inventory.insertItem(i, toInsert, false);
                    // If empty, end
                    if (toInsert.isEmpty()) {
                        break;
                    }
                }
                ItemStack toUse = TransporterManager.getToUse(origInsert, toInsert);
                if (!toUse.isEmpty()) {
                    amountRemoved += toUse.getCount();
                    removed.mergeInt(type, toUse.getCount(), Integer::sum);
                }
            }
            // actually remove the items from the QIO frequency
            for (Object2IntMap.Entry<HashedItem> entry : removed.object2IntEntrySet()) {
                int amount = entry.getIntValue();
                ItemStack ret = freq.removeByType(entry.getKey(), amount);
                if (ret.getCount() != amount) {
                    Mekanism.logger.error("QIO ejection item removal didn't line up with prediction: removed {}, expected {}", ret.getCount(), amount);
                }
            }
        }
    }
}
