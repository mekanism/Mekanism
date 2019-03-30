package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MinerUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityDigitalMiner extends TileEntityElectricBlock implements IUpgradeTile, IRedstoneControl,
      IActiveState, ISustainedData, IChunkLoader, IAdvancedBoundingBlock {

    private static final int[] INV_SLOTS = IntStream.range(0, 28).toArray();

    public static int[] EJECT_INV;
    public final double BASE_ENERGY_USAGE = usage.digitalMinerUsage;
    public Map<Chunk3D, BitSet> oresToMine = new HashMap<>();
    public Map<Integer, MinerFilter> replaceMap = new HashMap<>();
    public HashList<MinerFilter> filters = new HashList<>();
    public ThreadMinerSearch searcher = new ThreadMinerSearch(this);
    public double energyUsage = usage.digitalMinerUsage;

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

    public boolean isActive;
    public boolean clientActive;

    public boolean silkTouch;

    public boolean running;

    public double prevEnergy;

    public int delayTicks;

    public boolean initCalc = false;

    public int numPowering;

    public boolean clientRendering = false;

    private Set<ChunkPos> chunkSet;

    /**
     * This machine's current RedstoneControl type.
     */
    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, INV_SLOTS.length);
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);
    public TileComponentChunkLoader chunkLoaderComponent = new TileComponentChunkLoader(this);
    public String[] methods = {"setRadius", "setMin", "setMax", "addFilter", "removeFilter", "addOreFilter",
          "removeOreFilter", "reset", "start", "stop", "getToMine"};

    public TileEntityDigitalMiner() {
        super("DigitalMiner", BlockStateMachine.MachineType.DIGITAL_MINER.baseEnergy);
        inventory = NonNullList.withSize(INV_SLOTS.length + 1, ItemStack.EMPTY);
        radius = 10;

        upgradeComponent.setSupported(Upgrade.ANCHOR);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (getActive()) {
            for (EntityPlayer player : new HashSet<>(playersUsing)) {
                if (player.openContainer instanceof ContainerNull || player.openContainer instanceof ContainerFilter) {
                    player.closeScreen();
                }
            }
        }

        if (!world.isRemote) {
            if (!initCalc) {
                if (searcher.state == State.FINISHED) {
                    boolean prevRunning = running;

                    reset();
                    start();

                    running = prevRunning;
                }

                initCalc = true;
            }

            ChargeUtils.discharge(27, this);

            if (MekanismUtils.canFunction(this) && running && getEnergy() >= getPerTick()
                  && searcher.state == State.FINISHED && oresToMine.size() > 0) {
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

                            IBlockState state = coord.getBlockState(world);
                            Block block = state.getBlock();
                            int meta = block.getMetaFromState(state);

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
                            ItemStack is = new ItemStack(block, 1, meta);

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

                            List<ItemStack> drops = MinerUtils.getDrops(world, coord, silkTouch);

                            if (canInsert(drops) && setReplace(coord, index)) {
                                did = true;
                                add(drops);
                                set.clear(index);

                                if (set.cardinality() == 0) {
                                    it.remove();
                                }

                                world.playEvent(null, 2001, coord.getPos(), Block.getStateId(state));

                                missingStack = ItemStack.EMPTY;
                            }

                            break;
                        }
                    }

                    delay = getDelay();
                }
            } else {
                if (prevEnergy >= getEnergy()) {
                    setActive(false);
                }
            }

            TransitRequest ejectMap = getEjectItemMap();

            if (doEject && delayTicks == 0 && !ejectMap.isEmpty()) {
                TileEntity ejectInv = getEjectInv();
                TileEntity ejectTile = getEjectTile();
                if (ejectInv != null && ejectTile != null) {
                    TransitResponse response;
                    if (CapabilityUtils.hasCapability(ejectInv, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY,
                          facing.getOpposite())) {
                        response = TransporterUtils.insert(ejectTile, CapabilityUtils
                              .getCapability(ejectInv, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY,
                                    facing.getOpposite()), ejectMap, null, true, 0);
                    } else {
                        response = InventoryUtils.putStackInInventory(ejectInv, ejectMap, facing.getOpposite(), false);
                    }
                    if (!response.isEmpty()) {
                        response.getInvStack(this, facing.getOpposite()).use();
                    }

                    delayTicks = 10;
                }
            } else if (delayTicks > 0) {
                delayTicks--;
            }

            if (playersUsing.size() > 0) {
                for (EntityPlayer player : playersUsing) {
                    Mekanism.packetHandler
                          .sendTo(new TileEntityMessage(Coord4D.get(this), getSmallPacket(new TileNetworkList())),
                                (EntityPlayerMP) player);
                }
            }

            prevEnergy = getEnergy();
        }
    }

    public double getPerTick() {
        double ret = energyUsage;

        if (silkTouch) {
            ret *= general.minerSilkMultiplier;
        }

        int baseRad = Math.max(radius - 10, 0);
        ret *= (1 + ((float) baseRad / 22F));

        int baseHeight = Math.max((maxY - minY) - 60, 0);
        ret *= (1 + ((float) baseHeight / 195F));

        return ret;
    }

    public int getDelay() {
        return delayLength;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int newRadius) {
        boolean changed = (radius != newRadius);
        radius = newRadius;

        // If the radius changed and we're on the server, go ahead and refresh
        // the chunk set
        if (changed && hasWorld() && world.isRemote) {
            chunkSet = null;
            getChunkSet();
        }
    }

    /*
     * returns false if unsuccessful
     */
    public boolean setReplace(Coord4D obj, int index) {
        ItemStack stack = getReplace(index);

        if (!stack.isEmpty()) {
            world.setBlockState(obj.getPos(),
                  Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getItemDamage()), 3);

            IBlockState s = obj.getBlockState(world);
            if (s.getBlock() instanceof BlockBush && !((BlockBush) s.getBlock())
                  .canBlockStay(world, obj.getPos(), s)) {
                s.getBlock().dropBlockAsItem(world, obj.getPos(), s, 1);
                world.setBlockToAir(obj.getPos());
            }

            return true;
        } else {
            MinerFilter filter = replaceMap.get(index);

            if (filter == null || (filter.replaceStack.isEmpty() || !filter.requireStack)) {
                world.setBlockToAir(obj.getPos());

                return true;
            }

            missingStack = filter.replaceStack;

            return false;
        }
    }

    private boolean canMine(Coord4D coord) {
        IBlockState state = coord.getBlockState(world);

        EntityPlayer dummy = Mekanism.proxy.getDummyPlayer((WorldServer) world, pos).get();
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, coord.getPos(), state, dummy);
        MinecraftForge.EVENT_BUS.post(event);

        return !event.isCanceled();
    }

    public ItemStack getReplace(int index) {
        MinerFilter filter = replaceMap.get(index);

        if (filter == null || filter.replaceStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < 27; i++) {
            if (!inventory.get(i).isEmpty() && inventory.get(i).isItemEqual(filter.replaceStack)) {
                inventory.get(i).shrink(1);

                return StackUtils.size(filter.replaceStack, 1);
            }
        }

        if (doPull && getPullInv() != null) {
            InvStack stack = InventoryUtils
                  .takeDefinedItem(getPullInv(), EnumFacing.UP, filter.replaceStack.copy(), 1, 1);

            if (stack != null) {
                stack.use();
                return StackUtils.size(filter.replaceStack, 1);
            }
        }

        return ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> copy(NonNullList<ItemStack> stacks) {
        NonNullList<ItemStack> toReturn = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);

        for (int i = 0; i < stacks.size(); i++) {
            toReturn.set(i, !stacks.get(i).isEmpty() ? stacks.get(i).copy() : ItemStack.EMPTY);
        }

        return toReturn;
    }

    public TransitRequest getEjectItemMap() {
        TransitRequest request = new TransitRequest();

        for (int i = 27 - 1; i >= 0; i--) {
            ItemStack stack = inventory.get(i);

            if (!stack.isEmpty()) {
                if (isReplaceStack(stack)) {
                    continue;
                }

                if (!request.hasType(stack)) {
                    request.addItem(stack, i);
                }
            }
        }

        return request;
    }

    public boolean canInsert(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return true;
        }

        NonNullList<ItemStack> testInv = copy(inventory);

        int added = 0;

        stacks:
        for (ItemStack stack : stacks) {
            stack = stack.copy();

            if (stack.isEmpty()) {
                continue;
            }

            for (int i = 0; i < 27; i++) {
                if (testInv.get(i).isEmpty()) {
                    testInv.set(i, stack);
                    added++;

                    continue stacks;
                } else if (testInv.get(i).isItemEqual(stack)
                      && testInv.get(i).getCount() + stack.getCount() <= stack
                      .getMaxStackSize()) {
                    testInv.get(i).grow(stack.getCount());
                    added++;

                    continue stacks;
                }
            }
        }

        return added == stacks.size();

    }

    public TileEntity getPullInv() {
        return Coord4D.get(this).translate(0, 2, 0).getTileEntity(world);
    }

    public TileEntity getEjectInv() {
        EnumFacing side = facing.getOpposite();

        return world.getTileEntity(getPos().up().offset(side, 2));
    }

    public void add(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return;
        }

        stacks:
        for (ItemStack stack : stacks) {
            for (int i = 0; i < 27; i++) {
                if (inventory.get(i).isEmpty()) {
                    inventory.set(i, stack);

                    continue stacks;
                } else if (inventory.get(i).isItemEqual(stack)
                      && inventory.get(i).getCount() + stack.getCount() <= stack.getMaxStackSize()) {
                    inventory.get(i).grow(stack.getCount());

                    continue stacks;
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
    public void openInventory(@Nonnull EntityPlayer player) {
        super.openInventory(player);

        if (!world.isRemote) {
            Mekanism.packetHandler
                  .sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        (EntityPlayerMP) player);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        isActive = nbtTags.getBoolean("isActive");
        running = nbtTags.getBoolean("running");
        delay = nbtTags.getInteger("delay");
        numPowering = nbtTags.getInteger("numPowering");
        searcher.state = State.values()[nbtTags.getInteger("state")];
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        setConfigurationData(nbtTags);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        if (searcher.state == State.SEARCHING) {
            reset();
        }

        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setBoolean("running", running);
        nbtTags.setInteger("delay", delay);
        nbtTags.setInteger("numPowering", numPowering);
        nbtTags.setInteger("state", searcher.state.ordinal());
        nbtTags.setInteger("controlType", controlType.ordinal());
        return getConfigurationData(nbtTags);
    }

    private void readBasicData(ByteBuf dataStream) {
        setRadius(dataStream.readInt());//client allowed to use whatever server sends
        minY = dataStream.readInt();
        maxY = dataStream.readInt();
        doEject = dataStream.readBoolean();
        doPull = dataStream.readBoolean();
        clientActive = dataStream.readBoolean();
        running = dataStream.readBoolean();
        silkTouch = dataStream.readBoolean();
        numPowering = dataStream.readInt();
        searcher.state = State.values()[dataStream.readInt()];
        clientToMine = dataStream.readInt();
        controlType = RedstoneControl.values()[dataStream.readInt()];
        inverse = dataStream.readBoolean();

        if (dataStream.readBoolean()) {
            missingStack = new ItemStack(Item.getItemById(dataStream.readInt()), 1, dataStream.readInt());
        } else {
            missingStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
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
                    setRadius(Math.min(dataStream.readInt(), general.digitalMinerMaxRadius));
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

                    for (EntityPlayer player : playersUsing) {
                        openInventory(player);
                    }

                    break;
                }
                case 12: {
                    // Move filter down
                    int filterIndex = dataStream.readInt();
                    filters.swap(filterIndex, filterIndex + 1);

                    for (EntityPlayer player : playersUsing) {
                        openInventory(player);
                    }

                    break;
                }
            }

            MekanismUtils.saveChunk(this);

            for (EntityPlayer player : playersUsing) {
                Mekanism.packetHandler
                      .sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new TileNetworkList())),
                            (EntityPlayerMP) player);
            }

            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
                clientActive = dataStream.readBoolean();
                running = dataStream.readBoolean();
                clientToMine = dataStream.readInt();

                if (dataStream.readBoolean()) {
                    missingStack = new ItemStack(Item.getItemById(dataStream.readInt()), 1, dataStream.readInt());
                } else {
                    missingStack = ItemStack.EMPTY;
                }
            }

            if (clientActive != isActive) {
                isActive = clientActive;
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    private void addBasicData(TileNetworkList data) {
        data.add(radius);
        data.add(minY);
        data.add(maxY);
        data.add(doEject);
        data.add(doPull);
        data.add(isActive);
        data.add(running);
        data.add(silkTouch);
        data.add(numPowering);
        data.add(searcher.state.ordinal());

        if (searcher.state == State.SEARCHING) {
            data.add(searcher.found);
        } else {
            data.add(getSize());
        }

        data.add(controlType.ordinal());
        data.add(inverse);

        if (!missingStack.isEmpty()) {
            data.add(true);
            data.add(MekanismUtils.getID(missingStack));
            data.add(missingStack.getItemDamage());
        } else {
            data.add(false);
        }
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

        data.add(isActive);
        data.add(running);

        if (searcher.state == State.SEARCHING) {
            data.add(searcher.found);
        } else {
            data.add(getSize());
        }

        if (!missingStack.isEmpty()) {
            data.add(true);
            data.add(MekanismUtils.getID(missingStack));
            data.add(missingStack.getItemDamage());
        } else {
            data.add(false);
        }

        return data;
    }

    public TileNetworkList getGenericPacket(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(1);
        addBasicData(data);
        return data;
    }

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
        return new Coord4D(getPos().getX() - radius, minY, getPos().getZ() - radius, world.provider.getDimension());
    }

    public Coord4D getCoordFromIndex(int index) {
        int diameter = getDiameter();
        Coord4D start = getStartingCoord();

        int x = start.x + index % diameter;
        int y = start.y + (index / diameter / diameter);
        int z = start.z + (index / diameter) % diameter;

        return new Coord4D(x, y, z, world.provider.getDimension());
    }

    @Override
    public boolean isPowered() {
        return redstone || numPowering > 0;
    }

    @Override
    public boolean canPulse() {
        return false;
    }

    @Override
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type) {
        controlType = type;
        MekanismUtils.saveChunk(this);
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;

        if (clientActive != active) {
            Mekanism.packetHandler
                  .sendToReceivers(
                        new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));

            clientActive = active;
        }
    }

    @Override
    public boolean renderUpdate() {
        return false;
    }

    @Override
    public boolean lightUpdate() {
        return false;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
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
                    world.notifyNeighborsOfStateChange(pos1, getBlockType(), true);
                }
            }
        }
    }

    @Override
    public boolean canSetFacing(int side) {
        return side != 0 && side != 1;
    }

    @Override
    public void onBreak() {
        for (int x = -1; x <= +1; x++) {
            for (int y = 0; y <= +1; y++) {
                for (int z = -1; z <= +1; z++) {
                    world.setBlockToAir(getPos().add(x, y, z));
                }
            }
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        //Allow for automation via the top (as that is where it can auto pull from)
        return side == EnumFacing.UP || side == facing.getOpposite() ? INV_SLOTS : InventoryUtils.EMPTY;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack stack) {
        return slotID != 27 || ChargeUtils.canBeDischarged(stack);
    }

    public TileEntity getEjectTile() {
        EnumFacing side = facing.getOpposite();
        return world.getTileEntity(getPos().up().offset(side));
    }

    @Override
    public int[] getBoundSlots(BlockPos location, EnumFacing side) {
        EnumFacing dir = facing.getOpposite();

        BlockPos pull = getPos().up();
        BlockPos eject = pull.offset(dir);

        if ((location.equals(eject) && side == dir) || (location.equals(pull) && side == EnumFacing.UP)) {
            if (EJECT_INV == null) {
                EJECT_INV = new int[27];

                for (int i = 0; i < EJECT_INV.length; i++) {
                    EJECT_INV[i] = i;
                }
            }

            return EJECT_INV;
        }

        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean canBoundInsert(BlockPos location, int i, ItemStack itemstack) {
        EnumFacing side = facing.getOpposite();

        BlockPos pull = getPos().up();
        BlockPos eject = pull.offset(side);

        if (location.equals(eject)) {
            return false;
        } else if (location.equals(pull)) {
            return !itemstack.isEmpty() && isReplaceStack(itemstack);
        }

        return false;
    }

    @Override
    public boolean canBoundExtract(BlockPos location, int i, ItemStack itemstack, EnumFacing dir) {
        EnumFacing side = facing.getOpposite();

        BlockPos pull = getPos().up();
        BlockPos eject = pull.offset(side);

        if (location.equals(eject)) {
            return itemstack.isEmpty() || !isReplaceStack(itemstack);
        } else if (location.equals(pull)) {
            return false;
        }

        return false;
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
    public Object[] invoke(int method, Object[] arguments) {
        if (method == 0) {
            if (arguments.length != 1 || !(arguments[0] instanceof Double)) {
                return new Object[]{"Invalid parameters."};
            }

            setRadius(Math.min(((Double) arguments[0]).intValue(), general.digitalMinerMaxRadius));
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
            if (arguments.length < 1 || !(arguments[0] instanceof Double)) {
                return new Object[]{"Invalid parameters."};
            }

            int id = ((Double) arguments[0]).intValue();
            int meta = 0;

            if (arguments.length > 1) {
                if (arguments[1] instanceof Double) {
                    meta = ((Double) arguments[1]).intValue();
                }
            }

            filters.add(new MItemStackFilter(new ItemStack(Item.getItemById(id), 1, meta)));

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
                    if (MekanismUtils.getID(((MItemStackFilter) filter).itemType) == id) {
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

        for (EntityPlayer player : playersUsing) {
            Mekanism.packetHandler
                  .sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new TileNetworkList())),
                        (EntityPlayerMP) player);
        }

        return null;
    }

    @Override
    public NBTTagCompound getConfigurationData(NBTTagCompound nbtTags) {
        nbtTags.setInteger("radius", radius);
        nbtTags.setInteger("minY", minY);
        nbtTags.setInteger("maxY", maxY);
        nbtTags.setBoolean("doEject", doEject);
        nbtTags.setBoolean("doPull", doPull);
        nbtTags.setBoolean("silkTouch", silkTouch);
        nbtTags.setBoolean("inverse", inverse);

        NBTTagList filterTags = new NBTTagList();

        for (MinerFilter filter : filters) {
            filterTags.appendTag(filter.write(new NBTTagCompound()));
        }

        if (filterTags.tagCount() != 0) {
            nbtTags.setTag("filters", filterTags);
        }

        return nbtTags;
    }

    @Override
    public void setConfigurationData(NBTTagCompound nbtTags) {
        setRadius(Math.min(nbtTags.getInteger("radius"), general.digitalMinerMaxRadius));
        minY = nbtTags.getInteger("minY");
        maxY = nbtTags.getInteger("maxY");
        doEject = nbtTags.getBoolean("doEject");
        doPull = nbtTags.getBoolean("doPull");
        silkTouch = nbtTags.getBoolean("silkTouch");
        inverse = nbtTags.getBoolean("inverse");

        if (nbtTags.hasKey("filters")) {
            NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                filters.add(MinerFilter.readFromNBT(tagList.getCompoundTagAt(i)));
            }
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey() + "." + fullName + ".name";
    }

    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setBoolean(itemStack, "hasMinerConfig", true);

        ItemDataUtils.setInt(itemStack, "radius", radius);
        ItemDataUtils.setInt(itemStack, "minY", minY);
        ItemDataUtils.setInt(itemStack, "maxY", maxY);
        ItemDataUtils.setBoolean(itemStack, "doEject", doEject);
        ItemDataUtils.setBoolean(itemStack, "doPull", doPull);
        ItemDataUtils.setBoolean(itemStack, "silkTouch", silkTouch);
        ItemDataUtils.setBoolean(itemStack, "inverse", inverse);

        NBTTagList filterTags = new NBTTagList();

        for (MinerFilter filter : filters) {
            filterTags.appendTag(filter.write(new NBTTagCompound()));
        }

        if (filterTags.tagCount() != 0) {
            ItemDataUtils.setList(itemStack, "filters", filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "hasMinerConfig")) {
            setRadius(Math.min(ItemDataUtils.getInt(itemStack, "radius"), general.digitalMinerMaxRadius));
            minY = ItemDataUtils.getInt(itemStack, "minY");
            maxY = ItemDataUtils.getInt(itemStack, "maxY");
            doEject = ItemDataUtils.getBoolean(itemStack, "doEject");
            doPull = ItemDataUtils.getBoolean(itemStack, "doPull");
            silkTouch = ItemDataUtils.getBoolean(itemStack, "silkTouch");
            inverse = ItemDataUtils.getBoolean(itemStack, "inverse");

            if (ItemDataUtils.hasData(itemStack, "filters")) {
                NBTTagList tagList = ItemDataUtils.getList(itemStack, "filters");

                for (int i = 0; i < tagList.tagCount(); i++) {
                    filters.add(MinerFilter.readFromNBT(tagList.getCompoundTagAt(i)));
                }
            }
        }
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);

        switch (upgrade) {
            case SPEED:
                delayLength = MekanismUtils.getTicks(this, BASE_DELAY);
            case ENERGY:
                energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);
                maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
                setEnergy(Math.min(getMaxEnergy(), getEnergy()));
            default:
                break;
        }
    }

    @Override
    public boolean canBoundReceiveEnergy(BlockPos coord, EnumFacing side) {
        EnumFacing left = MekanismUtils.getLeft(facing);
        EnumFacing right = MekanismUtils.getRight(facing);

        if (coord.equals(getPos().offset(left))) {
            return side == left;
        } else if (coord.equals(getPos().offset(right))) {
            return side == right;
        }

        return false;
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
        return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing)
              || side == EnumFacing.DOWN;
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.CONFIG_CARD_CAPABILITY
              || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY
              || super.hasCapability(capability, side);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY
              || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean hasOffsetCapability(@Nonnull Capability<?> capability, EnumFacing side, @Nonnull Vec3i offset) {
        if (isOffsetCapabilityDisabled(capability, side, offset)) {
            return false;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        } else if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY || isTesla(capability, side)) {
            return true;
        }
        return hasCapability(capability, side);
    }

    @Override
    public <T> T getOffsetCapability(@Nonnull Capability<T> capability, EnumFacing side, @Nonnull Vec3i offset) {
        if (isOffsetCapabilityDisabled(capability, side, offset)) {
            return null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getItemHandler(side));
        } else if (isStrictEnergy(capability)) {
            return (T) this;
        } else if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(getForgeEnergyWrapper(side));
        } else if (isTesla(capability, side)) {
            return (T) getTeslaEnergyWrapper(side);
        }
        return getCapability(capability, side);
    }

    @Override
    public boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side,
          @Nonnull Vec3i offset) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //Input
            if (offset.equals(new Vec3i(0, 1, 0))) {
                //If input then disable if wrong face of input
                return side != EnumFacing.UP;
            }
            //Output
            EnumFacing back = facing.getOpposite();
            if (offset.equals(new Vec3i(back.getXOffset(), 1, back.getZOffset()))) {
                //If output then disable if wrong face of output
                return side != back;
            }
            return true;
        }
        if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY || isTesla(capability, side)) {
            if (offset.equals(Vec3i.NULL_VECTOR)) {
                //Disable if it is the bottom port but wrong side of it
                return side != EnumFacing.DOWN;
            }
            EnumFacing left = MekanismUtils.getLeft(facing);
            EnumFacing right = MekanismUtils.getRight(facing);
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
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        //Return some capabilities as disabled, and handle them with offset capabilities instead
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        } else if (isStrictEnergy(capability) || capability == CapabilityEnergy.ENERGY || isTesla(capability, side)) {
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
            chunkSet = new Range4D(Coord4D.get(this)).expandFromCenter(radius).
                  getIntersectingChunks().stream().
                  map(Chunk3D::getPos).collect(Collectors.toSet());

        }
        return chunkSet;
    }
}
