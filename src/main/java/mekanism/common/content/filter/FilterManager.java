package mekanism.common.content.filter;

import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.lib.collection.HashList;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

public class FilterManager<FILTER extends IFilter<?>> {

    private final Class<? extends FILTER> filterClass;
    protected final Runnable markForSave;

    protected HashList<FILTER> filters = new HashList<>();
    @Nullable
    protected List<FILTER> enabledFilters = null;

    public FilterManager(Class<? extends FILTER> filterClass, Runnable markForSave) {
        this.filterClass = filterClass;
        this.markForSave = markForSave;
    }

    public final HashList<FILTER> getFilters() {
        //TODO: Decide at some point if we want getFilters and getEnabledFilters to return an unmodifiable view
        return filters;
    }

    public final List<FILTER> getEnabledFilters() {
        if (enabledFilters == null) {
            //Collect it into a mutable array list so that we can modify the cache when adding filters to the end
            enabledFilters = filters.stream().filter(IFilter::isEnabled).collect(Collectors.toList());
        }
        return enabledFilters;
    }

    public final int count() {
        return filters.size();
    }

    public boolean anyEnabledMatch(Predicate<FILTER> validator) {
        for (FILTER filter : getEnabledFilters()) {
            if (validator.test(filter)) {
                return true;
            }
        }
        return false;
    }

    public <DATA> boolean anyEnabledMatch(DATA extra, BiPredicate<FILTER, DATA> validator) {
        for (FILTER filter : getEnabledFilters()) {
            if (validator.test(filter, extra)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEnabledFilters() {
        return !getEnabledFilters().isEmpty();
    }

    public void toggleState(int index) {
        FILTER filter = filters.getOrNull(index);
        if (filter != null) {
            filter.setEnabled(!filter.isEnabled());
            markForSave.run();
            //Clear the cache of enabled filters as we either need to remove the element from it or add to it
            enabledFilters = null;
        }
    }

    public void trySetFilters(Collection<IFilter<?>> filters) {
        this.filters.clear();
        //Instantiate an empty cache for enabled filters so that when we add enabled filters
        // we can also add them to the enabled ones, and also overwrite our old cache
        enabledFilters = new ArrayList<>();
        for (IFilter<?> filter : filters) {
            tryAddFilter(filter, false);
        }
        markForSave.run();
    }

    public void tryAddFilter(IFilter<?> toAdd, boolean save) {
        if (filterClass.isInstance(toAdd)) {
            addFilter(filterClass.cast(toAdd), save);
        }
    }

    public boolean addFilter(FILTER filter) {
        return addFilter(filter, true);
    }

    private boolean addFilter(FILTER filter, boolean save) {
        boolean result = filters.add(filter);
        if (save) {
            markForSave.run();
        }
        if (enabledFilters != null && filter.isEnabled()) {
            //If enabled filters is already initialized then just add it at the end which is where it should go
            enabledFilters.add(filter);
        }
        return result;
    }

    public boolean removeFilter(FILTER filter) {
        boolean result = filters.remove(filter);
        markForSave.run();
        if (filter.isEnabled()) {
            //Reset the enabled filter cache if we removed an enabled filter
            enabledFilters = null;
        }
        return result;
    }

    public <F extends IFilter<F>> void tryEditFilter(F currentFilter, @Nullable F newFilter) {
        if (filterClass.isInstance(currentFilter)) {
            if (newFilter == null) {
                removeFilter(filterClass.cast(currentFilter));
            } else {
                editFilter(filterClass.cast(currentFilter), filterClass.cast(newFilter));
            }
        }
    }

    private void editFilter(FILTER currentFilter, FILTER newFilter) {
        //TODO: Add in validation so that if a filter tries to be saved that is invalid we don't add it/save it?
        if (filters.replace(currentFilter, newFilter)) {
            //Save the filters
            markForSave.run();
            if (currentFilter.isEnabled() || newFilter.isEnabled()) {
                //Reset the enabled filter cache if we actually replaced the existing filter and at least one of the two was/is enabled
                enabledFilters = null;
            }
        }
    }

    public void addContainerTrackers(MekanismContainer container) {
        container.track(SyncableFilterList.create(this::getFilters, value -> {
            if (value instanceof HashList<FILTER> filterList) {
                this.filters = filterList;
            } else {
                this.filters = new HashList<>(value);
            }
            //Reset the enabled filter cache
            enabledFilters = null;
        }));
    }

    public void writeToNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (!filters.isEmpty()) {
            ListTag filterTags = new ListTag(filters.size());
            RegistryOps<Tag> serializationContext = provider.createSerializationContext(NbtOps.INSTANCE);
            for (FILTER filter : filters) {
                DataResult<Tag> encoded = BaseFilter.GENERIC_CODEC.encodeStart(serializationContext, filter);
                encoded.ifSuccess(filterTags::add);
                encoded.ifError(error -> Mekanism.logger.warn("Failed to serialize filter: {}", error.message()));
            }
            if (!filterTags.isEmpty()) {
                nbt.put(SerializationConstants.FILTERS, filterTags);
            }
        }
    }

    public void readFromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        filters.clear();
        //Instantiate an empty cache for enabled filters so that when we add enabled filters
        // we can also add them to the enabled ones, and also overwrite our old cache
        enabledFilters = new ArrayList<>();
        NBTUtils.setListIfPresent(nbt, SerializationConstants.FILTERS, Tag.TAG_COMPOUND, tagList -> {
            RegistryOps<Tag> serializationContext = provider.createSerializationContext(NbtOps.INSTANCE);
            for (int i = 0, size = tagList.size(); i < size; i++) {
                DataResult<IFilter<?>> decoded = BaseFilter.GENERIC_CODEC.parse(serializationContext, tagList.getCompound(i));
                if (decoded.isSuccess()) {
                    tryAddFilter(decoded.getOrThrow(), false);
                } else {
                    decoded.ifError(error -> Mekanism.logger.warn("Failed to deserialize stored filter: {}", error.message()));
                }
            }
        });
    }
}