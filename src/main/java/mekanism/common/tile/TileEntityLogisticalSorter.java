package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
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
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLogisticalSorter extends TileEntityMekanism implements ISustainedData, ITileFilterHolder<SorterFilter<?>>, IHasSortableFilters {

    private HashList<SorterFilter<?>> filters = new HashList<>();
    private final Finder strictFinder = stack -> filters.stream().noneMatch(filter -> !filter.allowDefault && filter.getFinder().modifies(stack));

    @SyntheticComputerMethod(getter = "getDefaultColor")
    public EnumColor color;
    private boolean autoEject;
    private boolean roundRobin;
    private boolean singleItem;
    @Nullable
    public SidedBlockPos rrTarget;
    private int delayTicks;

    public TileEntityLogisticalSorter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LOGISTICAL_SORTER, pos, state);
        delaySupplier = () -> 3;
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(InternalInventorySlot.create(listener), RelativeSide.FRONT);
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
            Direction direction = getDirection();
            BlockEntity back = WorldUtils.getTileEntity(getLevel(), worldPosition.relative(direction.getOpposite()));
            BlockEntity front = WorldUtils.getTileEntity(getLevel(), worldPosition.relative(direction));
            //If there is no tile to pull from or the push to, skip doing any checks
            if (InventoryUtils.isItemHandler(back, direction) && front != null) {
                boolean sentItems = false;
                for (SorterFilter<?> filter : filters) {
                    TransitRequest request = filter.mapInventory(back, direction, singleItem);
                    if (request.isEmpty()) {
                        continue;
                    }
                    int min = singleItem ? 1 : filter.sizeMode ? filter.min : 0;
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
                    TransitRequest request = TransitRequest.definedItem(back, direction, singleItem ? 1 : 64, strictFinder);
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

    private TransitResponse emitItemToTransporter(BlockEntity front, TransitRequest request, EnumColor filterColor, int min) {
        if (front instanceof TileEntityLogisticalTransporterBase transporterBase) {
            LogisticalTransporterBase transporter = transporterBase.getTransmitter();
            if (roundRobin) {
                return transporter.insertRR(this, request, filterColor, true, min);
            }
            return transporter.insert(this, request, filterColor, true, min);
        }
        return request.addToInventory(front, getDirection(), min, false);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        if (rrTarget != null) {
            nbtTags.put(NBTConstants.ROUND_ROBIN_TARGET, rrTarget.serialize());
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(NBTConstants.ROUND_ROBIN_TARGET, Tag.TAG_COMPOUND)) {
            rrTarget = SidedBlockPos.deserialize(nbt.getCompound(NBTConstants.ROUND_ROBIN_TARGET));
        }
    }

    @Override
    public void moveUp(int filterIndex) {
        filters.swap(filterIndex, filterIndex - 1);
        markForSave();
    }

    @Override
    public void moveDown(int filterIndex) {
        filters.swap(filterIndex, filterIndex + 1);
        markForSave();
    }

    @ComputerMethod(nameOverride = "getAutoMode")
    public boolean getAutoEject() {
        return autoEject;
    }

    @ComputerMethod(nameOverride = "isRoundRobin")
    public boolean getRoundRobin() {
        return roundRobin;
    }

    @ComputerMethod(nameOverride = "isSingle")
    public boolean getSingleItem() {
        return singleItem;
    }

    public void toggleAutoEject() {
        autoEject = !autoEject;
        markForSave();
    }

    public void toggleRoundRobin() {
        roundRobin = !roundRobin;
        rrTarget = null;
        markForSave();
    }

    public void toggleSingleItem() {
        singleItem = !singleItem;
        markForSave();
    }

    public void changeColor(@Nullable EnumColor color) {
        if (this.color != color) {
            this.color = color;
            markForSave();
        }
    }

    public boolean canSendHome(ItemStack stack) {
        Direction oppositeDirection = getOppositeDirection();
        BlockEntity back = WorldUtils.getTileEntity(getLevel(), worldPosition.relative(oppositeDirection));
        return TransporterUtils.canInsert(back, null, stack, oppositeDirection, true);
    }

    public boolean hasConnectedInventory() {
        Direction oppositeDirection = getOppositeDirection();
        BlockEntity tile = WorldUtils.getTileEntity(getLevel(), worldPosition.relative(oppositeDirection));
        return TransporterUtils.isValidAcceptorOnSide(tile, oppositeDirection);
    }

    @Nonnull
    public TransitResponse sendHome(TransitRequest request) {
        Direction oppositeDirection = getOppositeDirection();
        BlockEntity back = WorldUtils.getTileEntity(getLevel(), worldPosition.relative(oppositeDirection));
        return request.addToInventory(back, oppositeDirection, 0, true);
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        dataMap.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
        dataMap.putBoolean(NBTConstants.EJECT, autoEject);
        dataMap.putBoolean(NBTConstants.ROUND_ROBIN, roundRobin);
        dataMap.putBoolean(NBTConstants.SINGLE_ITEM, singleItem);
        if (!filters.isEmpty()) {
            ListTag filterTags = new ListTag();
            for (SorterFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundTag()));
            }
            dataMap.put(NBTConstants.FILTERS, filterTags);
        }
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        NBTUtils.setEnumIfPresent(dataMap, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
        autoEject = dataMap.getBoolean(NBTConstants.EJECT);
        roundRobin = dataMap.getBoolean(NBTConstants.ROUND_ROBIN);
        singleItem = dataMap.getBoolean(NBTConstants.SINGLE_ITEM);
        filters.clear();
        NBTUtils.setListIfPresent(dataMap, NBTConstants.FILTERS, Tag.TAG_COMPOUND, tagList -> {
            for (int i = 0, size = tagList.size(); i < size; i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof SorterFilter<?> sorterFilter) {
                    filters.add(sorterFilter);
                }
            }
        });
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
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        return false;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        //We don't cache the redstone level for the logistical sorter
        return getRedstoneLevel();
    }

    @Override
    @ComputerMethod
    public HashList<SorterFilter<?>> getFilters() {
        return filters;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getAutoEject, value -> autoEject = value));
        container.track(SyncableBoolean.create(this::getRoundRobin, value -> roundRobin = value));
        container.track(SyncableBoolean.create(this::getSingleItem, value -> singleItem = value));
        container.track(SyncableInt.create(() -> TransporterUtils.getColorIndex(color), value -> color = TransporterUtils.readColor(value)));
        container.track(SyncableFilterList.create(this::getFilters, value -> {
            if (value instanceof HashList<SorterFilter<?>> filters) {
                this.filters = filters;
            } else {
                this.filters = new HashList<>(value);
            }
        }));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private void setSingle(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (singleItem != value) {
            toggleSingleItem();
        }
    }

    @ComputerMethod
    private void setRoundRobin(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (roundRobin != value) {
            toggleRoundRobin();
        }
    }

    @ComputerMethod
    private void setAutoMode(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (autoEject != value) {
            toggleAutoEject();
        }
    }

    @ComputerMethod
    private void clearDefaultColor() throws ComputerException {
        validateSecurityIsPublic();
        changeColor(null);
    }

    @ComputerMethod
    private void incrementDefaultColor() throws ComputerException {
        validateSecurityIsPublic();
        color = TransporterUtils.increment(color);
        markForSave();
    }

    @ComputerMethod
    private void decrementDefaultColor() throws ComputerException {
        validateSecurityIsPublic();
        color = TransporterUtils.decrement(color);
        markForSave();
    }

    @ComputerMethod
    private void setDefaultColor(EnumColor color) throws ComputerException {
        validateSecurityIsPublic();
        if (!TransporterUtils.colors.contains(color)) {
            throw new ComputerException("Color '%s' is not a supported transporter color.", color);
        }
        changeColor(color);
    }

    @ComputerMethod
    private boolean addFilter(SorterFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filters.add(filter);
    }

    @ComputerMethod
    private boolean removeFilter(SorterFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filters.remove(filter);
    }
    //End methods IComputerTile
}