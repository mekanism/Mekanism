package mekanism.common.tile.qio;

import java.util.Map;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.lib.HashList;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityQIOFilterHandler extends TileEntityQIOComponent implements ISpecialConfigData, ISustainedData, ITileFilterHolder<QIOFilter<?>> {

    private HashList<QIOFilter<?>> filters = new HashList<>();

    public TileEntityQIOFilterHandler(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
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
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (QIOFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            nbtTags.put(NBTConstants.FILTERS, filterTags);
        }
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof QIOFilter) {
                    filters.add((QIOFilter<?>) filter);
                }
            }
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey();
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        return getConfigurationData(nbtTags);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        setConfigurationData(nbtTags);
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
}
