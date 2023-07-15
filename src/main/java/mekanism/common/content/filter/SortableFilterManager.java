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

    public void moveToTop(int filterIndex) {
        moveTo(filterIndex, 0);
    }

    public void moveToBottom(int filterIndex) {
        moveTo(filterIndex, count() - 1);
    }

    private void moveTo(int source, int target) {
        // Make sure both source and target are legal values
        if (source == target || source < 0 || target < 0) {
            return;
        }
        int size = count();
        if (source >= size || target >= size) {
            return;
        }
        //Remove from current position
        FILTER sourceFilter = filters.remove(source);
        // and add at the target position
        filters.add(target, sourceFilter);
        //Save the change
        this.markForSave.run();
        if (sourceFilter.isEnabled()) {
            //If the moved filter is enabled, then we need to invalidate the enabled filter cache
            // as it is quicker than checking if any of the filters that were jumped over were enabled
            enabledFilters = null;
        }
    }
}