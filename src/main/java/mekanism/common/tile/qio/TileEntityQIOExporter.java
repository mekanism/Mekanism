package mekanism.common.tile.qio;

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
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOFrequency.QIOItemTypeData;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler {

    private static final int MAX_DELAY = 10;
    private int delay = 0;
    private boolean exportWithoutFilter;

    private final EfficientEjector<Object2LongMap.Entry<HashedItem>> filterEjector =
          new EfficientEjector<>(Entry::getKey, e -> (int) Math.min(Integer.MAX_VALUE, e.getLongValue()));
    private final EfficientEjector<Map.Entry<HashedItem, QIOItemTypeData>> filterlessEjector =
          new EfficientEjector<>(Entry::getKey, e -> (int) Math.min(Integer.MAX_VALUE, e.getValue().getCount()));

    public TileEntityQIOExporter() {
        super(MekanismBlocks.QIO_EXPORTER);
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

        if (world.getGameTime() % 10 == 0) {
            QIOFrequency frequency = getQIOFrequency();
            setActive(frequency != null);
        }
    }

    private void tryEject() {
        QIOFrequency freq = getQIOFrequency();
        TileEntity back = MekanismUtils.getTileEntity(getWorld(), pos.offset(getOppositeDirection()));
        if (freq == null || !InventoryUtils.isItemHandler(back, getDirection())) {
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

    private Object2LongMap<HashedItem> getFilterEjectMap(TileEntity back, QIOFrequency freq) {
        Object2LongMap<HashedItem> map = new Object2LongOpenHashMap<>();
        for (QIOFilter<?> filter : getFilters()) {
            if (filter instanceof QIOItemStackFilter) {
                HashedItem type = new HashedItem(((QIOItemStackFilter) filter).getItemStack());
                map.put(type, freq.getStored(type));
            } else if (filter instanceof QIOTagFilter) {
                String tagName = ((QIOTagFilter) filter).getTagName();
                map.putAll(freq.getStacksByWildcard(tagName));
            }
        }
        return map;
    }

    public boolean getExportWithoutFilter() {
        return exportWithoutFilter;
    }

    public void toggleExportWithoutFilter() {
        exportWithoutFilter = !exportWithoutFilter;
        markDirty(false);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getExportWithoutFilter, value -> exportWithoutFilter = value));
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        super.writeSustainedData(itemStack);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.AUTO, exportWithoutFilter);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        super.readSustainedData(itemStack);
        exportWithoutFilter = ItemDataUtils.getBoolean(itemStack, NBTConstants.AUTO);
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = super.getTileDataRemap();
        remap.put(NBTConstants.AUTO, NBTConstants.AUTO);
        return remap;
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        super.getConfigurationData(nbtTags);
        nbtTags.putBoolean(NBTConstants.AUTO, exportWithoutFilter);
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        super.setConfigurationData(nbtTags);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.AUTO, value -> exportWithoutFilter = value);
    }

    /**
     * An efficient way to handle large (in item type) item ejections from a QIO frequency. Each eject attempt of a certain item type will use a uniform probability
     * distribution based on a predetermined 'max eject attempt' constant to see if the ejection should take place. This makes sure we will eventually eject each item
     * type, but not attempt every item in the frequency each operation.
     *
     * Abstracting us away from the item map (using the type/count suppliers) allows us to interface directly with the entries of the QIO's item data map when running a
     * filterless ejection, rather then recreating the whole map each ejection operation.
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

        private void eject(QIOFrequency freq, TileEntity tile, Collection<T> ejectMap) {
            if (ejectMap.isEmpty()) {
                return;
            }
            double ejectChance = Math.min(1, (double) MAX_EJECT_ATTEMPTS / ejectMap.size());
            int maxTypes = getMaxTransitTypes(), maxCount = getMaxTransitCount();
            Map<HashedItem, Integer> removed = new Object2IntOpenHashMap<>();
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
                    if (getWorld().getRandom().nextDouble() > ejectChance) {
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
            for (Map.Entry<HashedItem, Integer> entry : removed.entrySet()) {
                ItemStack ret = freq.removeByType(entry.getKey(), entry.getValue());
                if (ret.getCount() != entry.getValue()) {
                    Mekanism.logger.error("QIO ejection item removal didn't line up with prediction: removed {}, expected {}", ret.getCount(), entry.getValue());
                }
            }
        }
    }
}
