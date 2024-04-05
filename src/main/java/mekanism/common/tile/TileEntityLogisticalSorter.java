package mekanism.common.tile;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityLogisticalSorter extends TileEntityMekanism implements ITileFilterHolder<SorterFilter<?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final SortableFilterManager<SorterFilter<?>> filterManager = new SortableFilterManager<SorterFilter<?>>((Class) SorterFilter.class, this::markForSave);
    private final Finder strictFinder = stack -> {
        for (SorterFilter<?> filter : filterManager.getEnabledFilters()) {
            if (!filter.allowDefault && filter.getFinder().test(stack)) {
                return false;
            }
        }
        return true;
    };

    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> homeInventory;
    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> targetInventory;

    @SyntheticComputerMethod(getter = "getDefaultColor")
    public EnumColor color;
    private boolean autoEject;
    private boolean roundRobin;
    private boolean singleItem;
    @Nullable
    public SidedBlockPos rrTarget;
    private int delayTicks;
    private long nextSound = 0;

    public TileEntityLogisticalSorter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LOGISTICAL_SORTER, pos, state);
        delaySupplier = () -> 3;
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO - 1.20.4: Re-evaluate the internal inventory slot and why do we even have a slot on the sorter
        builder.addSlot(InternalInventorySlot.create(listener), RelativeSide.FRONT);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        delayTicks = Math.max(0, delayTicks - 1);
        if (delayTicks == 6) {
            setActive(false);
        }

        if (MekanismUtils.canFunction(this) && delayTicks == 0) {
            IItemHandler back = getHomeInventory();
            //If there is no tile to pull from or the push to, skip doing any checks
            if (back != null) {
                Direction direction = getDirection();
                BlockPos frontPos = worldPosition.relative(direction);
                Lazy<BlockEntity> front = Lazy.of(() -> WorldUtils.getTileEntity(getLevel(), frontPos));
                if (targetInventory == null) {
                    targetInventory = Capabilities.ITEM.createCache((ServerLevel) level, frontPos, direction.getOpposite());
                }
                IItemHandler frontCap = targetInventory.getCapability();
                if (frontCap != null) {
                    boolean sentItems = false;
                    for (SorterFilter<?> filter : filterManager.getEnabledFilters()) {
                        TransitRequest request = filter.mapInventory(back, singleItem);
                        if (request.isEmpty()) {
                            continue;
                        }
                        int min = singleItem ? 1 : filter.sizeMode ? filter.min : 0;
                        TransitResponse response = emitItemToTransporter(frontCap, front, request, filter.color, min);
                        if (!response.isEmpty()) {
                            response.useAll();
                            setActive(true);
                            sentItems = true;
                            break;
                        }
                    }

                    if (!sentItems && autoEject) {
                        TransitRequest request = TransitRequest.definedItem(back, singleItem ? 1 : 64, strictFinder);
                        TransitResponse response = emitItemToTransporter(frontCap, front, request, color, 0);
                        if (!response.isEmpty()) {
                            response.useAll();
                            setActive(true);
                        }
                    }
                }
            }
            delayTicks = MekanismUtils.TICKS_PER_HALF_SECOND;
        }
        return sendUpdatePacket;
    }

    private TransitResponse emitItemToTransporter(IItemHandler target, Supplier<BlockEntity> front, TransitRequest request, EnumColor filterColor, int min) {
        if (request.isEmpty()) {
            return request.getEmptyResponse();
        } else if (target instanceof CursedTransporterItemHandler) {
            if (front.get() instanceof TileEntityLogisticalTransporterBase transporterBase) {
                LogisticalTransporterBase transporter = transporterBase.getTransmitter();
                if (roundRobin) {
                    return transporter.insertRR(this, request, filterColor, true, min);
                }
                return transporter.insert(this, getBlockPos(), request, filterColor, true, min);
            }
            return request.getEmptyResponse();
        }
        return request.addToInventoryUnchecked(target, min);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        if (rrTarget != null) {
            nbtTags.put(NBTConstants.ROUND_ROBIN_TARGET, rrTarget.serialize());
        }
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(NBTConstants.ROUND_ROBIN_TARGET, Tag.TAG_COMPOUND)) {
            rrTarget = SidedBlockPos.deserialize(nbt.getCompound(NBTConstants.ROUND_ROBIN_TARGET));
        }
    }

    @Override
    protected boolean canPlaySound() {
        return false;//handle own sounds
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (MekanismConfig.client.enableMachineSounds.get() && getActive() && soundEvent != null && level.getGameTime() >= nextSound) {
            if (!isFullyMuffled()) {
                SoundHandler.startTileSound(soundEvent.get(), getSoundCategory(), getInitialVolume(), level.getRandom(), getSoundPos(), false);
            }
            nextSound = level.getGameTime() + 20L * (level.random.nextInt(5, 15));
        }
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
        return TransporterUtils.canInsert(level, worldPosition.relative(oppositeDirection), null, stack, oppositeDirection, true);
    }

    public boolean hasConnectedInventory() {
        Direction oppositeDirection = getOppositeDirection();
        return TransporterUtils.isValidAcceptorOnSide(getLevel(), worldPosition.relative(oppositeDirection), oppositeDirection);
    }

    @Nullable
    private IItemHandler getHomeInventory() {
        if (homeInventory == null) {
            Direction direction = getDirection();
            BlockPos pos = worldPosition.relative(direction.getOpposite());
            homeInventory = Capabilities.ITEM.createCache((ServerLevel) level, pos, direction);
        }
        return homeInventory.getCapability();
    }

    @Override
    protected void invalidateDirectionCaches(Direction newDirection) {
        super.invalidateDirectionCaches(newDirection);
        homeInventory = null;
        targetInventory = null;
    }

    @NotNull
    public TransitResponse sendHome(TransitRequest request) {
        Direction direction = getDirection();
        BlockPos pos = worldPosition.relative(direction.getOpposite());
        //Note: We pass false as we have no reason to allow daisy-chaining sorters given a sorter can't send from a sorter to another
        // and the only case would be if an inventory was replaced with another sorter connected to an inventory to proxy it back an extra spot
        return request.addToInventory(getLevel(), pos, getHomeInventory(), 0, false);
    }

    @Override
    public boolean supportsMode(RedstoneControl mode) {
        return true;
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        super.writeSustainedData(dataMap);
        if (color != null) {
            NBTUtils.writeEnum(dataMap, NBTConstants.COLOR, color);
        }
        dataMap.putBoolean(NBTConstants.EJECT, autoEject);
        dataMap.putBoolean(NBTConstants.ROUND_ROBIN, roundRobin);
        dataMap.putBoolean(NBTConstants.SINGLE_ITEM, singleItem);
        filterManager.writeToNBT(dataMap);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        super.readSustainedData(dataMap);
        this.color = NBTUtils.getEnum(dataMap, NBTConstants.COLOR, TransporterUtils::readColor);
        autoEject = dataMap.getBoolean(NBTConstants.EJECT);
        roundRobin = dataMap.getBoolean(NBTConstants.ROUND_ROBIN);
        singleItem = dataMap.getBoolean(NBTConstants.SINGLE_ITEM);
        filterManager.readFromNBT(dataMap);
    }

    @Override
    public Map<String, Holder<AttachmentType<?>>> getTileDataAttachmentRemap() {
        Map<String, Holder<AttachmentType<?>>> remap = super.getTileDataAttachmentRemap();
        remap.put(NBTConstants.COLOR, MekanismAttachmentTypes.TRANSPORTER_COLOR);
        remap.put(NBTConstants.EJECT, MekanismAttachmentTypes.EJECT);
        remap.put(NBTConstants.ROUND_ROBIN, MekanismAttachmentTypes.ROUND_ROBIN);
        remap.put(NBTConstants.SINGLE_ITEM, MekanismAttachmentTypes.SINGLE_ITEM);
        return remap;
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        stack.setData(MekanismAttachmentTypes.TRANSPORTER_COLOR, Optional.ofNullable(color));
        stack.setData(MekanismAttachmentTypes.EJECT, autoEject);
        stack.setData(MekanismAttachmentTypes.ROUND_ROBIN, roundRobin);
        stack.setData(MekanismAttachmentTypes.SINGLE_ITEM, singleItem);
    }

    @Override
    public void readFromStack(ItemStack stack) {
        super.readFromStack(stack);
        color = stack.getData(MekanismAttachmentTypes.TRANSPORTER_COLOR).orElse(null);
        autoEject = stack.getData(MekanismAttachmentTypes.EJECT);
        roundRobin = stack.getData(MekanismAttachmentTypes.ROUND_ROBIN);
        singleItem = stack.getData(MekanismAttachmentTypes.SINGLE_ITEM);
    }

    @Override
    public int getRedstoneLevel() {
        return getActive() ? 15 : 0;
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return false;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        //We don't cache the redstone level for the logistical sorter
        return getRedstoneLevel();
    }

    @Override
    public SortableFilterManager<SorterFilter<?>> getFilterManager() {
        return filterManager;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getAutoEject, value -> autoEject = value));
        container.track(SyncableBoolean.create(this::getRoundRobin, value -> roundRobin = value));
        container.track(SyncableBoolean.create(this::getSingleItem, value -> singleItem = value));
        container.track(SyncableInt.create(() -> TransporterUtils.getColorIndex(color), value -> color = TransporterUtils.readColor(value)));
        filterManager.addContainerTrackers(container);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void setSingle(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (singleItem != value) {
            toggleSingleItem();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setRoundRobin(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (roundRobin != value) {
            toggleRoundRobin();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setAutoMode(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (autoEject != value) {
            toggleAutoEject();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void clearDefaultColor() throws ComputerException {
        validateSecurityIsPublic();
        changeColor(null);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void incrementDefaultColor() throws ComputerException {
        validateSecurityIsPublic();
        color = TransporterUtils.increment(color);
        markForSave();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void decrementDefaultColor() throws ComputerException {
        validateSecurityIsPublic();
        color = TransporterUtils.decrement(color);
        markForSave();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setDefaultColor(EnumColor color) throws ComputerException {
        validateSecurityIsPublic();
        changeColor(color);
    }

    @ComputerMethod
    Collection<SorterFilter<?>> getFilters() {
        return filterManager.getFilters();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    boolean addFilter(SorterFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.addFilter(filter);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    boolean removeFilter(SorterFilter<?> filter) throws ComputerException {
        validateSecurityIsPublic();
        return filterManager.removeFilter(filter);
    }
    //End methods IComputerTile
}