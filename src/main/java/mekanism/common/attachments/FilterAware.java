package mekanism.common.attachments;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty()) {
            ItemDataUtils.getAndRemoveData(stack, NBTConstants.FILTERS, (c, k) -> c.getList(k, Tag.TAG_COMPOUND)).ifPresent(this::deserializeNBT);
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

    private <FILTER extends IFilter<?>> Stream<FILTER> getEnabledStream(Class<FILTER> filterClass) {
        return filters.stream()
              .filter(IFilter::isEnabled)
              .filter(filterClass::isInstance)
              .map(filterClass::cast);
    }

    public <FILTER extends IFilter<?>> List<FILTER> getEnabled(Class<FILTER> filterClass) {
        //TODO - 1.20.4: Do we want to cache enabled filters like we do for the filter manager?
        return getEnabledStream(filterClass).toList();
    }

    public <FILTER extends IFilter<?>> boolean anyEnabledMatch(Class<FILTER> filterClass, Predicate<FILTER> validator) {
        return getEnabledStream(filterClass).anyMatch(validator);
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

    @Nullable
    public FilterAware copy(IAttachmentHolder holder) {
        if (filters.isEmpty()) {
            return null;
        }
        FilterAware copy = new FilterAware(holder);
        for (IFilter<?> filter : filters) {
            copy.filters.add(filter.clone());
        }
        return copy;
    }
}