package mekanism.common.content.oredictionificator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class OredictionificatorFilter<TYPE extends IForgeRegistryEntry<TYPE>, STACK, FILTER extends OredictionificatorFilter<TYPE, STACK, FILTER>>
      extends BaseFilter<FILTER> {

    @Nullable
    private ResourceLocation filterLocation;
    private ITag<TYPE> filterTag;
    @Nonnull
    private TYPE selectedOutput = getFallbackElement();
    @Nullable
    private STACK cachedSelectedStack;
    private boolean isValid;

    protected OredictionificatorFilter() {
        filterTag = Tag.empty();
    }

    protected OredictionificatorFilter(OredictionificatorFilter<TYPE, STACK, FILTER> filter) {
        filterLocation = filter.filterLocation;
        filterTag = filter.filterTag;
        selectedOutput = filter.selectedOutput;
        cachedSelectedStack = filter.cachedSelectedStack;
        isValid = filter.isValid;
    }

    public void flushCachedTag() {
        //If the filter doesn't exist (because we loaded a tag that is no longer valid), then just set the filter to being empty
        filterTag = filterLocation == null ? Tag.empty() : getTagCollection().getTagOrEmpty(filterLocation);
        if (!filterTag.contains(selectedOutput)) {
            List<TYPE> elements = filterTag.getValues();
            setSelectedOutput(elements.isEmpty() ? getFallbackElement() : elements.get(0));
        }
        //Note: Even though the tag instance may have changed, we don't need to reset the cached
        // stack if the tag still contains the selected output as that means it is not empty and
        // the stack is still valid
    }

    @Override
    public boolean hasFilter() {
        return filterLocation != null && isValid;
    }

    public void checkValidity() {
        if (filterLocation != null && getTagCollection().getAvailableTags().contains(filterLocation)) {
            for (String filter : getValidValuesConfig().get().getOrDefault(filterLocation.getNamespace(), Collections.emptyList())) {
                if (filterLocation.getPath().startsWith(filter)) {
                    isValid = true;
                    return;
                }
            }
        }
        isValid = false;
    }

    public String getFilterText() {
        return filterLocation == null ? "" : filterLocation.toString();
    }

    /**
     * This method should only be called if the filter is valid or if it isn't the validity should be rechecked afterwards
     */
    public final void setFilter(ResourceLocation location) {
        filterLocation = location;
        flushCachedTag();
        isValid = true;
    }

    /**
     * Only publicly exposed for creating via ComputerCraft
     */
    public final void setSelectedOutput(@Nonnull TYPE output) {
        this.selectedOutput = output;
        //Invalidate cached stack
        cachedSelectedStack = null;
    }

    public boolean filterMatches(ResourceLocation location) {
        return filterLocation != null && filterLocation.equals(location);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.FILTER, getFilterText());
        if (selectedOutput != getFallbackElement()) {
            nbtTags.putString(NBTConstants.SELECTED, selectedOutput.getRegistryName().toString());
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        NBTUtils.setResourceLocationIfPresentElse(nbtTags, NBTConstants.FILTER, this::setFilter, () -> setFilter(null));
        NBTUtils.setResourceLocationIfPresent(nbtTags, NBTConstants.SELECTED, this::setSelectedOrFallback);
        //Recheck filter validity after reading from nbt
        checkValidity();
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        //Realistically the filter location shouldn't be null except when the filter is first being created
        // but handle it being null just in case
        buffer.writeBoolean(filterLocation != null);
        if (filterLocation != null) {
            buffer.writeResourceLocation(filterLocation);
        }
        buffer.writeResourceLocation(selectedOutput.getRegistryName());
        buffer.writeBoolean(isValid);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setFilter(buffer.readBoolean() ? buffer.readResourceLocation() : null);
        setSelectedOrFallback(buffer.readResourceLocation());
        isValid = buffer.readBoolean();
    }

    private void setSelectedOrFallback(@Nonnull ResourceLocation resourceLocation) {
        TYPE output = getRegistry().getValue(resourceLocation);
        setSelectedOutput(output == null ? getFallbackElement() : output);
    }

    public STACK getResult() {
        //If we don't currently have a result stack cached, calculate what the result stack is
        if (cachedSelectedStack == null) {
            //Note: if there is no filter filterTag will be empty
            List<TYPE> matchingElements = filterTag.getValues();
            if (matchingElements.isEmpty()) {
                cachedSelectedStack = getEmptyStack();
            } else {
                if (selectedOutput == getFallbackElement() || !matchingElements.contains(selectedOutput)) {
                    //Fallback to the first element if we don't have an output selected/it isn't in our possible outputs
                    selectedOutput = matchingElements.get(0);
                }
                cachedSelectedStack = createResultStack(selectedOutput);
            }
        }
        return cachedSelectedStack;
    }

    public final void next() {
        adjustSelected((index, size) -> {
            if (index < size - 1) {
                return index + 1;
            }
            return 0;
        });
    }

    public final void previous() {
        adjustSelected((index, size) -> {
            if (index == -1) {
                return 0;
            } else if (index > 0) {
                return index - 1;
            }
            return size - 1;
        });
    }

    private void adjustSelected(IntBinaryOperator calculateSelected) {
        List<TYPE> matchingElements = filterTag.getValues();
        int size = matchingElements.size();
        //Check if there is more than one element as the selected output does not need to change if there is only one element
        if (size > 1) {
            int selected;
            if (selectedOutput == getFallbackElement()) {
                selected = size - 1;
            } else {
                selected = calculateSelected.applyAsInt(matchingElements.indexOf(selectedOutput), size);
            }
            setSelectedOutput(matchingElements.get(selected));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(filterLocation, ((OredictionificatorFilter<?, ?, ?>) o).filterLocation);
    }

    public abstract TYPE getResultElement();

    protected abstract IForgeRegistry<TYPE> getRegistry();

    protected abstract ITagCollection<TYPE> getTagCollection();

    protected abstract TYPE getFallbackElement();

    protected abstract STACK getEmptyStack();

    protected abstract STACK createResultStack(TYPE type);

    protected abstract CachedOredictionificatorConfigValue getValidValuesConfig();
}