package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.inventory.container.tile.DigitalMinerConfigContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.Region;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityDigitalMiner extends TileEntityMekanism implements ISustainedData, IChunkLoader, IBoundingBlock, ITileFilterHolder<MinerFilter<?>>,
      IHasSortableFilters {

    public Long2ObjectMap<BitSet> oresToMine = new Long2ObjectOpenHashMap<>();
    public Int2ObjectMap<MinerFilter<?>> replaceMap = new Int2ObjectOpenHashMap<>();
    private HashList<MinerFilter<?>> filters = new HashList<>();
    public ThreadMinerSearch searcher = new ThreadMinerSearch(this);

    private int radius;
    private boolean inverse;
    private int minY;
    private int maxY = 60;
    private boolean doEject = false;
    private boolean doPull = false;
    public ItemStack missingStack = ItemStack.EMPTY;
    public int delay;
    private int delayLength = MekanismConfig.general.minerTicksPerMine.get();
    public int cachedToMine;
    private boolean silkTouch;
    private boolean running;
    private int delayTicks;
    private boolean initCalc = false;
    private int numPowering;
    public boolean clientRendering = false;

    private final TileComponentChunkLoader<TileEntityDigitalMiner> chunkLoaderComponent = new TileComponentChunkLoader<>(this);

    private MinerEnergyContainer energyContainer;
    private List<IInventorySlot> mainSlots;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityDigitalMiner() {
        super(MekanismBlocks.DIGITAL_MINER);
        radius = 10;
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
        //Return some capabilities as disabled, and handle them with offset capabilities instead
        addDisabledCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MinerEnergyContainer.input(this), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        mainSlots = new ArrayList<>();
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection, side -> side == RelativeSide.TOP, side -> side == RelativeSide.BACK);
        //Allow insertion manually or internally, or if it is a replace stack
        BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert = (stack, automationType) -> automationType != AutomationType.EXTERNAL || isReplaceStack(stack);
        //Allow extraction if it is manual or if it is a replace stack
        BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract = (stack, automationType) -> automationType == AutomationType.MANUAL || !isReplaceStack(stack);
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                BasicInventorySlot slot = BasicInventorySlot.at(canExtract, canInsert, this, 8 + slotX * 18, 92 + slotY * 18);
                builder.addSlot(slot, RelativeSide.BACK, RelativeSide.TOP);
                mainSlots.add(slot);
            }
        }
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 152, 20));
        return builder.build();
    }

    private void closeInvalidScreens() {
        if (getActive() && !playersUsing.isEmpty()) {
            for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                if (player.containerMenu instanceof DigitalMinerConfigContainer) {
                    player.closeContainer();
                }
            }
        }
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        closeInvalidScreens();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        closeInvalidScreens();
        if (!initCalc) {
            if (searcher.state == State.FINISHED) {
                boolean prevRunning = running;
                reset();
                start();
                running = prevRunning;
            }
            initCalc = true;
        }

        energySlot.fillContainerOrConvert();

        if (MekanismUtils.canFunction(this) && running && searcher.state == State.FINISHED && !oresToMine.isEmpty()) {
            FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                setActive(true);
                if (delay > 0) {
                    delay--;
                }
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                if (delay == 0) {
                    boolean did = false;
                    for (LongIterator it = oresToMine.keySet().iterator(); it.hasNext(); ) {
                        long chunk = it.nextLong();
                        BitSet set = oresToMine.get(chunk);
                        int next = 0;
                        while (!did) {
                            int index = set.nextSetBit(next);
                            BlockPos pos = getPosFromIndex(index);
                            if (index == -1) {
                                it.remove();
                                break;
                            }
                            Optional<BlockState> blockState = WorldUtils.getBlockState(level, pos);
                            if (!blockState.isPresent() || blockState.get().isAir(level, pos)) {
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                    break;
                                }
                                next = index + 1;
                                continue;
                            }
                            boolean hasFilter = false;
                            BlockState state = blockState.get();
                            for (MinerFilter<?> filter : filters) {
                                if (filter.canFilter(state)) {
                                    hasFilter = true;
                                    break;
                                }
                            }

                            if (inverse == hasFilter || !canMine(pos)) {
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                    break;
                                }
                                next = index + 1;
                                continue;
                            }

                            List<ItemStack> drops = getDrops(state, pos);
                            if (canInsert(drops) && setReplace(pos, index)) {
                                did = true;
                                add(drops);
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                }
                                level.levelEvent(WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getId(state));
                                missingStack = ItemStack.EMPTY;
                            }
                            break;
                        }
                    }
                    delay = getDelay();
                    //Update the cached to mine value now that we have actually performed a mine
                    updateCachedToMine();
                }
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }

        if (doEject && delayTicks == 0) {
            Direction oppositeDirection = getOppositeDirection();
            TileEntity ejectInv = WorldUtils.getTileEntity(level, getBlockPos().above().relative(oppositeDirection, 2));
            TileEntity ejectTile = WorldUtils.getTileEntity(getLevel(), getBlockPos().above().relative(oppositeDirection));
            if (ejectInv != null && ejectTile != null) {
                TransitRequest ejectMap = getEjectItemMap(ejectTile, oppositeDirection);
                if (!ejectMap.isEmpty()) {
                    TransitResponse response;
                    if (ejectInv instanceof TileEntityLogisticalTransporterBase) {
                        response = ((TileEntityLogisticalTransporterBase) ejectInv).getTransmitter().insert(ejectTile, ejectMap, null, true, 0);
                    } else {
                        response = ejectMap.addToInventory(ejectInv, oppositeDirection, false);
                    }
                    if (!response.isEmpty()) {
                        response.useAll();
                    }
                }
                delayTicks = 10;
            }
        } else if (delayTicks > 0) {
            delayTicks--;
        }
    }

    public int getDelay() {
        return delayLength;
    }

    @ComputerMethod
    public boolean getSilkTouch() {
        return silkTouch;
    }

    @ComputerMethod
    public int getRadius() {
        return radius;
    }

    @ComputerMethod
    public int getMinY() {
        return minY;
    }

    @ComputerMethod
    public int getMaxY() {
        return maxY;
    }

    @ComputerMethod(nameOverride = "getInverseMode")
    public boolean getInverse() {
        return inverse;
    }

    private void setSilkTouch(boolean newSilkTouch) {
        boolean changed = silkTouch != newSilkTouch;
        silkTouch = newSilkTouch;
        if (changed && (hasLevel() && !isRemote())) {
            energyContainer.updateMinerEnergyPerTick();
        }
    }

    public void toggleSilkTouch() {
        setSilkTouch(!getSilkTouch());
        markDirty(false);
    }

    public void toggleInverse() {
        inverse = !inverse;
        markDirty(false);
    }

    public void toggleAutoEject() {
        doEject = !doEject;
        markDirty(false);
    }

    public void toggleAutoPull() {
        doPull = !doPull;
        markDirty(false);
    }

    public void setRadiusFromPacket(int newRadius) {
        setRadius(Math.min(Math.max(0, newRadius), MekanismConfig.general.minerMaxRadius.get()));
        //Send a packet to update the visual renderer
        //TODO: Only do this if the renderer is actually active
        sendUpdatePacket();
        markDirty(false);
    }

    private void setRadius(int newRadius) {
        boolean changed = radius != newRadius;
        radius = newRadius;
        if (changed && (hasLevel() && !isRemote())) {
            energyContainer.updateMinerEnergyPerTick();
            // If the radius changed and we're on the server, go ahead and refresh the chunk set
            getChunkLoader().refreshChunkTickets();
        }
    }

    public void setMinYFromPacket(int newMinY) {
        setMinY(Math.min(Math.max(0, newMinY), getMaxY()));
        //Send a packet to update the visual renderer
        //TODO: Only do this if the renderer is actually active
        sendUpdatePacket();
        markDirty(false);
    }

    private void setMinY(int newMinY) {
        boolean changed = minY != newMinY;
        minY = newMinY;
        if (changed && (hasLevel() && !isRemote())) {
            energyContainer.updateMinerEnergyPerTick();
        }
    }

    public void setMaxYFromPacket(int newMaxY) {
        if (level != null) {
            setMaxY(Math.max(Math.min(newMaxY, level.getMaxBuildHeight() - 1), getMinY()));
            //Send a packet to update the visual renderer
            //TODO: Only do this if the renderer is actually active
            sendUpdatePacket();
            markDirty(false);
        }
    }

    private void setMaxY(int newMaxY) {
        boolean changed = maxY != newMaxY;
        maxY = newMaxY;
        if (changed && (hasLevel() && !isRemote())) {
            energyContainer.updateMinerEnergyPerTick();
        }
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

    /**
     * @return false if unsuccessful
     */
    private boolean setReplace(BlockPos pos, int index) {
        if (level == null) {
            return false;
        }
        MinerFilter<?> filter = replaceMap.get(index);
        ItemStack stack = getReplace(filter);
        if (stack.isEmpty()) {
            if (filter == null || filter.replaceStack.isEmpty() || !filter.requireStack) {
                level.removeBlock(pos, false);
                return true;
            }
            missingStack = filter.replaceStack;
            return false;
        }
        BlockState newState = MekFakePlayer.withFakePlayer((ServerWorld) level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), fakePlayer ->
              StackUtils.getStateForPlacement(stack, pos, fakePlayer)
        );
        if (newState == null || !newState.canSurvive(level, pos)) {
            //If the spot is not a valid position for the block, then we return that we were unsuccessful
            return false;
        }
        level.setBlockAndUpdate(pos, newState);
        return true;
    }

    private boolean canMine(BlockPos pos) {
        if (level == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return MekFakePlayer.withFakePlayer((ServerWorld) level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), dummy -> {
            dummy.setEmulatingUUID(getOwnerUUID());//pretend to be the owner
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, dummy);
            return !MinecraftForge.EVENT_BUS.post(event);
        });
    }

    private ItemStack getReplace(MinerFilter<?> filter) {
        if (filter == null || filter.replaceStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (IInventorySlot slot : mainSlots) {
            if (filter.replaceStackMatches(slot.getStack())) {
                MekanismUtils.logMismatchedStackSize(slot.shrinkStack(1, Action.EXECUTE), 1);
                return StackUtils.size(filter.replaceStack, 1);
            }
        }
        if (doPull && getPullInv() != null) {
            TransitRequest request = TransitRequest.definedItem(getPullInv(), Direction.DOWN, 1, Finder.strict(filter.replaceStack));
            if (!request.isEmpty() && request.createSimpleResponse().useAll().isEmpty()) {
                //If the request isn't empty and we were able to successfully use it all
                return StackUtils.size(filter.replaceStack, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private TransitRequest getEjectItemMap(TileEntity tile, Direction side) {
        TileTransitRequest request = new TileTransitRequest(tile, side);
        for (int i = mainSlots.size() - 1; i >= 0; i--) {
            IInventorySlot slot = mainSlots.get(i);
            //Note: We are using EXTERNAL as that is what we actually end up using when performing the extraction in the end
            ItemStack simulatedExtraction = slot.extractItem(slot.getCount(), Action.SIMULATE, AutomationType.EXTERNAL);
            if (!simulatedExtraction.isEmpty()) {
                request.addItem(simulatedExtraction, i);
            }
        }
        return request;
    }

    public boolean canInsert(List<ItemStack> toInsert) {
        if (toInsert.isEmpty()) {
            return true;
        }
        int slots = mainSlots.size();
        Int2ObjectMap<ItemCount> cachedStacks = new Int2ObjectOpenHashMap<>();
        for (ItemStack stackToInsert : toInsert) {
            if (stackToInsert.isEmpty()) {
                continue;
            }
            ItemStack stack = stackToInsert.copy();
            for (int i = 0; i < slots; i++) {
                IInventorySlot slot = mainSlots.get(i);
                //Try to insert the item across all slots until we inserted as much as we want to
                // We update our copies reference, to the remainder of what fit, so that we can
                // continue trying the next slots
                boolean wasEmpty = slot.isEmpty();
                if (wasEmpty && cachedStacks.containsKey(i)) {
                    //If we have cached information about the slot and our slot is currently empty so we can't simulate
                    ItemCount cachedItem = cachedStacks.get(i);
                    if (ItemHandlerHelper.canItemStacksStack(stack, cachedItem.stack)) {
                        //If our stack can stack with the item we already put there
                        // Increase how much we inserted up to the slot's limit for that stack type
                        // and then replace the reference to our stack with one that is of the adjusted size
                        int limit = slot.getLimit(stack);
                        int stackSize = stack.getCount();
                        int total = stackSize + cachedItem.count;
                        if (total <= limit) {
                            //It can all fit, increase the cached amount and break
                            cachedItem.count = total;
                            stack = ItemStack.EMPTY;
                            break;
                        }
                        int toAdd = total - limit;
                        if (toAdd > 0) {
                            //Otherwise add what can fit and update the stack to be a reference of that
                            // stack with the proper size
                            cachedItem.count += toAdd;
                            stack = StackUtils.size(stack, stackSize - toAdd);
                        }
                    }
                } else {
                    int stackSize = stack.getCount();
                    stack = slot.insertItem(stack, Action.SIMULATE, AutomationType.INTERNAL);
                    int remainderSize = stack.getCount();
                    if (wasEmpty && remainderSize < stackSize) {
                        //If the slot was empty, and accepted at least some of the item we are inserting
                        // then cache the item type that we put into that slot
                        cachedStacks.put(i, new ItemCount(stackToInsert, stackSize - remainderSize));
                    }
                    if (stack.isEmpty()) {
                        //Once we finished inserting this item, break and move on to the next item
                        break;
                    }
                }
            }
            if (!stack.isEmpty()) {
                //If our stack is not empty that means we could not fit it all inside of our inventory
                // so we return false to being able to insert all the items.
                return false;
            }
        }
        return true;
    }

    private TileEntity getPullInv() {
        return WorldUtils.getTileEntity(getLevel(), getBlockPos().above(2));
    }

    private void add(List<ItemStack> stacks) {
        //TODO: Improve this and the simulated insertion, to try to first complete stacks
        // before inserting into empty stacks, as this will give better results for various
        // edge cases that currently fail
        for (ItemStack stack : stacks) {
            for (IInventorySlot slot : mainSlots) {
                //Try to insert the item across all slots until we inserted it all
                stack = slot.insertItem(stack, Action.EXECUTE, AutomationType.INTERNAL);
                if (stack.isEmpty()) {
                    break;
                }
            }
        }
    }

    public void start() {
        if (getLevel() == null) {
            return;
        }
        if (searcher.state == State.IDLE) {
            BlockPos startingPos = getStartingPos();
            searcher.setChunkCache(new Region(getLevel(), startingPos, startingPos.offset(getDiameter(), getMaxY() - getMinY() + 1, getDiameter())));
            searcher.start();
        }
        running = true;
        markDirty(false);
    }

    public void stop() {
        if (searcher.state == State.SEARCHING) {
            searcher.interrupt();
            reset();
        } else if (searcher.state == State.FINISHED) {
            running = false;
            markDirty(false);
        }
    }

    public void reset() {
        searcher = new ThreadMinerSearch(this);
        running = false;
        cachedToMine = 0;
        oresToMine.clear();
        replaceMap.clear();
        missingStack = ItemStack.EMPTY;
        setActive(false);
        markDirty(false);
    }

    public boolean isReplaceStack(ItemStack stack) {
        for (MinerFilter<?> filter : filters) {
            if (filter.replaceStackMatches(stack)) {
                return true;
            }
        }
        return false;
    }

    private void updateCachedToMine() {
        cachedToMine = oresToMine.values().stream().mapToInt(BitSet::cardinality).sum();
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.load(state, nbtTags);
        running = nbtTags.getBoolean(NBTConstants.RUNNING);
        delay = nbtTags.getInt(NBTConstants.DELAY);
        numPowering = nbtTags.getInt(NBTConstants.NUM_POWERING);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.STATE, State::byIndexStatic, s -> searcher.state = s);
        setGeneralPersistentData(nbtTags);
        //Update energy per tick in case any of the values changed. It would be slightly cleaner to also validate the fact
        // the values changed, but it would make the code a decent bit messier as we couldn't use NBTUtils, and it is a
        // rather quick check to update the energy per tick, and in most cases at least one of the settings will not be at
        // the default value
        energyContainer.updateMinerEnergyPerTick();
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbtTags) {
        super.save(nbtTags);
        if (searcher.state == State.SEARCHING) {
            reset();
        }
        nbtTags.putBoolean(NBTConstants.RUNNING, running);
        nbtTags.putInt(NBTConstants.DELAY, delay);
        nbtTags.putInt(NBTConstants.NUM_POWERING, numPowering);
        nbtTags.putInt(NBTConstants.STATE, searcher.state.ordinal());
        return getGeneralPersistentData(nbtTags);
    }

    public int getTotalSize() {
        return getDiameter() * getDiameter() * (getMaxY() - getMinY() + 1);
    }

    public int getDiameter() {
        return (radius * 2) + 1;
    }

    public BlockPos getStartingPos() {
        return new BlockPos(getBlockPos().getX() - radius, getMinY(), getBlockPos().getZ() - radius);
    }

    private BlockPos getPosFromIndex(int index) {
        int diameter = getDiameter();
        BlockPos start = getStartingPos();
        return start.offset(index % diameter, index / diameter / diameter, (index / diameter) % diameter);
    }

    @Override
    public boolean isPowered() {
        return redstone || numPowering > 0;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (clientRendering) {
            //TODO: Improve on this to use the max that we actually need to do the rendering
            return INFINITE_EXTENT_AABB;
        }
        return new AxisAlignedBB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
    }

    @Override
    public void onPlace() {
        super.onPlace();
        if (level != null) {
            BlockPos pos = getBlockPos();
            for (int x = -1; x <= +1; x++) {
                for (int y = 0; y <= +1; y++) {
                    for (int z = -1; z <= +1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            BlockPos boundingPos = pos.offset(x, y, z);
                            WorldUtils.makeAdvancedBoundingBlock(level, boundingPos, pos);
                            level.updateNeighborsAt(boundingPos, getBlockType());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Mekanism.logger.info("Has level: {}", level != null);
        if (level != null) {
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            level.removeBlock(getBlockPos().offset(x, y, z), false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBoundingBlockPowerChange(BlockPos boundingPos, int oldLevel, int newLevel) {
        if (oldLevel > 0) {
            if (newLevel == 0) {
                numPowering--;
            }
        } else if (newLevel > 0) {
            numPowering++;
        }
    }

    @Override
    public int getBoundingComparatorSignal(Vector3i offset) {
        //Return the comparator signal if it is one of the horizontal ports
        Direction facing = getDirection();
        Direction back = facing.getOpposite();
        if (offset.equals(new Vector3i(back.getStepX(), 1, back.getStepZ()))) {
            return getCurrentRedstoneLevel();
        }
        Direction left = MekanismUtils.getLeft(facing);
        if (offset.equals(new Vector3i(left.getStepX(), 0, left.getStepZ()))) {
            return getCurrentRedstoneLevel();
        }
        Direction right = left.getOpposite();
        if (offset.equals(new Vector3i(right.getStepX(), 0, right.getStepZ()))) {
            return getCurrentRedstoneLevel();
        }
        return 0;
    }

    @Override
    protected void notifyComparatorChange() {
        super.notifyComparatorChange();
        Direction facing = getDirection();
        Direction left = MekanismUtils.getLeft(facing);
        //Proxy the comparator updates to the various ports we expose comparators to
        level.updateNeighbourForOutputSignal(worldPosition.relative(left), MekanismBlocks.ADVANCED_BOUNDING_BLOCK.getBlock());
        level.updateNeighbourForOutputSignal(worldPosition.relative(left.getOpposite()), MekanismBlocks.ADVANCED_BOUNDING_BLOCK.getBlock());
        level.updateNeighbourForOutputSignal(worldPosition.relative(facing.getOpposite()).above(), MekanismBlocks.ADVANCED_BOUNDING_BLOCK.getBlock());
    }

    @Override
    public CompoundNBT getConfigurationData(PlayerEntity player) {
        return getGeneralPersistentData(super.getConfigurationData(player));
    }

    private CompoundNBT getGeneralPersistentData(CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.RADIUS, getRadius());
        nbtTags.putInt(NBTConstants.MIN, getMinY());
        nbtTags.putInt(NBTConstants.MAX, getMaxY());
        nbtTags.putBoolean(NBTConstants.EJECT, doEject);
        nbtTags.putBoolean(NBTConstants.PULL, doPull);
        nbtTags.putBoolean(NBTConstants.SILK_TOUCH, getSilkTouch());
        nbtTags.putBoolean(NBTConstants.INVERSE, inverse);
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (MinerFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            nbtTags.put(NBTConstants.FILTERS, filterTags);
        }
        return nbtTags;
    }

    @Override
    public void setConfigurationData(PlayerEntity player, CompoundNBT data) {
        super.setConfigurationData(player, data);
        setGeneralPersistentData(data);
    }

    @Override
    public void configurationDataSet() {
        super.configurationDataSet();
        if (isRunning()) {
            //If it was running when we updated the configuration data, stop it, reset it, and start it again
            // to ensure that there are no desyncs in energy cost due to things like the radius changing but
            // it having had the blocks to mine calculated based on the old radius
            stop();
            reset();
            start();
        }
    }

    private void setGeneralPersistentData(CompoundNBT data) {
        setRadius(Math.min(data.getInt(NBTConstants.RADIUS), MekanismConfig.general.minerMaxRadius.get()));
        NBTUtils.setIntIfPresent(data, NBTConstants.MIN, this::setMinY);
        NBTUtils.setIntIfPresent(data, NBTConstants.MAX, this::setMaxY);
        doEject = data.getBoolean(NBTConstants.EJECT);
        doPull = data.getBoolean(NBTConstants.PULL);
        NBTUtils.setBooleanIfPresent(data, NBTConstants.SILK_TOUCH, this::setSilkTouch);
        inverse = data.getBoolean(NBTConstants.INVERSE);
        filters.clear();
        if (data.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = data.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof MinerFilter) {
                    filters.add((MinerFilter<?>) filter);
                }
            }
        }
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, NBTConstants.RADIUS, getRadius());
        ItemDataUtils.setInt(itemStack, NBTConstants.MIN, getMinY());
        ItemDataUtils.setInt(itemStack, NBTConstants.MAX, getMaxY());
        ItemDataUtils.setBoolean(itemStack, NBTConstants.EJECT, doEject);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.PULL, doPull);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.SILK_TOUCH, getSilkTouch());
        ItemDataUtils.setBoolean(itemStack, NBTConstants.INVERSE, inverse);
        if (!filters.isEmpty()) {
            ListNBT filterTags = new ListNBT();
            for (MinerFilter<?> filter : filters) {
                filterTags.add(filter.write(new CompoundNBT()));
            }
            ItemDataUtils.setList(itemStack, NBTConstants.FILTERS, filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, NBTConstants.RADIUS, NBT.TAG_INT)) {
            setRadius(Math.min(ItemDataUtils.getInt(itemStack, NBTConstants.RADIUS), MekanismConfig.general.minerMaxRadius.get()));
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.MIN, NBT.TAG_INT)) {
            setMinY(ItemDataUtils.getInt(itemStack, NBTConstants.MIN));
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.MAX, NBT.TAG_INT)) {
            setMaxY(ItemDataUtils.getInt(itemStack, NBTConstants.MAX));
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.EJECT, NBT.TAG_BYTE)) {
            doEject = ItemDataUtils.getBoolean(itemStack, NBTConstants.EJECT);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.PULL, NBT.TAG_BYTE)) {
            doPull = ItemDataUtils.getBoolean(itemStack, NBTConstants.PULL);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.SILK_TOUCH, NBT.TAG_BYTE)) {
            setSilkTouch(ItemDataUtils.getBoolean(itemStack, NBTConstants.SILK_TOUCH));
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.INVERSE, NBT.TAG_BYTE)) {
            inverse = ItemDataUtils.getBoolean(itemStack, NBTConstants.INVERSE);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = ItemDataUtils.getList(itemStack, NBTConstants.FILTERS);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof MinerFilter) {
                    filters.add((MinerFilter<?>) filter);
                }
            }
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.RADIUS, NBTConstants.RADIUS);
        remap.put(NBTConstants.MIN, NBTConstants.MIN);
        remap.put(NBTConstants.MAX, NBTConstants.MAX);
        remap.put(NBTConstants.EJECT, NBTConstants.EJECT);
        remap.put(NBTConstants.PULL, NBTConstants.PULL);
        remap.put(NBTConstants.SILK_TOUCH, NBTConstants.SILK_TOUCH);
        remap.put(NBTConstants.INVERSE, NBTConstants.INVERSE);
        remap.put(NBTConstants.FILTERS, NBTConstants.FILTERS);
        return remap;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            delayLength = MekanismUtils.getTicks(this, MekanismConfig.general.minerTicksPerMine.get());
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getOffsetCapabilityIfEnabled(@Nonnull Capability<T> capability, Direction side, @Nonnull Vector3i offset) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //Get item handler cap directly from here as we disable it entirely for the main block as we only have it enabled from ports
            return itemHandlerManager.resolve(capability, side);
        }
        //Otherwise we can just grab the capability from the tile normally
        return getCapability(capability, side);
    }

    private boolean canAccessFromAnySide(@Nonnull Capability<?> capability) {
        return capability == Capabilities.CONFIG_CARD_CAPABILITY;
    }

    @Override
    public boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, Direction side, @Nonnull Vector3i offset) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return notItemPort(side, offset);
        } else if (EnergyCompatUtils.isEnergyCapability(capability)) {
            return notEnergyPort(side, offset);
        } else if (canEverResolve(capability) && !canAccessFromAnySide(capability)) {
            //If we are not an item handler or energy capability and it is a capability that we can support,
            // but it is not one that we can access from any side so is only a capability that we want to expose
            // via our ports for things like computer integration capabilities, then we treat the capability as
            // disabled if it is not against one of our ports
            return canAccessFromAnySide(capability) || notItemPort(side, offset) && notEnergyPort(side, offset);
        }
        return false;
    }

    private boolean notItemPort(Direction side, Vector3i offset) {
        if (offset.equals(new Vector3i(0, 1, 0))) {
            //If input then disable if wrong face of input
            return side != Direction.UP;
        }
        Direction back = getOppositeDirection();
        if (offset.equals(new Vector3i(back.getStepX(), 1, back.getStepZ()))) {
            //If output then disable if wrong face of output
            return side != back;
        }
        return true;
    }

    private boolean notEnergyPort(Direction side, Vector3i offset) {
        if (offset.equals(Vector3i.ZERO)) {
            //Disable if it is the bottom port but wrong side of it
            return side != Direction.DOWN;
        }
        Direction left = getLeftSide();
        if (offset.equals(new Vector3i(left.getStepX(), 0, left.getStepZ()))) {
            //Disable if left power port but wrong side of the port
            return side != left;
        }
        Direction right = left.getOpposite();
        if (offset.equals(new Vector3i(right.getStepX(), 0, right.getStepZ()))) {
            //Disable if right power port but wrong side of the port
            return side != right;
        }
        return true;
    }

    @Override
    public TileComponentChunkLoader<TileEntityDigitalMiner> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        int chunkXMin = (worldPosition.getX() - radius) >> 4;
        int chunkXMax = (worldPosition.getX() + radius) >> 4;
        int chunkZMin = (worldPosition.getZ() - radius) >> 4;
        int chunkZMax = (worldPosition.getZ() + radius) >> 4;
        Set<ChunkPos> set = new ObjectOpenHashSet<>();
        for (int chunkX = chunkXMin; chunkX <= chunkXMax; chunkX++) {
            for (int chunkZ = chunkZMin; chunkZ <= chunkZMax; chunkZ++) {
                set.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        return set;
    }

    @Override
    @ComputerMethod
    public HashList<MinerFilter<?>> getFilters() {
        return filters;
    }

    public MinerEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @ComputerMethod
    public int getToMine() {
        return !isRemote() && searcher.state == State.SEARCHING ? searcher.found : cachedToMine;
    }

    @ComputerMethod
    public boolean isRunning() {
        return running;
    }

    @ComputerMethod(nameOverride = "getAutoEject")
    public boolean getDoEject() {
        return doEject;
    }

    @ComputerMethod(nameOverride = "getAutoPull")
    public boolean getDoPull() {
        return doPull;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        addConfigContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getDoEject, value -> doEject = value));
        container.track(SyncableBoolean.create(this::getDoPull, value -> doPull = value));
        container.track(SyncableBoolean.create(this::isRunning, value -> running = value));
        container.track(SyncableBoolean.create(this::getSilkTouch, this::setSilkTouch));
        container.track(SyncableEnum.create(State::byIndexStatic, State.IDLE, () -> searcher.state, value -> searcher.state = value));
        container.track(SyncableInt.create(this::getToMine, value -> cachedToMine = value));
        container.track(SyncableItemStack.create(() -> missingStack, value -> missingStack = value));
    }

    public void addConfigContainerTrackers(MekanismContainer container) {
        container.track(SyncableInt.create(this::getRadius, this::setRadius));
        container.track(SyncableInt.create(this::getMinY, this::setMinY));
        container.track(SyncableInt.create(this::getMaxY, this::setMaxY));
        container.track(SyncableBoolean.create(this::getInverse, value -> inverse = value));
        container.track(SyncableFilterList.create(this::getFilters, value -> {
            if (value instanceof HashList) {
                filters = (HashList<MinerFilter<?>>) value;
            } else {
                filters = new HashList<>(value);
            }
        }));
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putInt(NBTConstants.RADIUS, getRadius());
        updateTag.putInt(NBTConstants.MIN, getMinY());
        updateTag.putInt(NBTConstants.MAX, getMaxY());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setIntIfPresent(tag, NBTConstants.RADIUS, this::setRadius);//client allowed to use whatever server sends
        NBTUtils.setIntIfPresent(tag, NBTConstants.MIN, this::setMinY);
        NBTUtils.setIntIfPresent(tag, NBTConstants.MAX, this::setMaxY);
    }

    private List<ItemStack> getDrops(BlockState state, BlockPos pos) {
        if (state.isAir(getWorldNN(), pos)) {
            return Collections.emptyList();
        }
        ItemStack stack = MekanismItems.ATOMIC_DISASSEMBLER.getItemStack();
        if (getSilkTouch()) {
            stack.enchant(Enchantments.SILK_TOUCH, 1);
        }
        return MekFakePlayer.withFakePlayer((ServerWorld) getWorldNN(), this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), fakePlayer -> {
            fakePlayer.setEmulatingUUID(getOwnerUUID());
            LootContext.Builder lootContextBuilder = new LootContext.Builder((ServerWorld) getWorldNN())
                  .withRandom(getWorldNN().random)
                  .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pos))
                  .withParameter(LootParameters.TOOL, stack)
                  .withOptionalParameter(LootParameters.THIS_ENTITY, fakePlayer)
                  .withOptionalParameter(LootParameters.BLOCK_ENTITY, WorldUtils.getTileEntity(getWorldNN(), pos));
            return state.getDrops(lootContextBuilder);
        });
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private FloatingLong getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
    }

    @ComputerMethod
    private int getSlotCount() {
        return mainSlots.size();
    }

    @ComputerMethod
    private ItemStack getItemInSlot(int slot) throws ComputerException {
        int slots = getSlotCount();
        if (slot < 0 || slot >= slots) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this digital miner only has '%d' slots (zero indexed).", slot, slots);
        }
        return mainSlots.get(slot).getStack();
    }

    @ComputerMethod
    private State getState() {
        return searcher.state;
    }

    @ComputerMethod
    private void setAutoEject(boolean eject) throws ComputerException {
        validateSecurityIsPublic();
        if (doEject != eject) {
            toggleAutoEject();
        }
    }

    @ComputerMethod
    private void setAutoPull(boolean pull) throws ComputerException {
        validateSecurityIsPublic();
        if (doPull != pull) {
            toggleAutoPull();
        }
    }

    @ComputerMethod(nameOverride = "setSilkTouch")
    private void computerSetSilkTouch(boolean silk) throws ComputerException {
        validateSecurityIsPublic();
        setSilkTouch(silk);
    }

    @ComputerMethod(nameOverride = "start")
    private void computerStart() throws ComputerException {
        validateSecurityIsPublic();
        start();
    }

    @ComputerMethod(nameOverride = "stop")
    private void computerStop() throws ComputerException {
        validateSecurityIsPublic();
        stop();
    }

    @ComputerMethod(nameOverride = "reset")
    private void computerReset() throws ComputerException {
        validateSecurityIsPublic();
        reset();
    }

    @ComputerMethod
    private int getMaxRadius() {
        return MekanismConfig.general.minerMaxRadius.get();
    }

    private void validateCanChangeConfiguration() throws ComputerException {
        validateSecurityIsPublic();
        //Validate the miner is stopped and reset first
        if (searcher.state != State.IDLE) {
            throw new ComputerException("Miner must be stopped and reset before its targeting configuration is changed.");
        }
    }

    @ComputerMethod(nameOverride = "setRadius")
    private void computerSetRadius(int radius) throws ComputerException {
        validateCanChangeConfiguration();
        if (radius < 0 || radius > MekanismConfig.general.minerMaxRadius.get()) {
            //Validate dimensions even though we can clamp
            throw new ComputerException("Radius '%d' is out of range must be between 0 and %d. (Inclusive)", radius, MekanismConfig.general.minerMaxRadius.get());
        }
        setRadiusFromPacket(radius);
    }

    @ComputerMethod(nameOverride = "setMinY")
    private void computerSetMinY(int minY) throws ComputerException {
        validateCanChangeConfiguration();
        if (minY < 0 || minY > getMaxY()) {
            //Validate dimensions even though we can clamp
            throw new ComputerException("Min Y '%d' is out of range must be between 0 and %d. (Inclusive)", minY, getMaxY());
        }
        setMinYFromPacket(minY);
    }

    @ComputerMethod(nameOverride = "setMaxY")
    private void computerSetMaxY(int maxY) throws ComputerException {
        validateCanChangeConfiguration();
        if (level != null) {
            int max = level.getMaxBuildHeight() - 1;
            if (maxY < getMinY() || maxY > max) {
                //Validate dimensions even though we can clamp
                throw new ComputerException("Max Y '%d' is out of range must be between %d and %d. (Inclusive)", maxY, getMinY(), max);
            }
            setMaxYFromPacket(maxY);
        }
    }

    @ComputerMethod
    private void setInverseMode(boolean enabled) throws ComputerException {
        validateCanChangeConfiguration();
        if (inverse != enabled) {
            toggleInverse();
        }
    }

    @ComputerMethod
    private boolean addFilter(MinerFilter<?> filter) throws ComputerException {
        validateCanChangeConfiguration();
        return filters.add(filter);
    }

    @ComputerMethod
    private boolean removeFilter(MinerFilter<?> filter) throws ComputerException {
        validateCanChangeConfiguration();
        return filters.remove(filter);
    }
    //End methods IComputerTile

    private static class ItemCount {

        private final ItemStack stack;
        private int count;

        public ItemCount(ItemStack stack, int count) {
            this.stack = stack;
            this.count = count;
        }
    }
}