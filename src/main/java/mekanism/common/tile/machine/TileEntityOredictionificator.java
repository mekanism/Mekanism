package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.HashList;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.machine.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

//TODO - V11: Make this support other tag types, such as fluids
public class TileEntityOredictionificator extends TileEntityConfigurableMachine implements ISpecialConfigData, ISustainedData, ITileFilterHolder<OredictionificatorFilter> {

    public static final Map<String, List<String>> possibleFilters = new Object2ObjectOpenHashMap<>();

    static {
        //TODO: Make this configurable
        possibleFilters.put("forge", Arrays.asList("ingots/", "ores/", "dusts/", "nuggets/", "storage_blocks/"));
    }

    private HashList<OredictionificatorFilter> filters = new HashList<>();
    public boolean didProcess;

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;

    public TileEntityOredictionificator() {
        super(MekanismBlocks.OREDICTIONIFICATOR);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM);
        configComponent.setupIOConfig(TransmissionType.ITEM, inputSlot, outputSlot, RelativeSide.RIGHT);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> !getResult(item).isEmpty(), this, 26, 115));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 134, 115));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        didProcess = false;
        if (MekanismUtils.canFunction(this) && !inputSlot.isEmpty()) {
            ItemStack inputStack = inputSlot.getStack();
            if (getValidName(inputStack) != null) {
                ItemStack result = getResult(inputStack);
                if (!result.isEmpty()) {
                    ItemStack outputStack = outputSlot.getStack();
                    if (outputStack.isEmpty()) {
                        inputSlot.shrinkStack(1, Action.EXECUTE);
                        outputSlot.setStack(result);
                        didProcess = true;
                    } else if (ItemHandlerHelper.canItemStacksStack(outputStack, result) && outputStack.getCount() < outputSlot.getLimit(outputStack)) {
                        inputSlot.shrinkStack(1, Action.EXECUTE);
                        outputSlot.growStack(1, Action.EXECUTE);
                        didProcess = true;
                    }
                    markDirty(false);
                }
            }
        }
    }

    @Nullable
    public ResourceLocation getValidName(ItemStack stack) {
        //TODO: Cache this?
        Set<ResourceLocation> tags = stack.getItem().getTags();
        for (ResourceLocation resource : tags) {
            List<String> filters = possibleFilters.getOrDefault(resource.getNamespace(), Collections.emptyList());
            String path = resource.getPath();
            for (String pre : filters) {
                if (path.startsWith(pre)) {
                    return resource;
                }
            }
        }
        return null;
    }

    public ItemStack getResult(ItemStack stack) {
        ResourceLocation resource = getValidName(stack);
        if (resource == null) {
            return ItemStack.EMPTY;
        }
        for (OredictionificatorFilter filter : filters) {
            if (filter.filterMatches(resource)) {
                return filter.getResult();
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        getConfigurationData(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        setConfigurationData(nbtTags);
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (OredictionificatorFilter filter : filters) {
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
                if (filter instanceof OredictionificatorFilter) {
                    filters.add((OredictionificatorFilter) filter);
                }
            }
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey();
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (OredictionificatorFilter filter : filters) {
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
                if (filter instanceof OredictionificatorFilter) {
                    filters.add((OredictionificatorFilter) filter);
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
    public boolean canPulse() {
        return true;
    }

    @Override
    public HashList<OredictionificatorFilter> getFilters() {
        return filters;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> didProcess, value -> didProcess = value));
        container.track(SyncableFilterList.create(this::getFilters, value -> {
            if (value instanceof HashList) {
                filters = (HashList<OredictionificatorFilter>) value;
            } else {
                filters = new HashList<>(value);
            }
        }));
    }

    //TODO - V11: Rewrite this to be more efficient and also cache various values, with support for tags for fluids and other types
    public static class OredictionificatorFilter extends BaseFilter<OredictionificatorFilter> {

        private ResourceLocation filterLocation;
        private ITag<Item> filterTag;
        @Nonnull
        private Item selectedOutput = Items.AIR;

        public String getFilterText() {
            return filterLocation.toString();
        }

        public void setFilter(ResourceLocation location) {
            setFilterLocation(location);
            List<Item> matchingItems = getMatchingItems();
            if (matchingItems.isEmpty()) {
                selectedOutput = Items.AIR;
            } else {
                selectedOutput = matchingItems.get(0);
            }
        }

        private void setFilterLocation(ResourceLocation location) {
            filterLocation = location;
            if (ItemTags.getCollection().getRegisteredTags().contains(filterLocation)) {
                filterTag = ItemTags.makeWrapperTag(filterLocation.toString());
            } else {
                //If the filter doesn't exist (because we loaded a tag that is no longer valid), then just set the filter to being empty
                filterTag = Tag.getEmptyTag();
            }
        }

        public boolean filterMatches(ResourceLocation location) {
            return filterLocation.equals(location);
        }

        @Override
        public CompoundNBT write(CompoundNBT nbtTags) {
            super.write(nbtTags);
            nbtTags.putString(NBTConstants.FILTER, getFilterText());
            if (selectedOutput != Items.AIR) {
                nbtTags.putString(NBTConstants.SELECTED, selectedOutput.getRegistryName().toString());
            }
            return nbtTags;
        }

        @Override
        public void read(CompoundNBT nbtTags) {
            setFilterLocation(new ResourceLocation(nbtTags.getString(NBTConstants.FILTER)));
            NBTUtils.setResourceLocationIfPresent(nbtTags, NBTConstants.SELECTED, this::setSelectedOrAir);
        }

        @Override
        public void write(PacketBuffer buffer) {
            super.write(buffer);
            buffer.writeResourceLocation(filterLocation);
            buffer.writeResourceLocation(selectedOutput.getRegistryName());
        }

        @Override
        public void read(PacketBuffer dataStream) {
            setFilterLocation(dataStream.readResourceLocation());
            setSelectedOrAir(dataStream.readResourceLocation());
        }

        private void setSelectedOrAir(@Nonnull ResourceLocation resourceLocation) {
            Item output = ForgeRegistries.ITEMS.getValue(resourceLocation);
            selectedOutput = output == null ? Items.AIR : output;
        }

        public List<Item> getMatchingItems() {
            if (hasFilter()) {
                return new ArrayList<>(filterTag.getAllElements());
            }
            return Collections.emptyList();
        }

        public int getIndex() {
            List<Item> matchingItems = getMatchingItems();
            if (selectedOutput == Items.AIR) {
                return 0;
            }
            int index = matchingItems.indexOf(selectedOutput);
            if (index == -1) {
                //If the tag is empty properly handle it
                return 0;
            }
            return index;
        }

        public void next() {
            List<Item> matchingItems = getMatchingItems();
            int size = matchingItems.size();
            if (size > 1) {
                if (selectedOutput == Items.AIR) {
                    selectedOutput = matchingItems.get(1);
                } else {
                    int index = matchingItems.indexOf(selectedOutput);
                    if (index < size - 1) {
                        index++;
                    } else {
                        index = 0;
                    }
                    selectedOutput = matchingItems.get(index);
                }
            }
        }

        public void previous() {
            List<Item> matchingItems = getMatchingItems();
            int size = matchingItems.size();
            if (size > 0) {
                if (selectedOutput == Items.AIR) {
                    selectedOutput = matchingItems.get(size - 1);
                } else {
                    int index = matchingItems.indexOf(selectedOutput);
                    if (index == -1) {
                        index = 0;
                    } else if (index > 0) {
                        index--;
                    } else {
                        index = size - 1;
                    }
                    selectedOutput = matchingItems.get(index);
                }
            }
        }

        public ItemStack getResult() {
            if (!hasFilter()) {
                return ItemStack.EMPTY;
            }
            List<Item> matchingItems = getMatchingItems();
            if (matchingItems.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (selectedOutput == Items.AIR || !matchingItems.contains(selectedOutput)) {
                //Fallback to the first element if we don't have an output selected/it isn't in our possible outputs
                selectedOutput = matchingItems.get(0);
            }
            return new ItemStack(selectedOutput);
        }

        @Override
        public OredictionificatorFilter clone() {
            OredictionificatorFilter newFilter = new OredictionificatorFilter();
            newFilter.filterLocation = filterLocation;
            newFilter.filterTag = filterTag;
            newFilter.selectedOutput = selectedOutput;
            return newFilter;
        }

        @Override
        public FilterType getFilterType() {
            return FilterType.OREDICTIONIFICATOR;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + filterLocation.hashCode();
            return code;
        }

        @Override
        public boolean hasFilter() {
            return filterLocation != null;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof OredictionificatorFilter && filterLocation.equals(((OredictionificatorFilter) obj).filterLocation);
        }
    }
}