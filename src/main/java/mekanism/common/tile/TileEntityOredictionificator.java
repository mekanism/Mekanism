package mekanism.common.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.TileNetworkList;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

    private static final int[] SLOTS = {0, 1};
    public static final Map<String, List<String>> possibleFilters = new HashMap<>();

    static {
        possibleFilters.put("forge", Arrays.asList("ingots/", "ores/", "dusts/", "nuggets/"));
    }

    private HashList<OredictionificatorFilter> filters = new HashList<>();
    public boolean didProcess;

    public TileEntityOredictionificator() {
        super(MekanismBlock.OREDICTIONIFICATOR);
        doAutoSync = false;
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            if (playersUsing.size() > 0) {
                for (PlayerEntity player : playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(this, getGenericPacket(new TileNetworkList())), (ServerPlayerEntity) player);
                }
            }

            didProcess = false;
            ItemStack inputStack = getStackInSlot(0);
            if (MekanismUtils.canFunction(this) && !inputStack.isEmpty() && getValidName(inputStack) != null) {
                ItemStack result = getResult(inputStack);
                if (!result.isEmpty()) {
                    ItemStack outputStack = getStackInSlot(1);
                    if (outputStack.isEmpty()) {
                        inputStack.shrink(1);
                        if (inputStack.getCount() <= 0) {
                            getInventory().set(0, ItemStack.EMPTY);
                        }
                        getInventory().set(1, result);
                        didProcess = true;
                    } else if (ItemHandlerHelper.canItemStacksStack(outputStack, result) && outputStack.getCount() < outputStack.getMaxStackSize()) {
                        inputStack.shrink(1);
                        if (inputStack.getCount() <= 0) {
                            getInventory().set(0, ItemStack.EMPTY);
                        }
                        outputStack.grow(1);
                        didProcess = true;
                    }
                    markDirty();
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
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side != getDirection()) {
            return SLOTS;
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        return slotID == 1;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return slotID == 0 && !getResult(itemstack).isEmpty();

    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        ListNBT filterTags = new ListNBT();
        for (OredictionificatorFilter filter : filters) {
            CompoundNBT tagCompound = new CompoundNBT();
            filter.write(tagCompound);
            filterTags.add(tagCompound);
        }
        if (!filterTags.isEmpty()) {
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
            int type = dataStream.readInt();
            if (type == 0) {
                didProcess = dataStream.readBoolean();
                filters.clear();

                int amount = dataStream.readInt();
                for (int i = 0; i < amount; i++) {
                    filters.add(OredictionificatorFilter.readFromPacket(dataStream));
                }
            } else if (type == 1) {
                didProcess = dataStream.readBoolean();
            } else if (type == 2) {
                filters.clear();
                int amount = dataStream.readInt();
                for (int i = 0; i < amount; i++) {
                    filters.add(OredictionificatorFilter.readFromPacket(dataStream));
                }
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(0);
        data.add(didProcess);
        data.add(filters.size());
        for (OredictionificatorFilter filter : filters) {
            filter.write(data);
        }
        return data;
    }

    public TileNetworkList getGenericPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(1);
        data.add(didProcess);
        return data;
    }

    @Override
    public TileNetworkList getFilterPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(2);
        data.add(filters.size());
        for (OredictionificatorFilter filter : filters) {
            filter.write(data);
        }
        return data;
    }

    @Override
    public void openInventory(@Nonnull PlayerEntity player) {
        if (!isRemote()) {
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        ListNBT filterTags = new ListNBT();
        for (OredictionificatorFilter filter : filters) {
            CompoundNBT tagCompound = new CompoundNBT();
            filter.write(tagCompound);
            filterTags.add(tagCompound);
        }
        if (!filterTags.isEmpty()) {
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
        ItemDataUtils.setBoolean(itemStack, "hasOredictionificatorConfig", true);
        ListNBT filterTags = new ListNBT();
        for (OredictionificatorFilter filter : filters) {
            CompoundNBT tagCompound = new CompoundNBT();
            filter.write(tagCompound);
            filterTags.add(tagCompound);
        }
        if (!filterTags.isEmpty()) {
            ItemDataUtils.setList(itemStack, "filters", filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "hasOredictionificatorConfig")) {
            if (ItemDataUtils.hasData(itemStack, "filters")) {
                ListNBT tagList = ItemDataUtils.getList(itemStack, "filters");
                for (int i = 0; i < tagList.size(); i++) {
                    filters.add(OredictionificatorFilter.readFromNBT(tagList.getCompound(i)));
                }
            }
        }
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