package mekanism.common.content.oredictionificator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import mekanism.api.NBTConstants;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OredictionificatorFilter<TYPE, STACK, FILTER extends OredictionificatorFilter<TYPE, STACK, FILTER>> extends BaseFilter<FILTER> {

    @Nullable
    private TagKey<TYPE> filterLocation;
    @Nullable
    private HolderSet.Named<TYPE> filterTag;
    @NotNull
    private Holder<TYPE> selectedOutput = getFallbackElement();
    @Nullable
    private STACK cachedSelectedStack;
    private boolean isValid;

    protected OredictionificatorFilter() {
    }

    protected OredictionificatorFilter(FILTER other) {
        super(other);
        //Local variable needed so that it can reference the private fields on it
        OredictionificatorFilter<TYPE, STACK, FILTER> filter = other;
        filterLocation = filter.filterLocation;
        filterTag = filter.filterTag;
        selectedOutput = filter.selectedOutput;
        cachedSelectedStack = filter.cachedSelectedStack;
        isValid = filter.isValid;
    }

    public void flushCachedTag() {
        //If the filter doesn't exist (because we loaded a tag that is no longer valid), then just set the filter to being empty
        filterTag = filterLocation == null ? null : getRegistry().getTag(filterLocation).orElse(null);
        if (filterTag == null) {
            setSelectedOutput(getFallbackElement());
        } else if (!filterTag.contains(selectedOutput)) {
            filterTag.stream().findFirst().ifPresentOrElse(this::setSelectedOutput, this::setToFallback);
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
        if (filterLocation != null && getRegistry().getTag(filterLocation).isPresent()) {
            for (String filter : getValidValuesConfig().get().getOrDefault(filterLocation.location().getNamespace(), Collections.emptyList())) {
                if (filterLocation.location().getPath().startsWith(filter)) {
                    isValid = true;
                    return;
                }
            }
        }
        isValid = false;
    }

    @ComputerMethod(nameOverride = "getFilter", threadSafe = true)
    public String getFilterText() {
        return filterLocation == null ? "" : filterLocation.location().toString();
    }

    /**
     * This method should only be called if the filter is valid or if it isn't the validity should be rechecked afterwards
     */
    public final void setFilter(@Nullable ResourceLocation location) {
        filterLocation = location == null ? null : TagKey.create(getRegistry().key(), location);
        flushCachedTag();
        isValid = true;
    }

    @ComputerMethod(nameOverride = "setFilter")
    public void computerSetFilter(ResourceLocation tag) throws ComputerException {
        if (tag == null || !TileEntityOredictionificator.isValidTarget(tag)) {
            throw new ComputerException("Invalid tag");
        }
        setFilter(tag);
    }

    /**
     * Only publicly exposed for creating via ComputerCraft
     */
    public final void setSelectedOutput(@NotNull Holder<TYPE> output) {
        this.selectedOutput = output;
        //Invalidate cached stack
        cachedSelectedStack = null;
    }

    public boolean filterMatches(ResourceLocation location) {
        return filterLocation != null && filterLocation.location().equals(location);
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.FILTER, getFilterText());
        if (selectedOutput != getFallbackElement()) {
            NBTUtils.writeRegistryEntry(nbtTags, NBTConstants.SELECTED, getRegistry(), selectedOutput);
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        NBTUtils.setResourceLocationIfPresentElse(nbtTags, NBTConstants.FILTER, this::setFilter, () -> setFilter(null));
        NBTUtils.setResourceLocationIfPresent(nbtTags, NBTConstants.SELECTED, this::setSelectedOrFallback);
        //Recheck filter validity after reading from nbt
        checkValidity();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        //Realistically the filter location shouldn't be null except when the filter is first being created
        // but handle it being null just in case
        buffer.writeNullable(filterLocation, (buf, location) -> buf.writeResourceLocation(location.location()));
        buffer.writeResourceKey(selectedOutput.unwrapKey().orElseThrow());
        buffer.writeBoolean(isValid);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        super.read(buffer);
        setFilter(buffer.readNullable(FriendlyByteBuf::readResourceLocation));
        setSelectedOrFallback(buffer.readResourceLocation());
        isValid = buffer.readBoolean();
    }

    private void setToFallback() {
        setSelectedOutput(getFallbackElement());
    }

    private void setSelectedOrFallback(@NotNull ResourceLocation resourceLocation) {
        Registry<TYPE> registry = getRegistry();
        registry.getHolder(ResourceKey.create(registry.key(), resourceLocation))
              .ifPresentOrElse(this::setSelectedOutput, this::setToFallback);
    }

    public STACK getResult() {
        //If we don't currently have a result stack cached, calculate what the result stack is
        if (cachedSelectedStack == null) {
            if (filterTag == null || filterTag.size() == 0) {
                cachedSelectedStack = getEmptyStack();
            } else {
                if (selectedOutput == getFallbackElement() || !filterTag.contains(selectedOutput)) {
                    //Fallback to the first element if we don't have an output selected/it isn't in our possible outputs
                    selectedOutput = filterTag.get(0);
                }
                cachedSelectedStack = createResultStack(selectedOutput.value());
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
        if (filterTag == null) {
            return;
        }
        int size = filterTag.size();
        //Check if there is more than one element as the selected output does not need to change if there is only one element
        if (size > 1) {
            int selected;
            if (selectedOutput == getFallbackElement()) {
                selected = size - 1;
            } else {
                List<Holder<TYPE>> matchingElements = filterTag.stream().toList();
                selected = calculateSelected.applyAsInt(matchingElements.indexOf(selectedOutput), size);
            }
            setSelectedOutput(filterTag.get(selected));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), filterLocation, selectedOutput);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        OredictionificatorFilter<?, ?, ?> other = (OredictionificatorFilter<?, ?, ?>) o;
        return Objects.equals(filterLocation, other.filterLocation) && selectedOutput == other.selectedOutput;
    }

    public abstract TYPE getResultElement();

    protected abstract Registry<TYPE> getRegistry();

    protected abstract Holder<TYPE> getFallbackElement();

    protected abstract STACK getEmptyStack();

    protected abstract STACK createResultStack(TYPE type);

    protected abstract CachedOredictionificatorConfigValue getValidValuesConfig();

    @Override
    @ComputerMethod(threadSafe = true)
    public abstract FILTER clone();
}
