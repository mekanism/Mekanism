package mekanism.common.content.qio;

import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.NBTConstants;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.lib.HashList;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public interface IQIOInputTile extends ISpecialConfigData, ISustainedData, ITileFilterHolder<QIOFilter<?>>,
      IHasSortableFilters {

    HashList<QIOFilter<?>> getQIOFilters();

    void markDirty(boolean updateState);

    Block getBlockType();

    @Override
    default void writeSustainedData(ItemStack itemStack) {
        if (!getFilters().isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (QIOFilter<?> filter : getFilters()) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            ItemDataUtils.setList(itemStack, NBTConstants.FILTERS, filterTags);
        }
    }

    @Override
    default void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = ItemDataUtils.getList(itemStack, NBTConstants.FILTERS);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof QIOFilter) {
                    getFilters().add((QIOFilter<?>) filter);
                }
            }
        }
    }

    @Override
    default CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        if (!getFilters().isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (QIOFilter<?> filter : getFilters()) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            nbtTags.put(NBTConstants.FILTERS, filterTags);
        }
        return nbtTags;
    }

    @Override
    default void setConfigurationData(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof QIOFilter) {
                    getFilters().add((QIOFilter<?>) filter);
                }
            }
        }
    }

    @Override
    default String getDataType() {
        return getBlockType().getTranslationKey();
    }

    @Override
    default void moveUp(int filterIndex) {
        getFilters().swap(filterIndex, filterIndex - 1);
        markDirty(false);
    }

    @Override
    default void moveDown(int filterIndex) {
        getFilters().swap(filterIndex, filterIndex + 1);
        markDirty(false);
    }
}
