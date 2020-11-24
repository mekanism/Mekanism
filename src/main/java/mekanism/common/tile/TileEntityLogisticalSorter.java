package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.lib.HashList;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityLogisticalSorter extends TileEntityMekanism implements ISpecialConfigData, ISustainedData, ITileFilterHolder<SorterFilter<?>>,
      IHasSortableFilters {

    private HashList<SorterFilter<?>> filters = new HashList<>();
    private final Finder strictFinder = stack -> filters.stream().noneMatch(filter -> !filter.allowDefault && filter.getFinder().modifies(stack));

    public EnumColor color;
    public boolean autoEject;
    public boolean roundRobin;
    public boolean singleItem;
    public int rrIndex = 0;
    private int delayTicks;

    public TileEntityLogisticalSorter() {
        super(MekanismBlocks.LOGISTICAL_SORTER);
        delaySupplier = () -> 3;
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(InternalInventorySlot.create(this), RelativeSide.FRONT, RelativeSide.BACK);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        delayTicks = Math.max(0, delayTicks - 1);
        if (delayTicks == 6) {
            setActive(false);
        }

        if (MekanismUtils.canFunction(this) && delayTicks == 0) {
            TileEntity back = WorldUtils.getTileEntity(getWorld(), pos.offset(getOppositeDirection()));
            TileEntity front = WorldUtils.getTileEntity(getWorld(), pos.offset(getDirection()));
            //If there is no tile to pull from or the push to, skip doing any checks
            if (InventoryUtils.isItemHandler(back, getDirection()) && front != null) {
                boolean sentItems = false;
                int min = 0;

                for (SorterFilter<?> filter : filters) {
                    TransitRequest request = filter.mapInventory(back, getDirection(), singleItem);
                    if (request.isEmpty()) {
                        continue;
                    }
                    if (!singleItem && filter instanceof SorterItemStackFilter) {
                        SorterItemStackFilter itemFilter = (SorterItemStackFilter) filter;
                        if (itemFilter.sizeMode) {
                            min = itemFilter.min;
                        }
                    }
                    TransitResponse response = emitItemToTransporter(front, request, filter.color, min);
                    if (!response.isEmpty()) {
                        response.useAll();
                        WorldUtils.saveChunk(back);
                        setActive(true);
                        sentItems = true;
                        break;
                    }
                }

                if (!sentItems && autoEject) {
                    TransitRequest request = TransitRequest.definedItem(back, getDirection(), singleItem ? 1 : 64, strictFinder);
                    TransitResponse response = emitItemToTransporter(front, request, color, 0);
                    if (!response.isEmpty()) {
                        response.useAll();
                        WorldUtils.saveChunk(back);
                        setActive(true);
                    }
                }
            }
            delayTicks = 10;
        }
    }

    private TransitResponse emitItemToTransporter(TileEntity front, TransitRequest request, EnumColor filterColor, int min) {
        if (front instanceof TileEntityLogisticalTransporterBase) {
            LogisticalTransporterBase transporter = ((TileEntityLogisticalTransporterBase) front).getTransmitter();
            if (roundRobin) {
                return transporter.insertRR(this, request, filterColor, true, min);
            }
            return transporter.insert(this, request, filterColor, true, min);
        }
        return request.addToInventory(front, getDirection(), false);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        return getConfigurationData(nbtTags);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        setConfigurationData(nbtTags);
    }

    @Override
    public void moveUp(int filterIndex) {
        filters.swap(filterIndex, filterIndex - 1);
        markDirty(false);
    }

    @Override
    public void moveDown(int filterIndex) {
        filters.swap(filterIndex, filterIndex + 1);
        markDirty(false);
    }

    public void toggleAutoEject() {
        autoEject = !autoEject;
        markDirty(false);
    }

    public void toggleRoundRobin() {
        roundRobin = !roundRobin;
        rrIndex = 0;
        markDirty(false);
    }

    public void toggleSingleItem() {
        singleItem = !singleItem;
        markDirty(false);
    }

    public void changeColor(@Nullable EnumColor color) {
        this.color = color;
        markDirty(false);
    }

    public boolean canSendHome(ItemStack stack) {
        TileEntity back = WorldUtils.getTileEntity(getWorld(), pos.offset(getOppositeDirection()));
        return TransporterUtils.canInsert(back, null, stack, getOppositeDirection(), true);
    }

    public boolean hasConnectedInventory() {
        TileEntity tile = WorldUtils.getTileEntity(getWorld(), pos.offset(getOppositeDirection()));
        return TransporterUtils.isValidAcceptorOnSide(tile, getOppositeDirection());
    }

    @Nonnull
    public TransitResponse sendHome(TransitRequest request) {
        TileEntity back = WorldUtils.getTileEntity(getWorld(), pos.offset(getOppositeDirection()));
        return request.addToInventory(back, getOppositeDirection(), true);
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
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
        nbtTags.putBoolean(NBTConstants.EJECT, autoEject);
        nbtTags.putBoolean(NBTConstants.ROUND_ROBIN, roundRobin);
        nbtTags.putBoolean(NBTConstants.SINGLE_ITEM, singleItem);
        nbtTags.putInt(NBTConstants.INDEX, rrIndex);
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (SorterFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            nbtTags.put(NBTConstants.FILTERS, filterTags);
        }
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
        autoEject = nbtTags.getBoolean(NBTConstants.EJECT);
        roundRobin = nbtTags.getBoolean(NBTConstants.ROUND_ROBIN);
        singleItem = nbtTags.getBoolean(NBTConstants.SINGLE_ITEM);
        rrIndex = nbtTags.getInt(NBTConstants.INDEX);
        if (nbtTags.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof SorterFilter) {
                    filters.add((SorterFilter<?>) filter);
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
        ItemDataUtils.setInt(itemStack, NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
        ItemDataUtils.setBoolean(itemStack, NBTConstants.EJECT, autoEject);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.ROUND_ROBIN, roundRobin);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.SINGLE_ITEM, singleItem);
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (SorterFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            ItemDataUtils.setList(itemStack, NBTConstants.FILTERS, filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, NBTConstants.COLOR, NBT.TAG_INT)) {
            color = TransporterUtils.readColor(ItemDataUtils.getInt(itemStack, NBTConstants.COLOR));
        }
        autoEject = ItemDataUtils.getBoolean(itemStack, NBTConstants.EJECT);
        roundRobin = ItemDataUtils.getBoolean(itemStack, NBTConstants.ROUND_ROBIN);
        singleItem = ItemDataUtils.getBoolean(itemStack, NBTConstants.SINGLE_ITEM);
        if (ItemDataUtils.hasData(itemStack, NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = ItemDataUtils.getList(itemStack, NBTConstants.FILTERS);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof SorterFilter) {
                    filters.add((SorterFilter<?>) filter);
                }
            }
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.COLOR, NBTConstants.COLOR);
        remap.put(NBTConstants.EJECT, NBTConstants.EJECT);
        remap.put(NBTConstants.ROUND_ROBIN, NBTConstants.ROUND_ROBIN);
        remap.put(NBTConstants.SINGLE_ITEM, NBTConstants.SINGLE_ITEM);
        remap.put(NBTConstants.FILTERS, NBTConstants.FILTERS);
        return remap;
    }

    @Override
    public int getRedstoneLevel() {
        return getActive() ? 15 : 0;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        //We don't cache the redstone level for the logistical sorter
        return getRedstoneLevel();
    }

    @Override
    public HashList<SorterFilter<?>> getFilters() {
        return filters;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> autoEject, value -> autoEject = value));
        container.track(SyncableBoolean.create(() -> roundRobin, value -> roundRobin = value));
        container.track(SyncableBoolean.create(() -> singleItem, value -> singleItem = value));
        container.track(SyncableInt.create(() -> TransporterUtils.getColorIndex(color), value -> color = TransporterUtils.readColor(value)));
        container.track(SyncableFilterList.create(this::getFilters, value -> {
            if (value instanceof HashList) {
                filters = (HashList<SorterFilter<?>>) value;
            } else {
                filters = new HashList<>(value);
            }
        }));
    }
}