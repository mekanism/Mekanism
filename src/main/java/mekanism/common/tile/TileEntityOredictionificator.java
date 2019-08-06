package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.TileNetworkList;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.OreDictCache;
import mekanism.common.PacketHandler;
import mekanism.common.base.ISustainedData;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityOredictionificator extends TileEntityMekanism implements ISpecialConfigData, ISustainedData {

    private static final int[] SLOTS = {0, 1};
    public static List<String> possibleFilters = Arrays.asList("ingot", "ore", "dust", "nugget");
    public HashList<OredictionificatorFilter> filters = new HashList<>();

    public boolean didProcess;

    public TileEntityOredictionificator() {
        super(MekanismBlock.OREDICTIONIFICATOR);
        doAutoSync = false;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (playersUsing.size() > 0) {
                for (PlayerEntity player : playersUsing) {
                    Mekanism.packetHandler.sendTo(new TileEntityMessage(this, getGenericPacket(new TileNetworkList())), (ServerPlayerEntity) player);
                }
            }

            didProcess = false;
            ItemStack inputStack = getInventory().get(0);
            if (MekanismUtils.canFunction(this) && !inputStack.isEmpty() && getValidName(inputStack) != null) {
                ItemStack result = getResult(inputStack);
                if (!result.isEmpty()) {
                    ItemStack outputStack = getInventory().get(1);
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

    public String getValidName(ItemStack stack) {
        List<String> def = OreDictCache.getOreDictName(stack);
        for (String s : def) {
            for (String pre : possibleFilters) {
                if (s.startsWith(pre)) {
                    return s;
                }
            }
        }
        return null;
    }

    public ItemStack getResult(ItemStack stack) {
        String s = getValidName(stack);
        if (s == null) {
            return ItemStack.EMPTY;
        }
        List<ItemStack> ores = OreDictionary.getOres(s, false);
        for (OredictionificatorFilter filter : filters) {
            if (filter.filter.equals(s)) {
                if (ores.size() - 1 >= filter.index) {
                    return StackUtils.size(ores.get(filter.index), 1);
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
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
        if (!world.isRemote) {
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

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.CONFIG_CARD_CAPABILITY || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.CONFIG_CARD_CAPABILITY || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return (T) this;
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
    public boolean canSetFacing(@Nonnull Direction facing) {
        return facing != Direction.DOWN && facing != Direction.UP;
    }

    public static class OredictionificatorFilter implements IFilter {

        public String filter;
        public int index;

        public static OredictionificatorFilter readFromNBT(CompoundNBT nbtTags) {
            OredictionificatorFilter filter = new OredictionificatorFilter();
            filter.read(nbtTags);
            return filter;
        }

        public static OredictionificatorFilter readFromPacket(ByteBuf dataStream) {
            OredictionificatorFilter filter = new OredictionificatorFilter();
            filter.read(dataStream);
            return filter;
        }

        public void write(CompoundNBT nbtTags) {
            nbtTags.putString("filter", filter);
            nbtTags.putInt("index", index);
        }

        protected void read(CompoundNBT nbtTags) {
            filter = nbtTags.getString("filter");
            index = nbtTags.getInt("index");
        }

        public void write(TileNetworkList data) {
            data.add(filter);
            data.add(index);
        }

        protected void read(ByteBuf dataStream) {
            filter = PacketHandler.readString(dataStream);
            index = dataStream.readInt();
        }

        @Override
        public OredictionificatorFilter clone() {
            OredictionificatorFilter newFilter = new OredictionificatorFilter();
            newFilter.filter = filter;
            newFilter.index = index;
            return newFilter;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + filter.hashCode();
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof OredictionificatorFilter && ((OredictionificatorFilter) obj).filter.equals(filter);
        }
    }
}