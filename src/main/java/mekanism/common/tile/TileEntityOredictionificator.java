package mekanism.common.tile;

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
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.HashList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityOredictionificator extends TileEntityMekanism implements ISpecialConfigData, ISustainedData, ITileFilterHolder<OredictionificatorFilter> {

    public static final Map<String, List<String>> possibleFilters = new Object2ObjectOpenHashMap<>();

    static {
        possibleFilters.put("forge", Arrays.asList("ingots/", "ores/", "dusts/", "nuggets/"));
    }

    private HashList<OredictionificatorFilter> filters = new HashList<>();
    public boolean didProcess;

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;

    public TileEntityOredictionificator() {
        super(MekanismBlocks.OREDICTIONIFICATOR);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        RelativeSide[] sides = new RelativeSide[]{RelativeSide.BOTTOM, RelativeSide.TOP, RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK};
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> !getResult(item).isEmpty(), this, 26, 115), sides);
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 134, 115), sides);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
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
                        markDirty();
                    }
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
                List<Item> matchingItems = filter.getMatchingItems();
                if (matchingItems.size() - 1 >= filter.index) {
                    return new ItemStack(matchingItems.get(filter.index), 1);
                }
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (OredictionificatorFilter filter : filters) {
                CompoundNBT tagCompound = new CompoundNBT();
                filter.write(tagCompound);
                filterTags.add(tagCompound);
            }
            nbtTags.put("filters", filterTags);
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains("filters")) {
            ListNBT tagList = nbtTags.getList("filters", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(OredictionificatorFilter.readFromNBT(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            filters.clear();
            int amount = dataStream.readInt();
            for (int i = 0; i < amount; i++) {
                filters.add(OredictionificatorFilter.readFromPacket(dataStream));
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(filters.size());
        for (OredictionificatorFilter filter : filters) {
            filter.write(data);
        }
        return data;
    }

    @Override
    public TileNetworkList getFilterPacket(TileNetworkList data) {
        return getNetworkedData(data);
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (OredictionificatorFilter filter : filters) {
                CompoundNBT tagCompound = new CompoundNBT();
                filter.write(tagCompound);
                filterTags.add(tagCompound);
            }
            nbtTags.put("filters", filterTags);
        }
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        if (nbtTags.contains("filters")) {
            ListNBT tagList = nbtTags.getList("filters", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(OredictionificatorFilter.readFromNBT(tagList.getCompound(i)));
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
                CompoundNBT tagCompound = new CompoundNBT();
                filter.write(tagCompound);
                filterTags.add(tagCompound);
            }
            ItemDataUtils.setList(itemStack, "filters", filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "filters")) {
            ListNBT tagList = ItemDataUtils.getList(itemStack, "filters");
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(OredictionificatorFilter.readFromNBT(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("filters", "filters");
        return remap;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side == getDirection();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public HashList<OredictionificatorFilter> getFilters() {
        return filters;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> didProcess, value -> didProcess = value));
    }

    public static class OredictionificatorFilter implements IFilter<OredictionificatorFilter> {

        private ResourceLocation filterLocation;
        public int index;

        public static OredictionificatorFilter readFromNBT(CompoundNBT nbtTags) {
            OredictionificatorFilter filter = new OredictionificatorFilter();
            filter.read(nbtTags);
            return filter;
        }

        public static OredictionificatorFilter readFromPacket(PacketBuffer dataStream) {
            OredictionificatorFilter filter = new OredictionificatorFilter();
            filter.read(dataStream);
            return filter;
        }

        public String getFilterText() {
            return filterLocation.toString();
        }

        public void setFilter(ResourceLocation location) {
            filterLocation = location;
        }

        public boolean filterMatches(ResourceLocation location) {
            return filterLocation.equals(location);
        }

        public void write(CompoundNBT nbtTags) {
            nbtTags.putString("filter", getFilterText());
            nbtTags.putInt("index", index);
        }

        protected void read(CompoundNBT nbtTags) {
            filterLocation = new ResourceLocation(nbtTags.getString("filter"));
            index = nbtTags.getInt("index");
        }

        public void write(TileNetworkList data) {
            data.add(filterLocation);
            data.add(index);
        }

        protected void read(PacketBuffer dataStream) {
            filterLocation = dataStream.readResourceLocation();
            index = dataStream.readInt();
        }

        public List<Item> getMatchingItems() {
            if (!hasFilter()) {
                return Collections.emptyList();
            }
            //TODO: Cache the wrapper and maybe elements also
            return new ArrayList<>(new ItemTags.Wrapper(filterLocation).getAllElements());
        }

        @Override
        public OredictionificatorFilter clone() {
            OredictionificatorFilter newFilter = new OredictionificatorFilter();
            newFilter.filterLocation = filterLocation;
            newFilter.index = index;
            return newFilter;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + filterLocation.hashCode();
            return code;
        }

        public boolean hasFilter() {
            return filterLocation != null;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof OredictionificatorFilter && filterLocation.equals(((OredictionificatorFilter) obj).filterLocation);
        }
    }
}