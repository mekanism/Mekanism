package mekanism.common.tile;

import java.util.Collection;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.SortableFilterManager;
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
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityLogisticalSorter extends TileEntityMekanism implements ITileFilterHolder<SorterFilter<?>>, IAdvancedTransportEjector {

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
    private SidedBlockPos rrTarget;
    private int delayTicks;
    private long nextSound = 0;

    public TileEntityLogisticalSorter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LOGISTICAL_SORTER, pos, state);
        delaySupplier = () -> 3;
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        //TODO - 1.20.4: Re-evaluate the internal inventory slot and why do we even have a slot on the sorter
        builder.addSlot(InternalInventorySlot.create(listener), RelativeSide.FRONT);
        return builder.build();
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Note: We don't persist items because the slot we have is only actually for the transporters to connect visually
        return type != ContainerType.ITEM && super.persists(type);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        delayTicks = Math.max(0, delayTicks - 1);
        if (delayTicks == 6) {
            setActive(false);
        }

        if (canFunction() && delayTicks == 0) {
            IItemHandler back = getHomeInventory();
            //If there is no tile to pull from or the push to, skip doing any checks
            if (back != null) {
                Direction direction = getDirection();
                if (targetInventory == null) {
                    targetInventory = Capabilities.ITEM.createCache((ServerLevel) level, worldPosition.relative(direction), direction.getOpposite());
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
                        TransitResponse response = emitItemToTransporter(frontCap, request, filter.color, min);
                        if (!response.isEmpty()) {
                            response.useAll();
                            setActive(true);
                            sentItems = true;
                            break;
                        }
                    }

                    if (!sentItems && autoEject) {
                        //TODO - 1.21: Evaluate if this (and SorterFilter#mapInventory) should use a stack's max stack size or the absolute stack size
                        TransitRequest request = TransitRequest.definedItem(back, singleItem ? 1 : Item.ABSOLUTE_MAX_STACK_SIZE, strictFinder);
                        TransitResponse response = emitItemToTransporter(frontCap, request, color, 0);
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

    private TransitResponse emitItemToTransporter(IItemHandler target, TransitRequest request, EnumColor filterColor, int min) {
        if (request.isEmpty()) {
            return request.getEmptyResponse();
        } else if (target instanceof CursedTransporterItemHandler cursed) {
            return cursed.getTransporter().insertMaybeRR(this, getBlockPos(), request, filterColor, true, min);
        }
        return request.addToInventoryUnchecked(target, min);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        SidedBlockPos rrTarget = getRoundRobinTarget();
        if (rrTarget != null) {
            nbtTags.put(SerializationConstants.ROUND_ROBIN_TARGET, rrTarget.serialize());
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        if (nbt.contains(SerializationConstants.ROUND_ROBIN_TARGET, Tag.TAG_COMPOUND)) {
            setRoundRobinTarget(SidedBlockPos.deserialize(nbt.getCompound(SerializationConstants.ROUND_ROBIN_TARGET)));
        }
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(SerializationConstants.ROUND_ROBIN_TARGET);
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
            nextSound = level.getGameTime() + (long) SharedConstants.TICKS_PER_SECOND * level.random.nextInt(5, 15);
        }
    }

    @ComputerMethod(nameOverride = "getAutoMode")
    public boolean getAutoEject() {
        return autoEject;
    }

    @Override
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

    @Override
    public void toggleRoundRobin() {
        roundRobin = !roundRobin;
        setRoundRobinTarget((SidedBlockPos) null);
        markForSave();
    }

    @Nullable
    @Override
    public SidedBlockPos getRoundRobinTarget() {
        return rrTarget;
    }

    @Override
    public void setRoundRobinTarget(@Nullable SidedBlockPos target) {
        rrTarget = target;
    }

    @Override
    public boolean canSendHome(@NotNull ItemStack stack) {
        Direction oppositeDirection = getOppositeDirection();
        return TransporterUtils.canInsert(level, worldPosition.relative(oppositeDirection), null, stack, oppositeDirection, true);
    }

    @NotNull
    @Override
    public TransitResponse sendHome(@NotNull TransitRequest request) {
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
    protected WrenchResult tryWrenchRotate(BlockState state, Player player, ItemStack stack) {
        Direction change = MekanismUtils.rotate(getDirection(), true);
        if (!hasConnectedInventory()) {
            for (Direction dir : EnumUtils.DIRECTIONS) {
                Direction opposite = dir.getOpposite();
                if (InventoryUtils.isItemHandler(level, worldPosition.relative(dir), opposite)) {
                    change = opposite;
                    break;
                }
            }
        }
        setFacing(change);
        level.updateNeighborsAt(worldPosition, state.getBlock());
        return WrenchResult.SUCCESS;
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.writeSustainedData(provider, dataMap);
        if (color != null) {
            NBTUtils.writeEnum(dataMap, SerializationConstants.COLOR, color);
        }
        dataMap.putBoolean(SerializationConstants.EJECT, autoEject);
        dataMap.putBoolean(SerializationConstants.ROUND_ROBIN, roundRobin);
        dataMap.putBoolean(SerializationConstants.SINGLE_ITEM, singleItem);
        filterManager.writeToNBT(provider, dataMap);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.readSustainedData(provider, dataMap);
        this.color = NBTUtils.getEnum(dataMap, SerializationConstants.COLOR, EnumColor.BY_ID);
        autoEject = dataMap.getBoolean(SerializationConstants.EJECT);
        roundRobin = dataMap.getBoolean(SerializationConstants.ROUND_ROBIN);
        singleItem = dataMap.getBoolean(SerializationConstants.SINGLE_ITEM);
        filterManager.readFromNBT(provider, dataMap);
    }

    @Override
    public List<DataComponentType<?>> getRemapEntries() {
        List<DataComponentType<?>> remapEntries = super.getRemapEntries();
        remapEntries.add(MekanismDataComponents.COLOR.get());
        return remapEntries;
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (color != null) {
            builder.set(MekanismDataComponents.COLOR, color);
        }
        builder.set(MekanismDataComponents.EJECT, autoEject);
        builder.set(MekanismDataComponents.ROUND_ROBIN, roundRobin);
        builder.set(MekanismDataComponents.SINGLE_ITEM, singleItem);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        color = input.get(MekanismDataComponents.COLOR);
        autoEject = input.getOrDefault(MekanismDataComponents.EJECT, autoEject);
        roundRobin = input.getOrDefault(MekanismDataComponents.ROUND_ROBIN, roundRobin);
        singleItem = input.getOrDefault(MekanismDataComponents.SINGLE_ITEM, singleItem);
    }

    @Override
    public int getRedstoneLevel() {
        return getActive() ? Redstone.SIGNAL_MAX : Redstone.SIGNAL_NONE;
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