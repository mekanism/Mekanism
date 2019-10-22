package mekanism.common.tile;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.ItemRegistryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MinerUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
      ITileFilterHolder<MinerFilter> {

    private static final int[] INV_SLOTS = IntStream.range(0, 28).toArray();

    public Map<Chunk3D, BitSet> oresToMine = new HashMap<>();
    public Map<Integer, MinerFilter> replaceMap = new HashMap<>();
    private HashList<MinerFilter> filters = new HashList<>();
    public ThreadMinerSearch searcher = new ThreadMinerSearch(this);

    private int radius;

    public boolean inverse;

    public int minY = 0;
    public int maxY = 60;

    public boolean doEject = false;
    public boolean doPull = false;

    public ItemStack missingStack = ItemStack.EMPTY;

    public int BASE_DELAY = 80;

    public int delay;

    public int delayLength = BASE_DELAY;

    public int clientToMine;

    public boolean silkTouch;

    public boolean running;

    public double prevEnergy;

    public int delayTicks;

    public boolean initCalc = false;

    public int numPowering;

    public boolean clientRendering = false;

    private Set<ChunkPos> chunkSet;

    public TileComponentChunkLoader chunkLoaderComponent = new TileComponentChunkLoader(this);
    public String[] methods = {"setRadius", "setMin", "setMax", "addFilter", "removeFilter", "addOreFilter", "removeOreFilter", "reset", "start", "stop", "getToMine"};

    private List<IInventorySlot> mainSlots;
    private EnergyInventorySlot energySlot;

    public TileEntityDigitalMiner() {
        super(MekanismBlock.DIGITAL_MINER);
        radius = 10;
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        mainSlots = new ArrayList<>();
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                BasicInventorySlot slot = BasicInventorySlot.at(this, 8 + slotX * 18, 80 + slotY * 18);
                builder.addSlot(slot, RelativeSide.BACK, RelativeSide.TOP);
                mainSlots.add(slot);
                //TODO: Make it so insertion/extraction is sided but the inventory is the same???
                //Can insert;
                //RelativeSide.TOP && isReplaceStack(stack)
                //Can extract:
                //RelativeSide.BACK && !isReplaceStack(stack)
            }
        }
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 152, 6));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (getActive()) {
            for (PlayerEntity player : new HashSet<>(playersUsing)) {
                if (player.openContainer instanceof IEmptyContainer || player.openContainer instanceof FilterContainer) {
                    player.closeScreen();
                }
            }
        }

        if (!isRemote()) {
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

            if (MekanismUtils.canFunction(this) && running && getEnergy() >= getPerTick() && searcher.state == State.FINISHED && oresToMine.size() > 0) {
                setActive(true);
                if (delay > 0) {
                    delay--;
                }
                setEnergy(getEnergy() - getPerTick());
                if (delay == 0) {
                    boolean did = false;
                    for (Iterator<Chunk3D> it = oresToMine.keySet().iterator(); it.hasNext(); ) {
                        Chunk3D chunk = it.next();
                        BitSet set = oresToMine.get(chunk);
                        int next = 0;
                        while (!did) {
                            int index = set.nextSetBit(next);
                            Coord4D coord = getCoordFromIndex(index);
                            if (index == -1) {
                                it.remove();
                                break;
                            }

                            if (!coord.exists(world)) {
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                    break;
                                }
                                next = index + 1;
                                continue;
                            }

                            BlockState state = coord.getBlockState(world);
                            Block block = state.getBlock();

                            if (coord.isAirBlock(world)) {
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                    break;
                                }
                                next = index + 1;
                                continue;
                            }

                            boolean hasFilter = false;
                            ItemStack is = new ItemStack(block);
                            for (MinerFilter filter : filters) {
                                if (filter.canFilter(is)) {
                                    hasFilter = true;
                                    break;
                                }
                            }

                            if (inverse == hasFilter || !canMine(coord)) {
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                    break;
                                }
                                next = index + 1;
                                continue;
                            }

                            List<ItemStack> drops = MinerUtils.getDrops(world, coord, silkTouch, this.pos);
                            if (canInsert(drops) && setReplace(coord, index)) {
                                did = true;
                                add(drops);
                                set.clear(index);
                                if (set.cardinality() == 0) {
                                    it.remove();
                                }
                                world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, coord.getPos(), Block.getStateId(state));
                                missingStack = ItemStack.EMPTY;
                            }
                            break;
                        }
                    }
                    delay = getDelay();
                }
            } else if (prevEnergy >= getEnergy()) {
                setActive(false);
            }

            TransitRequest ejectMap = getEjectItemMap();
            if (doEject && delayTicks == 0 && !ejectMap.isEmpty()) {
                TileEntity ejectInv = getEjectInv();
                TileEntity ejectTile = getEjectTile();
                if (ejectInv != null && ejectTile != null) {
                    TransitResponse response = CapabilityUtils.getCapabilityHelper(ejectInv, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, getOppositeDirection()).getIfPresentElseDo(
                          transporter -> TransporterUtils.insert(ejectTile, transporter, ejectMap, null, true, 0),
                          () -> InventoryUtils.putStackInInventory(ejectInv, ejectMap, getOppositeDirection(), false)
                    );
                    if (!response.isEmpty()) {
                        response.getInvStack(ejectTile, getOppositeDirection()).use();
                    }
                    delayTicks = 10;
                }
            } else if (delayTicks > 0) {
                delayTicks--;
            }

            if (playersUsing.size() > 0) {
                for (PlayerEntity player : playersUsing) {
                    Mekanism.packetHandler.sendTo(new PacketTileEntity(this, getSmallPacket(new TileNetworkList())), (ServerPlayerEntity) player);
                }
            }
            prevEnergy = getEnergy();
        }
    }

    public double getPerTick() {
        double ret = getEnergyPerTick();
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

    public void setRadius(int newRadius) {
        boolean changed = radius != newRadius;
        radius = newRadius;
        // If the radius changed and we're on the server, go ahead and refresh
        // the chunk set
        if (changed && hasWorld() && isRemote()) {
            chunkSet = null;
            getChunkSet();
        }
    }

    /*
     * returns false if unsuccessful
     */
    public boolean setReplace(Coord4D obj, int index) {
        ItemStack stack = getReplace(index);
        BlockPos pos = obj.getPos();
        PlayerEntity fakePlayer = Objects.requireNonNull(Mekanism.proxy.getDummyPlayer((ServerWorld) world, this.pos).get());

        //TODO: Verify shulker checks not needed

        if (!stack.isEmpty()) {
            world.setBlockState(pos, StackUtils.getStateForPlacement(stack, world, pos, fakePlayer));
            BlockState s = obj.getBlockState(world);
            if (s.getBlock() instanceof BushBlock && !((BushBlock) s.getBlock()).isValidPosition(s, world, pos)) {
                //TODO Block.spawnDrops fortune 1?? Also make sure to drop the item
                //s.getBlock().dropBlockAsItem(world, pos, s, 1);
                world.removeBlock(pos, false);
            }
            return true;
        } else {
            MinerFilter filter = replaceMap.get(index);
            if (filter == null || filter.replaceStack.isEmpty() || !filter.requireStack) {
                world.removeBlock(pos, false);
                return true;
            }
            missingStack = filter.replaceStack;
            //TODO: Verify shulker checks not needed
            return false;
        }
    }

    private boolean canMine(Coord4D coord) {
        BlockState state = coord.getBlockState(world);
        PlayerEntity dummy = Objects.requireNonNull(Mekanism.proxy.getDummyPlayer((ServerWorld) world, pos).get());
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, coord.getPos(), state, dummy);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public ItemStack getReplace(int index) {
        MinerFilter filter = replaceMap.get(index);
        if (filter == null || filter.replaceStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (IInventorySlot slot : mainSlots) {
            ItemStack stack = slot.getStack();
            //TODO: Should this be ItemHandlerHelper.canItemStacksStack() instead of isItemEqual
            if (!stack.isEmpty() && stack.isItemEqual(filter.replaceStack)) {
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

    public NonNullList<ItemStack> copy(List<IInventorySlot> slots) {
        //TODO: Clean this up/cache it??
        NonNullList<ItemStack> toReturn = NonNullList.withSize(slots.size(), ItemStack.EMPTY);
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getStack();
            toReturn.set(i, !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY);
        }
        return toReturn;
    }

    public TransitRequest getEjectItemMap() {
        TransitRequest request = new TransitRequest();
        for (int i = mainSlots.size() - 1; i >= 0; i--) {
            ItemStack stack = mainSlots.get(i).getStack();
            if (!stack.isEmpty() && !isReplaceStack(stack)) {
                //TODO: Check if we need to place a copy in terms of mutability
                request.addItem(stack, i);
            }
        }
        return request;
    }

    public boolean canInsert(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return true;
        }
        NonNullList<ItemStack> testInv = copy(mainSlots);
        int added = 0;

        stacks:
        for (ItemStack stack : stacks) {
            stack = stack.copy();
            if (stack.isEmpty()) {
                continue;
            }
            for (int i = 0; i < testInv.size(); i++) {
                //TODO: Would be better if we could do this via the slots rather than copying it and then looping it
                // This way we can obey what the slot believe its limit is/how high to stack
                ItemStack existingStack = testInv.get(i);
                if (existingStack.isEmpty()) {
                    testInv.set(i, stack);
                    added++;
                    continue stacks;
                } else if (ItemHandlerHelper.canItemStacksStack(existingStack, stack) && existingStack.getCount() + stack.getCount() <= stack.getMaxStackSize()) {
                    existingStack.grow(stack.getCount());
                    added++;
                    continue stacks;
                }
            }
        }
        return added == stacks.size();

    }

    public TileEntity getPullInv() {
        return Coord4D.get(this).translate(0, 2, 0).getTileEntity(getWorld());
    }

    public TileEntity getEjectInv() {
        final Direction side = getOppositeDirection();
        final BlockPos pos = getPos().up().offset(side, 2);
        if (world.isAreaLoaded(pos, 0)) {
            return world.getTileEntity(pos);
        }
        return null;
    }

    public void add(List<ItemStack> stacks) {
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
        if (searcher.state == State.IDLE) {
            searcher.start();
        }
        running = true;
        MekanismUtils.saveChunk(this);
    }

    public void stop() {
        if (searcher.state == State.SEARCHING) {
            searcher.interrupt();
            reset();
            return;
        } else if (searcher.state == State.FINISHED) {
            running = false;
        }
        MekanismUtils.saveChunk(this);
    }

    public void reset() {
        searcher = new ThreadMinerSearch(this);
        running = false;
        oresToMine.clear();
        replaceMap.clear();
        missingStack = ItemStack.EMPTY;
        setActive(false);
        MekanismUtils.saveChunk(this);
    }

    public boolean isReplaceStack(ItemStack stack) {
        for (MinerFilter filter : filters) {
            if (!filter.replaceStack.isEmpty() && filter.replaceStack.isItemEqual(stack)) {
                return true;
            }
        }
        return false;
    }

    public int getSize() {
        int size = 0;
        for (Chunk3D chunk : oresToMine.keySet()) {
            size += oresToMine.get(chunk).cardinality();
        }
        return size;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        running = nbtTags.getBoolean("running");
        delay = nbtTags.getInt("delay");
        numPowering = nbtTags.getInt("numPowering");
        searcher.state = State.values()[nbtTags.getInt("state")];
        setConfigurationData(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (searcher.state == State.SEARCHING) {
            reset();
        }
        nbtTags.putBoolean("running", running);
        nbtTags.putInt("delay", delay);
        nbtTags.putInt("numPowering", numPowering);
        nbtTags.putInt("state", searcher.state.ordinal());
        return getConfigurationData(nbtTags);
    }

    private void readBasicData(PacketBuffer dataStream) {
        setRadius(dataStream.readInt());//client allowed to use whatever server sends
        minY = dataStream.readInt();
        maxY = dataStream.readInt();
        doEject = dataStream.readBoolean();
        doPull = dataStream.readBoolean();
        running = dataStream.readBoolean();
        silkTouch = dataStream.readBoolean();
        numPowering = dataStream.readInt();
        searcher.state = dataStream.readEnumValue(State.class);
        clientToMine = dataStream.readInt();
        inverse = dataStream.readBoolean();
        missingStack = dataStream.readItemStack();
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
                    for (PlayerEntity player : playersUsing) {
                        //TODO: I believe this is meant to sync the changes to all the players currently using the inventory
                        //openInventory(player);
                    }
                    break;
                }
                case 12: {
                    // Move filter down
                    int filterIndex = dataStream.readInt();
                    filters.swap(filterIndex, filterIndex + 1);
                    for (PlayerEntity player : playersUsing) {
                        //TODO: I believe this is meant to sync the changes to all the players currently using the inventory
//                        openInventory(player);
                    }
                    break;
                }
            }

            MekanismUtils.saveChunk(this);
            for (PlayerEntity player : playersUsing) {
                Mekanism.packetHandler.sendTo(new PacketTileEntity(this, getGenericPacket(new TileNetworkList())), (ServerPlayerEntity) player);
            }
            return;
        }

        boolean wasActive = getActive();
        super.handlePacketData(dataStream);

        if (isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                readBasicData(dataStream);
                filters.clear();
                int amount = dataStream.readInt();
                for (int i = 0; i < amount; i++) {
                    filters.add(MinerFilter.readFromPacket(dataStream));
                }
            } else if (type == 1) {
                readBasicData(dataStream);
            } else if (type == 2) {
                filters.clear();
                int amount = dataStream.readInt();
                for (int i = 0; i < amount; i++) {
                    filters.add(MinerFilter.readFromPacket(dataStream));
                }
            } else if (type == 3) {
                running = dataStream.readBoolean();
                clientToMine = dataStream.readInt();
                missingStack = dataStream.readItemStack();
            }
            //TODO: Does this get handled by TileEntityMekanism
            if (wasActive != getActive()) {
                MekanismUtils.updateBlock(getWorld(), getPos());
            }
        }
    }

    private void addBasicData(TileNetworkList data) {
        data.add(radius);
        data.add(minY);
        data.add(maxY);
        data.add(doEject);
        data.add(doPull);
        data.add(running);
        data.add(silkTouch);
        data.add(numPowering);
        data.add(searcher.state);

        if (searcher.state == State.SEARCHING) {
            data.add(searcher.found);
        } else {
            data.add(getSize());
        }

        data.add(inverse);
        data.add(missingStack);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(0);
        addBasicData(data);
        data.add(filters.size());
        for (MinerFilter filter : filters) {
            filter.write(data);
        }
        return data;
    }

    public TileNetworkList getSmallPacket(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(3);

        data.add(running);

        if (searcher.state == State.SEARCHING) {
            data.add(searcher.found);
        } else {
            data.add(getSize());
        }
        data.add(missingStack);
        return data;
    }

    public TileNetworkList getGenericPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(1);
        addBasicData(data);
        return data;
    }

    @Override
    public TileNetworkList getFilterPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(2);
        data.add(filters.size());
        for (MinerFilter filter : filters) {
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

    public Coord4D getStartingCoord() {
        return new Coord4D(getPos().getX() - radius, minY, getPos().getZ() - radius, world.getDimension().getType());
    }

    public Coord4D getCoordFromIndex(int index) {
        int diameter = getDiameter();
        Coord4D start = getStartingCoord();
        int x = start.x + index % diameter;
        int y = start.y + (index / diameter / diameter);
        int z = start.z + (index / diameter) % diameter;
        return new Coord4D(x, y, z, world.getDimension().getType());
    }

    @Override
    public boolean isPowered() {
        return redstone || numPowering > 0;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void onPlace() {
        for (int x = -1; x <= +1; x++) {
            for (int y = 0; y <= +1; y++) {
                for (int z = -1; z <= +1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockPos pos1 = getPos().add(x, y, z);
                    MekanismUtils.makeAdvancedBoundingBlock(world, pos1, Coord4D.get(this));
                    world.notifyNeighborsOfStateChange(pos1, getBlockType());
                }
            }
        }
    }

    @Override
    public void onBreak() {
        for (int x = -1; x <= +1; x++) {
            for (int y = 0; y <= +1; y++) {
                for (int z = -1; z <= +1; z++) {
                    world.removeBlock(getPos().add(x, y, z), false);
                }
            }
        }
    }

    public TileEntity getEjectTile() {
        BlockPos pos = getPos().up().offset(getOppositeDirection());
        if (world != null && world.isAreaLoaded(pos, 0)) {
            return world.getTileEntity(pos);
        }
        return null;
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
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (method == 0) {
            if (arguments.length != 1 || !(arguments[0] instanceof Double)) {
                return new Object[]{"Invalid parameters."};
            }
            setRadius(Math.min(((Double) arguments[0]).intValue(), MekanismConfig.general.digitalMinerMaxRadius.get()));
        } else if (method == 1) {
            if (arguments.length != 1 || !(arguments[0] instanceof Double)) {
                return new Object[]{"Invalid parameters."};
            }
            minY = ((Double) arguments[0]).intValue();
        } else if (method == 2) {
            if (arguments.length != 1 || !(arguments[0] instanceof Double)) {
                return new Object[]{"Invalid parameters."};
            }
            maxY = ((Double) arguments[0]).intValue();
        } else if (method == 3) {
            if (arguments.length < 1 || !(arguments[0] instanceof String)) {
                return new Object[]{"Invalid parameters."};
            }
            Item item = ItemRegistryUtils.getByName((String) arguments[0]);
            if (item != Items.AIR) {
                filters.add(new MItemStackFilter(new ItemStack(item)));
            }
            return new Object[]{"Added filter."};
        } else if (method == 4) {
            if (arguments.length < 1 || !(arguments[0] instanceof Double)) {
                return new Object[]{"Invalid parameters."};
            }
            int id = ((Double) arguments[0]).intValue();
            Iterator<MinerFilter> iter = filters.iterator();
            while (iter.hasNext()) {
                MinerFilter filter = iter.next();
                if (filter instanceof MItemStackFilter) {
                    if (MekanismUtils.getID(((MItemStackFilter) filter).getItemStack()) == id) {
                        iter.remove();
                        return new Object[]{"Removed filter."};
                    }
                }
            }
            return new Object[]{"Couldn't find filter."};
        } else if (method == 5) {
            if (arguments.length < 1 || !(arguments[0] instanceof String)) {
                return new Object[]{"Invalid parameters."};
            }
            String ore = (String) arguments[0];
            MOreDictFilter filter = new MOreDictFilter();
            filter.setOreDictName(ore);
            filters.add(filter);
            return new Object[]{"Added filter."};
        } else if (method == 6) {
            if (arguments.length < 1 || !(arguments[0] instanceof String)) {
                return new Object[]{"Invalid parameters."};
            }
            String ore = (String) arguments[0];
            Iterator<MinerFilter> iter = filters.iterator();
            while (iter.hasNext()) {
                MinerFilter filter = iter.next();
                if (filter instanceof MOreDictFilter) {
                    if (((MOreDictFilter) filter).getOreDictName().equals(ore)) {
                        iter.remove();
                        return new Object[]{"Removed filter."};
                    }
                }
            }
            return new Object[]{"Couldn't find filter."};
        } else if (method == 7) {
            reset();
            return new Object[]{"Reset miner."};
        } else if (method == 8) {
            start();
            return new Object[]{"Started miner."};
        } else if (method == 9) {
            stop();
            return new Object[]{"Stopped miner."};
        } else if (method == 10) {
            return new Object[]{searcher != null ? searcher.found : 0};
        }
        for (PlayerEntity player : playersUsing) {
            Mekanism.packetHandler.sendTo(new PacketTileEntity(this, getGenericPacket(new TileNetworkList())), (ServerPlayerEntity) player);
        }
        return null;
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        nbtTags.putInt("radius", radius);
        nbtTags.putInt("minY", minY);
        nbtTags.putInt("maxY", maxY);
        nbtTags.putBoolean("doEject", doEject);
        nbtTags.putBoolean("doPull", doPull);
        nbtTags.putBoolean("silkTouch", silkTouch);
        nbtTags.putBoolean("inverse", inverse);
        ListNBT filterTags = new ListNBT();
        for (MinerFilter filter : filters) {
            filterTags.add(filter.write(new CompoundNBT()));
        }
        if (!filterTags.isEmpty()) {
            nbtTags.put("filters", filterTags);
        }
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        setRadius(Math.min(nbtTags.getInt("radius"), MekanismConfig.general.digitalMinerMaxRadius.get()));
        minY = nbtTags.getInt("minY");
        maxY = nbtTags.getInt("maxY");
        doEject = nbtTags.getBoolean("doEject");
        doPull = nbtTags.getBoolean("doPull");
        silkTouch = nbtTags.getBoolean("silkTouch");
        inverse = nbtTags.getBoolean("inverse");
        if (nbtTags.contains("filters")) {
            ListNBT tagList = nbtTags.getList("filters", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                filters.add(MinerFilter.readFromNBT(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey();
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setBoolean(itemStack, "hasMinerConfig", true);

        ItemDataUtils.setInt(itemStack, "radius", radius);
        ItemDataUtils.setInt(itemStack, "minY", minY);
        ItemDataUtils.setInt(itemStack, "maxY", maxY);
        ItemDataUtils.setBoolean(itemStack, "doEject", doEject);
        ItemDataUtils.setBoolean(itemStack, "doPull", doPull);
        ItemDataUtils.setBoolean(itemStack, "silkTouch", silkTouch);
        ItemDataUtils.setBoolean(itemStack, "inverse", inverse);

        ListNBT filterTags = new ListNBT();

        for (MinerFilter filter : filters) {
            filterTags.add(filter.write(new CompoundNBT()));
        }

        if (!filterTags.isEmpty()) {
            ItemDataUtils.setList(itemStack, "filters", filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "hasMinerConfig")) {
            setRadius(Math.min(ItemDataUtils.getInt(itemStack, "radius"), MekanismConfig.general.digitalMinerMaxRadius.get()));
            minY = ItemDataUtils.getInt(itemStack, "minY");
            maxY = ItemDataUtils.getInt(itemStack, "maxY");
            doEject = ItemDataUtils.getBoolean(itemStack, "doEject");
            doPull = ItemDataUtils.getBoolean(itemStack, "doPull");
            silkTouch = ItemDataUtils.getBoolean(itemStack, "silkTouch");
            inverse = ItemDataUtils.getBoolean(itemStack, "inverse");

            if (ItemDataUtils.hasData(itemStack, "filters")) {
                ListNBT tagList = ItemDataUtils.getList(itemStack, "filters");
                for (int i = 0; i < tagList.size(); i++) {
                    filters.add(MinerFilter.readFromNBT(tagList.getCompound(i)));
                }
            }
        }
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            delayLength = MekanismUtils.getTicks(this, BASE_DELAY);
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getOffsetCapability(@Nonnull Capability<T> capability, Direction side, @Nonnull Vec3i offset) {
        if (isOffsetCapabilityDisabled(capability, side, offset)) {
            return LazyOptional.empty();
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> getItemHandler(side)));
        }
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY) {
            return Capabilities.ENERGY_ACCEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
            return Capabilities.ENERGY_OUTPUTTER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> forgeEnergyManager.getWrapper(this, side)));
        }
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
        }
        if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY) {
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
    public TileComponentChunkLoader getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        if (chunkSet == null) {
            chunkSet = new Range4D(Coord4D.get(this)).expandFromCenter(radius).getIntersectingChunks().stream().map(Chunk3D::getPos).collect(Collectors.toSet());
        }
        return chunkSet;
    }

    @Override
    public HashList<MinerFilter> getFilters() {
        return filters;
    }
}