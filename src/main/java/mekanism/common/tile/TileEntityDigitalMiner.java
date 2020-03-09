package mekanism.common.tile;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MinerUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Region;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityDigitalMiner extends TileEntityMekanism implements IActiveState, ISustainedData, IChunkLoader, IAdvancedBoundingBlock,
      ITileFilterHolder<MinerFilter<?>> {

    public Map<ChunkPos, BitSet> oresToMine = new Object2ObjectOpenHashMap<>();
    public Int2ObjectMap<MinerFilter<?>> replaceMap = new Int2ObjectOpenHashMap<>();
    private HashList<MinerFilter<?>> filters = new HashList<>();
    public ThreadMinerSearch searcher = new ThreadMinerSearch(this);

    private int radius;

    public boolean inverse;

    public int minY = 0;
    public int maxY = 60;

    public boolean doEject = false;
    public boolean doPull = false;

    public ItemStack missingStack = ItemStack.EMPTY;

    public int delay;

    private int delayLength = MekanismConfig.general.digitalMinerTicksPerMine.get();

    public int cachedToMine;

    public boolean silkTouch;

    public boolean running;

    private double prevEnergy;

    private int delayTicks;

    private boolean initCalc = false;

    private int numPowering;

    public boolean clientRendering = false;

    private TileComponentChunkLoader<TileEntityDigitalMiner> chunkLoaderComponent = new TileComponentChunkLoader<>(this);

    private List<IInventorySlot> mainSlots;
    private EnergyInventorySlot energySlot;

    public TileEntityDigitalMiner() {
        super(MekanismBlocks.DIGITAL_MINER);
        radius = 10;
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
                BasicInventorySlot slot = BasicInventorySlot.at(canExtract, canInsert, this, 8 + slotX * 18, 80 + slotY * 18);
                builder.addSlot(slot, RelativeSide.BACK, RelativeSide.TOP);
                mainSlots.add(slot);
            }
        }
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 152, 6));
        return builder.build();
    }

    private void closeInvalidScreens() {
        if (getActive() && !playersUsing.isEmpty()) {
            for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                if (player.openContainer instanceof IEmptyContainer || player.openContainer instanceof FilterContainer) {
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

        energySlot.discharge(this);

        if (MekanismUtils.canFunction(this) && running && getEnergy() >= getEnergyPerTick() && searcher.state == State.FINISHED && !oresToMine.isEmpty()) {
            setActive(true);
            if (delay > 0) {
                delay--;
            }
            setEnergy(getEnergy() - getEnergyPerTick());
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

                        List<ItemStack> drops = MinerUtils.getDrops((ServerWorld) world, pos, silkTouch, this.pos);
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
        } else if (prevEnergy >= getEnergy()) {
            setActive(false);
        }

        TransitRequest ejectMap = getEjectItemMap();
        if (doEject && delayTicks == 0 && !ejectMap.isEmpty()) {
            TileEntity ejectInv = getEjectInv();
            TileEntity ejectTile = getEjectTile();
            if (ejectInv != null && ejectTile != null) {
                TransitResponse response;
                Optional<ILogisticalTransporter> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(ejectInv, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, getOppositeDirection()));
                if (capability.isPresent()) {
                    response = capability.get().insert(ejectTile, ejectMap, null, true, 0);
                } else {
                    response = InventoryUtils.putStackInInventory(ejectInv, ejectMap, getOppositeDirection(), false);
                }
                if (!response.isEmpty()) {
                    response.getInvStack(ejectTile, getOppositeDirection()).use();
                }
                delayTicks = 10;
            }
        } else if (delayTicks > 0) {
            delayTicks--;
        }
        prevEnergy = getEnergy();
    }

    @Override
    public double getEnergyPerTick() {
        double ret = super.getEnergyPerTick();
        if (silkTouch) {
            ret *= MekanismConfig.general.minerSilkMultiplier.get();
        }
        int baseRad = Math.max(radius - 10, 0);
        ret *= 1 + ((float) baseRad / 22F);
        int baseHeight = Math.max(maxY - minY - 60, 0);
        ret *= 1 + ((float) baseHeight / 195F);
        return ret;
    }

    public int getDelay() {
        return delayLength;
    }

    public int getRadius() {
        return radius;
    }

    private void setRadius(int newRadius) {
        boolean changed = radius != newRadius;
        radius = newRadius;
        // If the radius changed and we're on the server, go ahead and refresh
        // the chunk set
        if (changed && hasWorld() && isRemote()) {
            getChunkLoader().refreshChunkTickets();
        }
    }

    /**
     * @return false if unsuccessful
     */
    private boolean setReplace(BlockPos pos, int index) {
        if (world == null) {
            return false;
        }
        ItemStack stack = getReplace(index);
        if (stack.isEmpty()) {
            MinerFilter<?> filter = replaceMap.get(index);
            if (filter == null || filter.replaceStack.isEmpty() || !filter.requireStack) {
                world.removeBlock(pos, false);
                return true;
            }
            missingStack = filter.replaceStack;
            return false;
        }
        PlayerEntity fakePlayer = Objects.requireNonNull(Mekanism.proxy.getDummyPlayer((ServerWorld) world, this.pos).get());
        BlockState newState = StackUtils.getStateForPlacement(stack, pos, fakePlayer);
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
        PlayerEntity dummy = Objects.requireNonNull(Mekanism.proxy.getDummyPlayer((ServerWorld) world, getPos()).get());
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, dummy);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    private ItemStack getReplace(int index) {
        MinerFilter<?> filter = replaceMap.get(index);
        if (filter == null || filter.replaceStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (IInventorySlot slot : mainSlots) {
            if (filter.replaceStackMatches(slot.getStack())) {
                if (slot.shrinkStack(1, Action.EXECUTE) != 1) {
                    //TODO: Print error/warning
                }
                return StackUtils.size(filter.replaceStack, 1);
            }
        }
        if (doPull && getPullInv() != null) {
            InvStack stack = InventoryUtils.takeDefinedItem(getPullInv(), Direction.UP, filter.replaceStack.copy(), 1, 1);
            if (stack != null) {
                stack.use();
                return StackUtils.size(filter.replaceStack, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private TransitRequest getEjectItemMap() {
        TransitRequest request = new TransitRequest();
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

    private void start() {
        if (getWorld() == null) {
            return;
        }
        if (searcher.state == State.IDLE) {
            BlockPos startingPos = getStartingPos();
            searcher.setChunkCache(new Region(getWorld(), startingPos, startingPos.add(getDiameter(), maxY - minY + 1, getDiameter())));
            searcher.start();
        }
        running = true;
        MekanismUtils.saveChunk(this);
    }

    private void stop() {
        if (searcher.state == State.SEARCHING) {
            searcher.interrupt();
            reset();
            return;
        } else if (searcher.state == State.FINISHED) {
            running = false;
        }
        MekanismUtils.saveChunk(this);
    }

    private void reset() {
        searcher = new ThreadMinerSearch(this);
        running = false;
        cachedToMine = 0;
        oresToMine.clear();
        replaceMap.clear();
        missingStack = ItemStack.EMPTY;
        setActive(false);
        MekanismUtils.saveChunk(this);
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        running = nbtTags.getBoolean(NBTConstants.RUNNING);
        delay = nbtTags.getInt(NBTConstants.DELAY);
        numPowering = nbtTags.getInt(NBTConstants.NUM_POWERING);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.STATE, State::byIndexStatic, state -> searcher.state = state);
        setConfigurationData(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
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

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            switch (type) {
                case 0:
                    doEject = !doEject;
                    break;
                case 1:
                    doPull = !doPull;
                    break;
                case 3:
                    start();
                    break;
                case 4:
                    stop();
                    break;
                case 5:
                    reset();
                    break;
                case 6:
                    setRadius(Math.min(dataStream.readInt(), MekanismConfig.general.digitalMinerMaxRadius.get()));
                    break;
                case 7:
                    minY = dataStream.readInt();
                    break;
                case 8:
                    maxY = dataStream.readInt();
                    break;
                case 9:
                    silkTouch = !silkTouch;
                    break;
                case 10:
                    inverse = !inverse;
                    break;
                case 11: {
                    // Move filter up
                    int filterIndex = dataStream.readInt();
                    filters.swap(filterIndex, filterIndex - 1);
                    sendToAllUsing(() -> new PacketTileEntity(this, getFilterPacket()));
                    break;
                }
                case 12: {
                    // Move filter down
                    int filterIndex = dataStream.readInt();
                    filters.swap(filterIndex, filterIndex + 1);
                    sendToAllUsing(() -> new PacketTileEntity(this, getFilterPacket()));
                    break;
                }
            }

            MekanismUtils.saveChunk(this);
            return;
        }
        super.handlePacketData(dataStream);
        if (isRemote()) {
            if (dataStream.readBoolean()) {
                setRadius(dataStream.readInt());//client allowed to use whatever server sends
                minY = dataStream.readInt();
                maxY = dataStream.readInt();
            }
            filters.clear();
            int amount = dataStream.readInt();
            for (int i = 0; i < amount; i++) {
                filters.add(MinerFilter.readFromPacket(dataStream));
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(true);
        //These three are used for the miner renderer
        data.add(radius);
        data.add(minY);
        data.add(maxY);
        data.add(filters.size());
        for (MinerFilter<?> filter : filters) {
            filter.write(data);
        }
        return data;
    }

    @Override
    public TileNetworkList getFilterPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(false);
        data.add(filters.size());
        for (MinerFilter<?> filter : filters) {
            filter.write(data);
        }
        return data;
    }

    public int getTotalSize() {
        return getDiameter() * getDiameter() * (maxY - minY + 1);
    }

    public int getDiameter() {
        return (radius * 2) + 1;
    }

    public BlockPos getStartingPos() {
        return new BlockPos(getPos().getX() - radius, minY, getPos().getZ() - radius);
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
        return INFINITE_EXTENT_AABB;
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
    public void onBreak() {
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
        nbtTags.putInt(NBTConstants.RADIUS, radius);
        nbtTags.putInt(NBTConstants.MIN, minY);
        nbtTags.putInt(NBTConstants.MAX, maxY);
        nbtTags.putBoolean(NBTConstants.EJECT, doEject);
        nbtTags.putBoolean(NBTConstants.PULL, doPull);
        nbtTags.putBoolean(NBTConstants.SILK_TOUCH, silkTouch);
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
        setRadius(Math.min(nbtTags.getInt(NBTConstants.RADIUS), MekanismConfig.general.digitalMinerMaxRadius.get()));
        minY = nbtTags.getInt(NBTConstants.MIN);
        maxY = nbtTags.getInt(NBTConstants.MAX);
        doEject = nbtTags.getBoolean(NBTConstants.EJECT);
        doPull = nbtTags.getBoolean(NBTConstants.PULL);
        silkTouch = nbtTags.getBoolean(NBTConstants.SILK_TOUCH);
        inverse = nbtTags.getBoolean(NBTConstants.INVERSE);
        filters.clear();
        if (nbtTags.contains(NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.FILTERS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(MinerFilter.readFromNBT(tagList.getCompound(i)));
            }
        }
        if (getWorld() != null) {
            //send filter update packet, as this isn't tracked by container
            Mekanism.packetHandler.sendToAllTracking(new PacketTileEntity(this, getFilterPacket()), this);
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey();
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, NBTConstants.RADIUS, radius);
        ItemDataUtils.setInt(itemStack, NBTConstants.MIN, minY);
        ItemDataUtils.setInt(itemStack, NBTConstants.MAX, maxY);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.EJECT, doEject);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.PULL, doPull);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.SILK_TOUCH, silkTouch);
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
            setRadius(Math.min(ItemDataUtils.getInt(itemStack, NBTConstants.RADIUS), MekanismConfig.general.digitalMinerMaxRadius.get()));
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.MIN, NBT.TAG_INT)) {
            minY = ItemDataUtils.getInt(itemStack, NBTConstants.MIN);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.MAX, NBT.TAG_INT)) {
            maxY = ItemDataUtils.getInt(itemStack, NBTConstants.MAX);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.EJECT, NBT.TAG_BYTE)) {
            doEject = ItemDataUtils.getBoolean(itemStack, NBTConstants.EJECT);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.PULL, NBT.TAG_BYTE)) {
            doPull = ItemDataUtils.getBoolean(itemStack, NBTConstants.PULL);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.SILK_TOUCH, NBT.TAG_BYTE)) {
            silkTouch = ItemDataUtils.getBoolean(itemStack, NBTConstants.SILK_TOUCH);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.INVERSE, NBT.TAG_BYTE)) {
            inverse = ItemDataUtils.getBoolean(itemStack, NBTConstants.INVERSE);
        }
        if (ItemDataUtils.hasData(itemStack, NBTConstants.FILTERS, NBT.TAG_LIST)) {
            ListNBT tagList = ItemDataUtils.getList(itemStack, NBTConstants.FILTERS);
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(MinerFilter.readFromNBT(tagList.getCompound(i)));
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
            delayLength = MekanismUtils.getTicks(this, MekanismConfig.general.digitalMinerTicksPerMine.get());
        }
    }

    @Override
    public boolean canBoundReceiveEnergy(BlockPos coord, Direction side) {
        Direction left = getLeftSide();
        Direction right = getRightSide();
        if (coord.equals(getPos().offset(left))) {
            return side == left;
        } else if (coord.equals(getPos().offset(right))) {
            return side == right;
        }
        return false;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getLeftSide() || side == getRightSide() || side == Direction.DOWN;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        } else if (capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getOffsetCapabilityIfEnabled(@Nonnull Capability<T> capability, Direction side, @Nonnull Vec3i offset) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> getItemHandler(side)));
        } else if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        } else if (capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY) {
            return Capabilities.ENERGY_ACCEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        } else if (capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
            return Capabilities.ENERGY_OUTPUTTER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        } else if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> forgeEnergyManager.getWrapper(this, side)));
        }
        //Fallback to checking the normal capabilities
        return getCapability(capability, side);
    }

    @Override
    public boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, Direction side, @Nonnull Vec3i offset) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //Input
            if (offset.equals(new Vec3i(0, 1, 0))) {
                //If input then disable if wrong face of input
                return side != Direction.UP;
            }
            //Output
            Direction back = getOppositeDirection();
            if (offset.equals(new Vec3i(back.getXOffset(), 1, back.getZOffset()))) {
                //If output then disable if wrong face of output
                return side != back;
            }
            return true;
        } else if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY) {
            if (offset.equals(Vec3i.NULL_VECTOR)) {
                //Disable if it is the bottom port but wrong side of it
                return side != Direction.DOWN;
            }
            Direction left = getLeftSide();
            Direction right = getRightSide();
            if (offset.equals(new Vec3i(left.getXOffset(), 0, left.getZOffset()))) {
                //Disable if left power port but wrong side of the port
                return side != left;
            } else if (offset.equals(new Vec3i(right.getXOffset(), 0, right.getZOffset()))) {
                //Disable if right power port but wrong side of the port
                return side != right;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        //Return some capabilities as disabled, and handle them with offset capabilities instead
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        } else if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public TileComponentChunkLoader<TileEntityDigitalMiner> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        int chunkXMin = pos.getX() - radius >> 4;
        int chunkXMax = pos.getX() + radius >> 4;
        int chunkZMin = pos.getX() - radius >> 4;
        int chunkZMax = pos.getZ() + radius >> 4;
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

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        addConfigContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> doEject, value -> doEject = value));
        container.track(SyncableBoolean.create(() -> doPull, value -> doPull = value));
        container.track(SyncableBoolean.create(() -> running, value -> running = value));
        container.track(SyncableBoolean.create(() -> silkTouch, value -> silkTouch = value));
        container.track(SyncableEnum.create(State::byIndexStatic, State.IDLE, () -> searcher.state, value -> searcher.state = value));
        container.track(SyncableInt.create(() -> !isRemote() && searcher.state == State.SEARCHING ? searcher.found : cachedToMine, value -> cachedToMine = value));
        container.track(SyncableItemStack.create(() -> missingStack, value -> missingStack = value));
    }

    public void addConfigContainerTrackers(MekanismContainer container) {
        container.track(SyncableInt.create(() -> radius, value -> radius = value));
        container.track(SyncableInt.create(() -> minY, value -> minY = value));
        container.track(SyncableInt.create(() -> maxY, value -> maxY = value));
        container.track(SyncableBoolean.create(() -> inverse, value -> inverse = value));
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