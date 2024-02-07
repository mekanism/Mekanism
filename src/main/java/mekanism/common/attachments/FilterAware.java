package mekanism.common.attachments;

import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.lib.collection.HashList;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class FilterAware implements INBTSerializable<ListTag> {

    private final HashList<IFilter<?>> filters = new HashList<>();

    public FilterAware(IAttachmentHolder attachmentHolder) {
        loadLegacyData(attachmentHolder);
    }

    @Deprecated//TODO - 1.21: Remove this legacy way of loading data
    private void loadLegacyData(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty() && ItemDataUtils.hasData(stack, NBTConstants.FILTERS, Tag.TAG_LIST)) {
            deserializeNBT(ItemDataUtils.getList(stack, NBTConstants.FILTERS));
            //Remove the legacy data now that it has been parsed and loaded
            ItemDataUtils.removeData(stack, NBTConstants.FILTERS);
        }
    }

    public void copyTo(FilterManager<?> filterManager) {
        //TODO - 1.20.4: Do we need to copy these or can we just pass the raw instance?
        filterManager.trySetFilters(filters);
    }

    public void copyFrom(FilterManager<?> filterManager) {
        filters.clear();
        filters.addAll(filterManager.getFilters());
    }

    public boolean isCompatible(FilterAware other) {
        return other == this || filters.equals(other.filters);
    }

    @Nullable
    @Override
    public ListTag serializeNBT() {
        if (this.filters.isEmpty()) {
            return null;
        }
        ListTag filterTags = new ListTag();
        for (IFilter<?> filter : filters) {
            filterTags.add(filter.write(new CompoundTag()));
        }
        return filterTags;
    }

    @Override
    public void deserializeNBT(ListTag tagList) {
        filters.clear();
        for (int i = 0, size = tagList.size(); i < size; i++) {
            IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
            if (filter != null) {
                filters.add(filter);
            }
        }
    }
}