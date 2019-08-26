package mekanism.common.tile;

import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.Upgrade;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.Finder;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.StackSearcher;
import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.content.transporter.TOreDictFilter;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.ItemRegistryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityLogisticalSorter extends TileEntityMekanism implements ISpecialConfigData, ISustainedData, IComputerIntegration, IUpgradeTile, IComparatorSupport,
      ITileFilterHolder<TransporterFilter> {

    private HashList<TransporterFilter> filters = new HashList<>();
    public EnumColor color;
    public boolean autoEject;
    public boolean roundRobin;
    public boolean singleItem;
    public int rrIndex = 0;
    public int delayTicks;
    public TileComponentUpgrade<TileEntityLogisticalSorter> upgradeComponent;
    public String[] methods = {"setDefaultColor", "setRoundRobin", "setAutoEject", "addFilter", "removeFilter", "addOreFilter", "removeOreFilter", "setSingleItem"};
    private int currentRedstoneLevel;

    public TileEntityLogisticalSorter() {
        super(MekanismBlock.LOGISTICAL_SORTER);
        rapidChangeThreshold = 3;
        doAutoSync = false;
        upgradeComponent = new TileComponentUpgrade<>(this, 1);
        upgradeComponent.clearSupportedTypes();
        upgradeComponent.setSupported(Upgrade.MUFFLING);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            delayTicks = Math.max(0, delayTicks - 1);
            if (delayTicks == 6) {
                setActive(false);
            }

            if (MekanismUtils.canFunction(this) && delayTicks == 0) {
                TileEntity back = Coord4D.get(this).offset(getOppositeDirection()).getTileEntity(world);
                TileEntity front = Coord4D.get(this).offset(getDirection()).getTileEntity(world);
                //If there is no tile to pull from or the push to, skip doing any checks
                if (InventoryUtils.isItemHandler(back, getDirection()) && front != null) {
                    boolean sentItems = false;
                    int min = 0;

                    outer:
                    for (TransporterFilter filter : filters) {
                        for (StackSearcher search = new StackSearcher(back, getOppositeDirection()); search.getSlotCount() >= 0; ) {
                            InvStack invStack = filter.getStackFromInventory(search, singleItem);
                            if (invStack == null) {
                                break;
                            }
                            ItemStack itemStack = invStack.getStack();
                            if (filter.canFilter(itemStack, !singleItem)) {
                                if (!singleItem && filter instanceof TItemStackFilter) {
                                    TItemStackFilter itemFilter = (TItemStackFilter) filter;
                                    if (itemFilter.sizeMode) {
                                        min = itemFilter.min;
                                    }
                                }

                                TransitRequest request = TransitRequest.getFromStack(itemStack);
                                TransitResponse response = emitItemToTransporter(front, request, filter.color, min);
                                if (!response.isEmpty()) {
                                    invStack.use(response.getSendingAmount());
                                    back.markDirty();
                                    setActive(true);
                                    sentItems = true;
                                    break outer;
                                }
                            }
                        }
                    }

                    if (!sentItems && autoEject) {
                        TransitRequest request = TransitRequest.buildInventoryMap(back, getOppositeDirection(), singleItem ? 1 : 64, new StrictFilterFinder());
                        TransitResponse response = emitItemToTransporter(front, request, color, 0);
                        if (!response.isEmpty()) {
                            response.getInvStack(back, getDirection()).use(response.getSendingAmount());
                            back.markDirty();
                            setActive(true);
                        }
                    }
                }

                delayTicks = 10;
            }
            if (playersUsing.size() > 0) {
                for (PlayerEntity player : playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(this, getGenericPacket(new TileNetworkList())), (ServerPlayerEntity) player);
                }
            }

            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    public TransitResponse emitItemToTransporter(TileEntity front, TransitRequest request, EnumColor filterColor, int min) {
        return CapabilityUtils.getCapabilityHelper(front, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, getOppositeDirection()).getIfPresentElseDo(
              transporter -> {
                  if (roundRobin) {
                      return TransporterUtils.insertRR(this, transporter, request, filterColor, true, min);
                  }
                  return TransporterUtils.insert(this, transporter, request, filterColor, true, min);
              },
              () -> InventoryUtils.putStackInInventory(front, request, getDirection(), false)
        );
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);

        if (color != null) {
            nbtTags.putInt("color", TransporterUtils.colors.indexOf(color));
        }

        nbtTags.putBoolean("autoEject", autoEject);
        nbtTags.putBoolean("roundRobin", roundRobin);
        nbtTags.putBoolean("singleItem", singleItem);

        nbtTags.putInt("rrIndex", rrIndex);

        ListNBT filterTags = new ListNBT();

        for (TransporterFilter filter : filters) {
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
        if (nbtTags.contains("color")) {
            color = TransporterUtils.colors.get(nbtTags.getInt("color"));
        }

        autoEject = nbtTags.getBoolean("autoEject");
        roundRobin = nbtTags.getBoolean("roundRobin");
        singleItem = nbtTags.getBoolean("singleItem");

        rrIndex = nbtTags.getInt("rrIndex");

        if (nbtTags.contains("filters")) {
            ListNBT tagList = nbtTags.getList("filters", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(TransporterFilter.readFromNBT(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!world.isRemote) {
            int type = dataStream.readInt();
            if (type == 0) {
                int clickType = dataStream.readInt();
                if (clickType == 0) {
                    color = TransporterUtils.increment(color);
                } else if (clickType == 1) {
                    color = TransporterUtils.decrement(color);
                } else if (clickType == 2) {
                    color = null;
                }
            } else if (type == 1) {
                autoEject = !autoEject;
            } else if (type == 2) {
                roundRobin = !roundRobin;
                rrIndex = 0;
            } else if (type == 3) {
                // Move filter up
                int filterIndex = dataStream.readInt();
                filters.swap(filterIndex, filterIndex - 1);
                for (PlayerEntity player : playersUsing) {
                    openInventory(player);
                }
            } else if (type == 4) {
                // Move filter down
                int filterIndex = dataStream.readInt();
                filters.swap(filterIndex, filterIndex + 1);
                for (PlayerEntity player : playersUsing) {
                    openInventory(player);
                }
            } else if (type == 5) {
                singleItem = !singleItem;
            }
            return;
        }

        boolean wasActive = getActive();
        super.handlePacketData(dataStream);

        if (world.isRemote) {
            int type = dataStream.readInt();

            if (type == 0) {
                readState(dataStream);
                readFilters(dataStream);
            } else if (type == 1) {
                readState(dataStream);
            } else if (type == 2) {
                readFilters(dataStream);
            }
            if (wasActive != getActive()) {
                //TileEntityEffectsBlock only updates it if it was not recently turned off.
                // (This is soo that lighting updates do not cause lag)
                // The sorter gets toggled a lot we need to make sure to update it anyways
                // so that the light on the side of it (the texture) updates properly.
                // We do not need to worry about block lighting updates causing lag as
                // #lightUpdate() returns false meaning that logistical sorters do not give
                // off actual light.
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    private void readState(PacketBuffer dataStream) {
        int c = dataStream.readInt();
        if (c != -1) {
            color = TransporterUtils.colors.get(c);
        } else {
            color = null;
        }
        autoEject = dataStream.readBoolean();
        roundRobin = dataStream.readBoolean();
        singleItem = dataStream.readBoolean();
    }

    private void readFilters(PacketBuffer dataStream) {
        filters.clear();
        int amount = dataStream.readInt();
        for (int i = 0; i < amount; i++) {
            filters.add(TransporterFilter.readFromPacket(dataStream));
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(0);
        if (color != null) {
            data.add(TransporterUtils.colors.indexOf(color));
        } else {
            data.add(-1);
        }

        data.add(autoEject);
        data.add(roundRobin);
        data.add(singleItem);

        data.add(filters.size());
        for (TransporterFilter filter : filters) {
            filter.write(data);
        }
        return data;
    }

    public TileNetworkList getGenericPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(1);
        if (color != null) {
            data.add(TransporterUtils.colors.indexOf(color));
        } else {
            data.add(-1);
        }

        data.add(autoEject);
        data.add(roundRobin);
        data.add(singleItem);
        return data;
    }

    @Override
    public TileNetworkList getFilterPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(2);
        data.add(filters.size());
        for (TransporterFilter filter : filters) {
            filter.write(data);
        }
        return data;
    }

    public boolean canSendHome(ItemStack stack) {
        TileEntity back = Coord4D.get(this).offset(getOppositeDirection()).getTileEntity(world);
        return InventoryUtils.canInsert(back, null, stack, getOppositeDirection(), true);
    }

    public boolean hasConnectedInventory() {
        TileEntity tile = Coord4D.get(this).offset(getOppositeDirection()).getTileEntity(world);
        return TransporterUtils.isValidAcceptorOnSide(tile, getOppositeDirection());
    }

    public TransitResponse sendHome(ItemStack stack) {
        TileEntity back = Coord4D.get(this).offset(getOppositeDirection()).getTileEntity(world);
        return InventoryUtils.putStackInInventory(back, TransitRequest.getFromStack(stack), getOppositeDirection(), true);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side == getDirection() || side == getOppositeDirection()) {
            return new int[]{0};
        }
        return InventoryUtils.EMPTY;
    }

    @Override
    public void openInventory(@Nonnull PlayerEntity player) {
        if (!world.isRemote) {
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        if (color != null) {
            nbtTags.putInt("color", TransporterUtils.colors.indexOf(color));
        }
        nbtTags.putBoolean("autoEject", autoEject);
        nbtTags.putBoolean("roundRobin", roundRobin);
        nbtTags.putBoolean("singleItem", singleItem);
        nbtTags.putInt("rrIndex", rrIndex);

        ListNBT filterTags = new ListNBT();
        for (TransporterFilter filter : filters) {
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
        if (nbtTags.contains("color")) {
            color = TransporterUtils.colors.get(nbtTags.getInt("color"));
        }
        autoEject = nbtTags.getBoolean("autoEject");
        roundRobin = nbtTags.getBoolean("roundRobin");
        singleItem = nbtTags.getBoolean("singleItem");
        rrIndex = nbtTags.getInt("rrIndex");

        if (nbtTags.contains("filters")) {
            ListNBT tagList = nbtTags.getList("filters", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(TransporterFilter.readFromNBT(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey();
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setBoolean(itemStack, "hasSorterConfig", true);
        if (color != null) {
            ItemDataUtils.setInt(itemStack, "color", TransporterUtils.colors.indexOf(color));
        }

        ItemDataUtils.setBoolean(itemStack, "autoEject", autoEject);
        ItemDataUtils.setBoolean(itemStack, "roundRobin", roundRobin);
        ItemDataUtils.setBoolean(itemStack, "singleItem", singleItem);

        ListNBT filterTags = new ListNBT();
        for (TransporterFilter filter : filters) {
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
        if (ItemDataUtils.hasData(itemStack, "hasSorterConfig")) {
            if (ItemDataUtils.hasData(itemStack, "color")) {
                color = TransporterUtils.colors.get(ItemDataUtils.getInt(itemStack, "color"));
            }
            autoEject = ItemDataUtils.getBoolean(itemStack, "autoEject");
            roundRobin = ItemDataUtils.getBoolean(itemStack, "roundRobin");
            singleItem = ItemDataUtils.getBoolean(itemStack, "singleItem");
            if (ItemDataUtils.hasData(itemStack, "filters")) {
                ListNBT tagList = ItemDataUtils.getList(itemStack, "filters");
                for (int i = 0; i < tagList.size(); i++) {
                    filters.add(TransporterFilter.readFromNBT(tagList.getCompound(i)));
                }
            }
        }
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (arguments.length > 0) {
            if (method == 0) {
                if (!(arguments[0] instanceof String)) {
                    return new Object[]{"Invalid parameters."};
                }
                color = EnumColor.getFromDyeName((String) arguments[0]);
                if (color == null) {
                    return new Object[]{"Default color set to null"};
                }
                return new Object[]{"Default color set to " + color.dyeName};
            } else if (method == 1) {
                if (!(arguments[0] instanceof Boolean)) {
                    return new Object[]{"Invalid parameters."};
                }
                roundRobin = (Boolean) arguments[0];
                return new Object[]{"Round-robin mode set to " + roundRobin};
            } else if (method == 2) {
                if (!(arguments[0] instanceof Boolean)) {
                    return new Object[]{"Invalid parameters."};
                }
                autoEject = (Boolean) arguments[0];
                return new Object[]{"Auto-eject mode set to " + autoEject};
            } else if (method == 3) {
                if (arguments.length != 5 || !(arguments[0] instanceof String) || !(arguments[1] instanceof String) || !(arguments[2] instanceof Boolean) ||
                    !(arguments[3] instanceof Double) || !(arguments[4] instanceof Double)) {
                    return new Object[]{"Invalid parameters."};
                }
                TItemStackFilter filter = new TItemStackFilter();
                filter.setItemStack(new ItemStack(ItemRegistryUtils.getByName((String) arguments[0])));
                filter.color = EnumColor.getFromDyeName((String) arguments[1]);
                filter.sizeMode = (Boolean) arguments[2];
                filter.min = ((Double) arguments[3]).intValue();
                filter.max = ((Double) arguments[4]).intValue();
                filters.add(filter);
                return new Object[]{"Added filter."};
            } else if (method == 4) {
                if (arguments.length != 1 || !(arguments[0] instanceof String)) {
                    return new Object[]{"Invalid parameters."};
                }
                ItemStack stack = new ItemStack(ItemRegistryUtils.getByName((String) arguments[0]));
                Iterator<TransporterFilter> iter = filters.iterator();
                while (iter.hasNext()) {
                    TransporterFilter filter = iter.next();
                    if (filter instanceof TItemStackFilter) {
                        if (StackUtils.equalsWildcard(((TItemStackFilter) filter).getItemStack(), stack)) {
                            iter.remove();
                            return new Object[]{"Removed filter."};
                        }
                    }
                }
                return new Object[]{"Couldn't find filter."};
            } else if (method == 5) {
                if (arguments.length != 2 || !(arguments[0] instanceof String) || !(arguments[1] instanceof String)) {
                    return new Object[]{"Invalid parameters."};
                }
                TOreDictFilter filter = new TOreDictFilter();
                filter.setOreDictName((String) arguments[0]);
                filter.color = EnumColor.getFromDyeName((String) arguments[1]);
                filters.add(filter);
                return new Object[]{"Added filter."};
            } else if (method == 6) {
                if (arguments.length != 1 || !(arguments[0] instanceof String)) {
                    return new Object[]{"Invalid parameters."};
                }
                String ore = (String) arguments[0];
                Iterator<TransporterFilter> iter = filters.iterator();
                while (iter.hasNext()) {
                    TransporterFilter filter = iter.next();
                    if (filter instanceof TOreDictFilter) {
                        if (((TOreDictFilter) filter).getOreDictName().equals(ore)) {
                            iter.remove();
                            return new Object[]{"Removed filter."};
                        }
                    }
                }
                return new Object[]{"Couldn't find filter."};
            } else if (method == 7) {
                if (!(arguments[0] instanceof Boolean)) {
                    return new Object[]{"Invalid parameters."};
                }
                singleItem = (Boolean) arguments[0];
                return new Object[]{"Single-item mode set to " + singleItem};
            }
        }

        for (PlayerEntity player : playersUsing) {
            Mekanism.packetHandler.sendTo(new PacketTileEntity(this, getGenericPacket(new TileNetworkList())), (ServerPlayerEntity) player);
        }
        return null;
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
            return side != null && side != getDirection() && side != getOppositeDirection();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgradeType) {

    }

    @Override
    public int getRedstoneLevel() {
        return getActive() ? 15 : 0;
    }

    @Override
    public HashList<TransporterFilter> getFilters() {
        return filters;
    }

    private class StrictFilterFinder extends Finder {

        @Override
        public boolean modifies(ItemStack stack) {
            for (TransporterFilter filter : filters) {
                if (filter.canFilter(stack, false) && !filter.allowDefault) {
                    return false;
                }
            }
            return true;
        }
    }
}