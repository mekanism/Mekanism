package mekanism.common.tile.qio;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.lib.collection.HashList;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityQIOFilterHandler extends TileEntityQIOComponent implements ITileFilterHolder<QIOFilter<?>>, IHasSortableFilters, ISustainedData {

    private HashList<QIOFilter<?>> filters = new HashList<>();

    public TileEntityQIOFilterHandler(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    @ComputerMethod
    public HashList<QIOFilter<?>> getFilters() {
        return filters;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (QIOFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            ItemDataUtils.setList(itemStack, NBTConstants.FILTERS, filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = ItemDataUtils.getList(itemStack, NBTConstants.FILTERS);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof QIOFilter) {
                    filters.add((QIOFilter<?>) filter);
                }
            }
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.FILTERS, NBTConstants.FILTERS);
        return remap;
    }

    @Override
    protected void addGeneralPersistentData(CompoundNBT data) {
        super.addGeneralPersistentData(data);
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (QIOFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            data.put(NBTConstants.FILTERS, filterTags);
        }
    }

    @Override
    protected void loadGeneralPersistentData(CompoundNBT data) {
        super.loadGeneralPersistentData(data);
        filters.clear();
        if (data.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = data.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof QIOFilter) {
                    filters.add((QIOFilter<?>) filter);
                }
            }
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFilterList.create(this::getFilters, value -> {
            if (value instanceof HashList) {
                filters = (HashList<QIOFilter<?>>) value;
            } else {
                filters = new HashList<>(value);
            }
        }));
    }

    @Override
    public void moveUp(int filterIndex) {
        filters.swap(filterIndex, filterIndex - 1);
        markDirty(false);
    }

    @Override
    public void moveDown(int filterIndex) {
        filters.swap(filterIndex, filterIndex + 1);
        markDirty(false);
    }

    protected int getMaxTransitCount() {
        // 64 to 320 items
        return 64 + 32 * upgradeComponent.getUpgrades(Upgrade.SPEED);
    }

    protected int getMaxTransitTypes() {
        // 1 to 5 types
        return Math.round(1F + upgradeComponent.getUpgrades(Upgrade.SPEED) / 2F);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private boolean addFilter(QIOFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filters.add(filter);
    }

    @ComputerMethod
    private boolean removeFilter(QIOFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filters.remove(filter);
    }
    //End methods IComputerTile
}