package mekanism.common.content.filter;

import java.util.function.BiConsumer;

public class SortableFilterManager<FILTER extends IFilter<?>> extends FilterManager<FILTER> {

    private final BiConsumer<FILTER, FILTER> postSwap;

    //TODO: Improve how we create this when done with a generic intermediary class
    public SortableFilterManager(Class<? extends FILTER> filterClass, Runnable markForSave) {
        super(filterClass, markForSave);
        this.postSwap = (sourceFilter, targetFilter) -> {
            //Save the change
            this.markForSave.run();
            if (sourceFilter.isEnabled() && targetFilter.isEnabled()) {
                //If both the filters are enabled, then we need to invalidate the enabled filter cache
                // In other cases we can skip doing so as we know the distance is only one
                enabledFilters = null;
            }
        };
    }

    public void moveUp(int filterIndex) {
        filters.swap(filterIndex, filterIndex - 1, postSwap);
    }

    public void moveDown(int filterIndex) {
        filters.swap(filterIndex, filterIndex + 1, postSwap);
    }
}