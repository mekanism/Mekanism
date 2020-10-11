package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
import mekanism.common.base.MekFakePlayer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.content.miner.ThreadMinerSearch.State;
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
import mekanism.common.lib.HashList;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.IAdvancedBoundingBlock;
import mekanism.common.tile.interfaces.IHasSortableFilters;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
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
import org.apache.logging.log4j.core.jmx.Server;

public class TileEntityDigitalMiner extends TileEntityMekanism implements ISustainedData, IChunkLoader, IAdvancedBoundingBlock, ITileFilterHolder<MinerFilter<?>>,
      IHasSortableFilters {

    public Map<ChunkPos, BitSet> oresToMine = new Object2ObjectOpenHashMap<>();
    public Int2ObjectMap<MinerFilter<?>> replaceMap = new Int2ObjectOpenHashMap<>();
    private HashList<MinerFilter<?>> filters = new HashList<>();
    public ThreadMinerSearch searcher = new ThreadMinerSearch(this);

    private int radius;

    public boolean inverse;

    private int minY;
    private int maxY = 60;

    public boolean doEject = false;
    public boolean doPull = false;

    public ItemStack missingStack = ItemStack.EMPTY;

    public int delay;

    private int delayLength = MekanismConfig.general.minerTicksPerMine.get();

    public int cachedToMine;

    private boolean silkTouch;

    public boolean running;

    private int delayTicks;

    private boolean initCalc = false;

    private int numPowering;

    public boolean clientRendering = false;

    private final TileComponentChunkLoader<TileEntityDigitalMiner> chunkLoaderComponent = new TileComponentChunkLoader<>(this);

    private MinerEnergyContainer energyContainer;
    private List<IInventorySlot> mainSlots;
    private EnergyInventorySlot energySlot;

    public TileEntityDigitalMiner() {
        super(MekanismBlocks.DIGITAL_MINER);
        radius = 10;
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, this));
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
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 152, 20));
        return builder.build();
    }

    private void closeInvalidScreens() {
        if (getActive() && !playersUsing.isEmpty()) {
            for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                if (player.openContainer instanceof DigitalMinerConfigContainer) {
                    player.closeScreen();
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
                    for (Iterator<ChunkPos> it = oresToMine.keySet().iterator(); it.hasNext(); ) {
                        ChunkPos chunk = it.next();
                        BitSet set = oresToMine.get(chunk);
                        int next = 0;
                        while (!did) {
                            int index = set.nextSetBit(next);
                            BlockPos pos = getPosFromIndex(index);
                            if (index == -1) {
                                it.remove();
                                break;
                            }
                            if (!world.isBlockPresent(pos) || world.isAirBlock(pos)) {
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                    break;
                                }
                                next = index + 1;
                                continue;
                            }
                            boolean hasFilter = false;
                            BlockState state = world.getBlockState(pos);
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

                            List<ItemStack> drops = getDrops(pos);
                            if (canInsert(drops) && setReplace(pos, index)) {
                                did = true;
                                add(drops);
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                }
                                world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(state));
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
            TileEntity ejectInv = getEjectInv();
            TileEntity ejectTile = getEjectTile();
            if (ejectInv != null && ejectTile != null) {
                TransitRequest ejectMap = getEjectItemMap(ejectTile, getOppositeDirection());
                if (!ejectMap.isEmpty()) {
                    TransitResponse response;
                    if (ejectInv instanceof TileEntityLogisticalTransporterBase) {
                        response = ((TileEntityLogisticalTransporterBase) ejectInv).getTransmitter().insert(ejectTile, ejectMap, null, true, 0);
                    } else {
                        response = ejectMap.addToInventory(ejectInv, getOppositeDirection(), false);
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

    public boolean getSilkTouch() {
        return silkTouch;
    }

    public int getRadius() {
        return radius;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setSilkTouch(boolean newSilkTouch) {
        boolean changed = silkTouch != newSilkTouch;
        silkTouch = newSilkTouch;
        if (changed && (hasWorld() && !isRemote())) {
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
        if (changed && (hasWorld() && !isRemote())) {
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
        if (changed && (hasWorld() && !isRemote())) {
            energyContainer.updateMinerEnergyPerTick();
        }
    }

    public void setMaxYFromPacket(int newMaxY) {
        if (world != null) {
            setMaxY(Math.max(Math.min(newMaxY, world.getHeight() - 1), getMinY()));
            //Send a packet to update the visual renderer
            //TODO: Only do this if the renderer is actually active
            sendUpdatePacket();
            markDirty(false);
        }
    }

    private void setMaxY(int newMaxY) {
        boolean changed = maxY != newMaxY;
        maxY = newMaxY;
        if (changed && (hasWorld() && !isRemote())) {
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
        if (world == null) {
            return false;
        }
        MinerFilter<?> filter = replaceMap.get(index);
        ItemStack stack = getReplace(filter);
        if (stack.isEmpty()) {
            if (filter == null || filter.replaceStack.isEmpty() || !filter.requireStack) {
                world.removeBlock(pos, false);
                return true;
            }
            missingStack = filter.replaceStack;
            return false;
        }
        BlockState newState = MekFakePlayer.withFakePlayer((ServerWorld)world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), fakePlayer ->
              StackUtils.getStateForPlacement(stack, pos, fakePlayer)
        );
        if (newState == null || !newState.isValidPosition(world, pos)) {
            //If the spot is not a valid position for the block, then we return that we were unsuccessful
            return false;
        }
        world.setBlockState(pos, newState);
        return true;
    }

    private boolean canMine(BlockPos pos) {
        if (world == null) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        return MekFakePlayer.withFakePlayer((ServerWorld) world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), dummy -> {
            dummy.setEmulatingUUID(getSecurity().getOwnerUUID());//pretend to be the owner
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, dummy);
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
        return MekanismUtils.getTileEntity(getWorld(), getPos().up(2));
    }

    private TileEntity getEjectInv() {
        return MekanismUtils.getTileEntity(world, getPos().up().offset(getOppositeDirection(), 2));
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
        if (getWorld() == null) {
            return;
        }
        if (searcher.state == State.IDLE) {
            BlockPos startingPos = getStartingPos();
            searcher.setChunkCache(new Region(getWorld(), startingPos, startingPos.add(getDiameter(), getMaxY() - getMinY() + 1, getDiameter())));
            searcher.start();
        }
        running = true;
        markDirty(false);
    }

    public void stop() {
        if (searcher.state == State.SEARCHING) {
            searcher.interrupt();
            reset();
            return;
        } else if (searcher.state == State.FINISHED) {
            running = false;
        }
        markDirty(false);
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
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        running = nbtTags.getBoolean(NBTConstants.RUNNING);
        delay = nbtTags.getInt(NBTConstants.DELAY);
        numPowering = nbtTags.getInt(NBTConstants.NUM_POWERING);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.STATE, State::byIndexStatic, s -> searcher.state = s);
        setConfigurationData(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (searcher.state == State.SEARCHING) {
            reset();
        }
        nbtTags.putBoolean(NBTConstants.RUNNING, running);
        nbtTags.putInt(NBTConstants.DELAY, delay);
        nbtTags.putInt(NBTConstants.NUM_POWERING, numPowering);
        nbtTags.putInt(NBTConstants.STATE, searcher.state.ordinal());
        return getConfigurationData(nbtTags);
    }

    public int getTotalSize() {
        return getDiameter() * getDiameter() * (getMaxY() - getMinY() + 1);
    }

    public int getDiameter() {
        return (radius * 2) + 1;
    }

    public BlockPos getStartingPos() {
        return new BlockPos(getPos().getX() - radius, getMinY(), getPos().getZ() - radius);
    }

    private BlockPos getPosFromIndex(int index) {
        int diameter = getDiameter();
        BlockPos start = getStartingPos();
        return start.add(index % diameter, index / diameter / diameter, (index / diameter) % diameter);
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
        return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
    }

    @Override
    public void onPlace() {
        if (world != null) {
            BlockPos pos = getPos();
            for (int x = -1; x <= +1; x++) {
                for (int y = 0; y <= +1; y++) {
                    for (int z = -1; z <= +1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            BlockPos boundingPos = pos.add(x, y, z);
                            MekanismUtils.makeAdvancedBoundingBlock(world, boundingPos, pos);
                            world.notifyNeighborsOfStateChange(boundingPos, getBlockType());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBreak(BlockState oldState) {
        if (world != null) {
            for (int x = -1; x <= +1; x++) {
                for (int y = 0; y <= +1; y++) {
                    for (int z = -1; z <= +1; z++) {
                        world.removeBlock(getPos().add(x, y, z), false);
                    }
                }
            }
        }
    }

    private TileEntity getEjectTile() {
        return MekanismUtils.getTileEntity(getWorld(), getPos().up().offset(getOppositeDirection()));
    }

    @Override
    public void onPower() {
        numPowering++;
    }

    @Override
    public void onNoPower() {
        numPowering--;
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
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
    public void setConfigurationData(CompoundNBT nbtTags) {
        setRadius(Math.min(nbtTags.getInt(NBTConstants.RADIUS), MekanismConfig.general.minerMaxRadius.get()));
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.MIN, this::setMinY);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.MAX, this::setMaxY);
        doEject = nbtTags.getBoolean(NBTConstants.EJECT);
        doPull = nbtTags.getBoolean(NBTConstants.PULL);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.SILK_TOUCH, this::setSilkTouch);
        inverse = nbtTags.getBoolean(NBTConstants.INVERSE);
        filters.clear();
        if (nbtTags.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                IFilter<?> filter = BaseFilter.readFromNBT(tagList.getCompound(i));
                if (filter instanceof MinerFilter) {
                    filters.add((MinerFilter<?>) filter);
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
            if (hasInventory()) {
                return itemHandlerManager.resolve(capability, side);
            }
            return LazyOptional.empty();
        } else if (EnergyCompatUtils.isEnergyCapability(capability)) {
            if (canHandleEnergy()) {
                return energyHandlerManager.resolve(capability, side);
            }
            return LazyOptional.empty();
        }
        //Fallback to checking the normal capabilities
        return getCapability(capability, side);
    }

    @Override
    public boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, Direction side, @Nonnull Vector3i offset) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //Input
            if (offset.equals(new Vector3i(0, 1, 0))) {
                //If input then disable if wrong face of input
                return side != Direction.UP;
            }
            //Output
            Direction back = getOppositeDirection();
            if (offset.equals(new Vector3i(back.getXOffset(), 1, back.getZOffset()))) {
                //If output then disable if wrong face of output
                return side != back;
            }
            return true;
        } else if (EnergyCompatUtils.isEnergyCapability(capability)) {
            if (offset.equals(Vector3i.NULL_VECTOR)) {
                //Disable if it is the bottom port but wrong side of it
                return side != Direction.DOWN;
            }
            Direction left = getLeftSide();
            Direction right = getRightSide();
            if (offset.equals(new Vector3i(left.getXOffset(), 0, left.getZOffset()))) {
                //Disable if left power port but wrong side of the port
                return side != left;
            } else if (offset.equals(new Vector3i(right.getXOffset(), 0, right.getZOffset()))) {
                //Disable if right power port but wrong side of the port
                return side != right;
            }
            return true;
        }
        return false;
    }

    @Override
    public TileComponentChunkLoader<TileEntityDigitalMiner> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        int chunkXMin = (pos.getX() - radius) >> 4;
        int chunkXMax = (pos.getX() + radius) >> 4;
        int chunkZMin = (pos.getZ() - radius) >> 4;
        int chunkZMax = (pos.getZ() + radius) >> 4;
        Set<ChunkPos> set = new ObjectOpenHashSet<>();
        for (int chunkX = chunkXMin; chunkX <= chunkXMax; chunkX++) {
            for (int chunkZ = chunkZMin; chunkZ <= chunkZMax; chunkZ++) {
                set.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        return set;
    }

    @Override
    public HashList<MinerFilter<?>> getFilters() {
        return filters;
    }

    public MinerEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        addConfigContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> doEject, value -> doEject = value));
        container.track(SyncableBoolean.create(() -> doPull, value -> doPull = value));
        container.track(SyncableBoolean.create(() -> running, value -> running = value));
        container.track(SyncableBoolean.create(this::getSilkTouch, this::setSilkTouch));
        container.track(SyncableEnum.create(State::byIndexStatic, State.IDLE, () -> searcher.state, value -> searcher.state = value));
        container.track(SyncableInt.create(() -> !isRemote() && searcher.state == State.SEARCHING ? searcher.found : cachedToMine, value -> cachedToMine = value));
        container.track(SyncableItemStack.create(() -> missingStack, value -> missingStack = value));
    }

    public void addConfigContainerTrackers(MekanismContainer container) {
        container.track(SyncableInt.create(this::getRadius, this::setRadius));
        container.track(SyncableInt.create(this::getMinY, this::setMinY));
        container.track(SyncableInt.create(this::getMaxY, this::setMaxY));
        container.track(SyncableBoolean.create(() -> inverse, value -> inverse = value));
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

    private List<ItemStack> getDrops(BlockPos pos) {
        BlockState state = this.getWorldNN().getBlockState(pos);
        if (state.isAir(this.getWorldNN(), pos)) {
            return Collections.emptyList();
        }
        ItemStack stack = MekanismItems.ATOMIC_DISASSEMBLER.getItemStack();
        if (getSilkTouch()) {
            stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
        }
        return MekFakePlayer.withFakePlayer((ServerWorld)this.getWorldNN(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), fakePlayer -> {
            fakePlayer.setEmulatingUUID(getSecurity().getOwnerUUID());
            LootContext.Builder lootContextBuilder = new LootContext.Builder((ServerWorld)this.getWorldNN())
                  .withRandom(this.getWorldNN().rand)
                  .withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(pos))
                  .withParameter(LootParameters.TOOL, stack)
                  .withNullableParameter(LootParameters.THIS_ENTITY, fakePlayer)
                  .withNullableParameter(LootParameters.BLOCK_ENTITY, MekanismUtils.getTileEntity(this.getWorldNN(), pos));
            return state.getDrops(lootContextBuilder);
        });
    }

    private static class ItemCount {

        private final ItemStack stack;
        private int count;

        public ItemCount(ItemStack stack, int count) {
            this.stack = stack;
            this.count = count;
        }
    }
}