package mekanism.common.tile.qio;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
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
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler {

    private static final int MAX_DELAY = 10;
    private int delay = 0;
    private boolean exportWithoutFilter;

    private final EfficientEjector<Object2LongMap.Entry<HashedItem>> filterEjector =
          new EfficientEjector<>(Entry::getKey, e -> MathUtils.clampToInt(e.getLongValue()));
    private final EfficientEjector<Map.Entry<HashedItem, QIOItemTypeData>> filterlessEjector =
          new EfficientEjector<>(Entry::getKey, e -> MathUtils.clampToInt(e.getValue().getCount()));

    public TileEntityQIOExporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_EXPORTER, pos, state);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (MekanismUtils.canFunction(this)) {
            if (delay > 0) {
                delay--;
                return;
            }
            tryEject();
            delay = MAX_DELAY;
        }
    }

    private void tryEject() {
        QIOFrequency freq = getQIOFrequency();
        Direction direction = getDirection();
        BlockEntity back = WorldUtils.getTileEntity(getLevel(), worldPosition.relative(direction.getOpposite()));
        if (freq == null || !InventoryUtils.isItemHandler(back, direction)) {
            return;
        }
        if (!exportWithoutFilter && getFilters().isEmpty()) {
            return;
        }
        if (exportWithoutFilter && getFilters().isEmpty()) {
            filterlessEjector.eject(freq, back, freq.getItemDataMap().entrySet());
        } else if (!getFilters().isEmpty()) {
            filterEjector.eject(freq, back, getFilterEjectMap(back, freq).object2LongEntrySet());
        }
    }

    private Object2LongMap<HashedItem> getFilterEjectMap(BlockEntity back, QIOFrequency freq) {
        Object2LongMap<HashedItem> map = new Object2LongOpenHashMap<>();
        for (QIOFilter<?> filter : getFilters()) {
            if (filter instanceof QIOItemStackFilter itemFilter) {
                if (itemFilter.fuzzyMode) {
                    map.putAll(freq.getStacksByItem(itemFilter.getItemStack().getItem()));
                } else {
                    HashedItem type = HashedItem.create(itemFilter.getItemStack());
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
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = super.getTileDataRemap();
        remap.put(NBTConstants.AUTO, NBTConstants.AUTO);
        return remap;
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private void setExportsWithoutFilter(boolean value) throws ComputerException {
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
     *
     * Abstracting us away from the item map (using the type/count suppliers) allows us to interface directly with the entries of the QIO's item data map when running a
     * filterless ejection, rather than recreating the whole map each ejection operation.
     *
     * Complexity: O(k * s), where 'k' is our max eject attempts constant and 's' is the size of the inventory.
     *
     * @author aidancbrady
     */
    private final class EfficientEjector<T> {

        private static final int MAX_EJECT_ATTEMPTS = 100;

        private final Function<T, HashedItem> typeSupplier;
        private final ToIntFunction<T> countSupplier;

        private EfficientEjector(Function<T, HashedItem> typeSupplier, ToIntFunction<T> countSupplier) {
            this.typeSupplier = typeSupplier;
            this.countSupplier = countSupplier;
        }

        private void eject(QIOFrequency freq, BlockEntity tile, Collection<T> ejectMap) {
            if (ejectMap.isEmpty()) {
                return;
            }
            double ejectChance = Math.min(1, (double) MAX_EJECT_ATTEMPTS / ejectMap.size());
            int maxTypes = getMaxTransitTypes(), maxCount = getMaxTransitCount();
            Object2IntMap<HashedItem> removed = new Object2IntOpenHashMap<>();
            int amountRemoved = 0;

            Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getDirection()).resolve();

            if (capability.isPresent()) {
                IItemHandler inventory = capability.get();
                for (T obj : ejectMap) {
                    // break if we've reached our quota
                    if (amountRemoved == maxCount || removed.size() == maxTypes) {
                        break;
                    }
                    // skip randomly based on our eject chance
                    if (getLevel().getRandom().nextDouble() > ejectChance) {
                        continue;
                    }
                    HashedItem type = typeSupplier.apply(obj);
                    ItemStack origInsert = type.createStack(Math.min(maxCount - amountRemoved, countSupplier.applyAsInt(obj)));
                    ItemStack toInsert = origInsert.copy();
                    for (int i = 0; i < inventory.getSlots(); i++) {
                        // Check validation
                        if (inventory.isItemValid(i, toInsert)) {
                            // Do insert
                            toInsert = inventory.insertItem(i, toInsert, false);
                            // If empty, end
                            if (toInsert.isEmpty()) {
                                break;
                            }
                        }
                    }
                    ItemStack toUse = TransporterManager.getToUse(origInsert, toInsert);
                    if (!toUse.isEmpty()) {
                        amountRemoved += toUse.getCount();
                        removed.put(type, removed.getOrDefault(type, 0) + toUse.getCount());
                    }
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