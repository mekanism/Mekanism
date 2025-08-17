package mekanism.common.tile.machine;

import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.OverflowAware;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.block.BlockBounding;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MinerEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.MinerRegionCache;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.content.miner.ThreadMinerSearch.State;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.container.sync.SyncableRegistryEntry;
import mekanism.common.inventory.container.tile.DigitalMinerConfigContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityDigitalMiner extends TileEntityMekanism implements IChunkLoader, IBoundingBlock, ITileFilterHolder<MinerFilter<?>>, IHasVisualization {

    public static final int DEFAULT_HEIGHT_RANGE = 60;
    public static final int DEFAULT_RADIUS = 10;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final SortableFilterManager<MinerFilter<?>> filterManager = new SortableFilterManager<MinerFilter<?>>((Class) MinerFilter.class, this::markForSave);
    private Long2ObjectMap<BitSet> oresToMine = Long2ObjectMaps.emptyMap();
    public ThreadMinerSearch searcher = new ThreadMinerSearch(this);

    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> pullInventory;
    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> selfEjectInventory;
    @Nullable
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> ejectInventory;

    private int radius;
    private boolean inverse;
    private boolean inverseRequiresReplacement;
    private Item inverseReplaceTarget = Items.AIR;
    private int minY;
    private int maxY = minY + DEFAULT_HEIGHT_RANGE;
    private boolean doEject = false;
    private boolean doPull = false;
    public ItemStack missingStack = ItemStack.EMPTY;

    private final Predicate<ItemStack> overflowCollector = this::trackOverflow;
    //Note: Linked map to ensure each call to save is in the same order so that there is more uniformity
    private final Object2IntSortedMap<HashedItem> overflow = new Object2IntLinkedOpenHashMap<>();
    private boolean hasOverflow;
    private boolean recheckOverflow;

    private int delay;
    private int delayLength = MekanismConfig.general.minerTicksPerMine.get();
    private int cachedToMine;
    private boolean silkTouch;
    private boolean running;
    private int delayTicks;
    private boolean initCalc = false;
    private int numPowering;
    private boolean clientRendering;

    private final TileComponentChunkLoader<TileEntityDigitalMiner> chunkLoaderComponent = new TileComponentChunkLoader<>(this);
    @Nullable
    private ChunkPos targetChunk;

    private MinerEnergyContainer energyContainer;
    private List<IInventorySlot> mainSlots;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityDigitalMiner(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DIGITAL_MINER, pos, state);
        radius = DEFAULT_RADIUS;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = MinerEnergyContainer.input(this, listener), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        mainSlots = new ArrayList<>();
        IContentsListener mainSlotListener = () -> {
            listener.onContentsChanged();
            //Ensure we recheck if our overflow can fit anywhere
            recheckOverflow = true;
        };
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier, side -> side == RelativeSide.TOP, side -> side == RelativeSide.BACK);
        //Allow insertion manually or internally, or if it is a replace stack
        BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert = (stack, automationType) -> automationType != AutomationType.EXTERNAL || isReplaceTarget(stack.getItem());
        //Allow extraction if it is manual or for internal usage, or if it is not a replace stack
        //Note: We don't currently use internal for extraction anywhere here as we just shrink replace stacks directly
        BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract = (stack, automationType) -> automationType != AutomationType.EXTERNAL || !isReplaceTarget(stack.getItem());
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                BasicInventorySlot slot = BasicInventorySlot.at(canExtract, canInsert, mainSlotListener, 8 + slotX * 18, 92 + slotY * 18);
                builder.addSlot(slot, RelativeSide.BACK, RelativeSide.TOP);
                mainSlots.add(slot);
            }
        }
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 152, 20));
        return builder.build();
    }

    private void closeInvalidScreens() {
        if (getActive() && !playersUsing.isEmpty()) {
            for (Player player : new HashSet<>(playersUsing)) {
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
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        closeInvalidScreens();
        if (!initCalc) {
            //If it had finished searching, and we didn't initialize things yet,
            // reset it and start running again if needed. This happens after saving the miner to disk
            if (searcher.state == State.FINISHED) {
                boolean prevRunning = running;
                reset();
                start();
                running = prevRunning;
            }
            initCalc = true;
        }

        energySlot.fillContainerOrConvert();

        if (recheckOverflow) {
            //Try adding any overflow stacks we have before we actually try to process as if we have some overflow we can't add
            // then we will skip functioning and avoid draining energy.
            // Note: We may not have any overflow stacks, in which case this will effectively NO-OP
            // We also mark needing to recheck if the overflow can fit as false as we will know if we can or can't currently add it all
            tryAddOverflow();
        }

        //Note: If we have any overflow don't function or use any energy until the overflow has been dealt with
        if (!hasOverflow && canFunction() && running && searcher.state == State.FINISHED && !oresToMine.isEmpty()) {
            long energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) == energyPerTick) {
                setActive(true);
                if (delay > 0) {
                    delay--;
                }
                //TODO: Eventually we may want to avoid draining energy if we can't function due to a missing replace stack or the normal drops
                // being too much to fit
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                if (delay == 0) {
                    tryMineBlock();
                    delay = getDelay();
                }
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }

        if (doEject && delayTicks == 0) {
            Direction direction = getDirection();
            Direction oppositeDirection = direction.getOpposite();
            BlockPos ejectPos = getBlockPos().above().relative(oppositeDirection);
            if (selfEjectInventory == null) {
                selfEjectInventory = Capabilities.ITEM.createCache((ServerLevel) level, ejectPos, oppositeDirection);
            }
            IItemHandler ejectHandler = selfEjectInventory.getCapability();
            if (ejectInventory == null) {
                ejectInventory = Capabilities.ITEM.createCache((ServerLevel) level, ejectPos.relative(oppositeDirection), direction);
            }
            IItemHandler targetHandler = ejectInventory.getCapability();
            if (ejectHandler != null && targetHandler != null) {
                TransitRequest ejectMap = InventoryUtils.getEjectItemMap(ejectHandler, mainSlots);
                if (!ejectMap.isEmpty()) {
                    TransitResponse response = ejectMap.eject(this, ejectPos, targetHandler, 0, LogisticalTransporterBase::getColor);
                    if (!response.isEmpty()) {
                        response.useAll();
                    }
                }
            }
            delayTicks = MekanismUtils.TICKS_PER_HALF_SECOND;
        } else if (delayTicks > 0) {
            delayTicks--;
        }
        return sendUpdatePacket;
    }

    public void updateFromSearch(Long2ObjectMap<BitSet> oresToMine, int found) {
        this.oresToMine = oresToMine;
        cachedToMine = found;
        updateTargetChunk(null);
        markForSave();
    }

    public int getDelay() {
        return delayLength;
    }

    @ComputerMethod(methodDescription = "Whether Silk Touch mode is enabled or not")
    public boolean getSilkTouch() {
        return silkTouch;
    }

    @ComputerMethod(methodDescription = "Get the current radius configured (blocks)")
    public int getRadius() {
        return radius;
    }

    @ComputerMethod(methodDescription = "Gets the configured minimum Y level for mining")
    public int getMinY() {
        return minY;
    }

    @ComputerMethod(methodDescription = "Gets the configured maximum Y level for mining")
    public int getMaxY() {
        return maxY;
    }

    @ComputerMethod(nameOverride = "getInverseMode", methodDescription = "Whether Inverse Mode is enabled or not")
    public boolean getInverse() {
        return inverse;
    }

    @ComputerMethod(nameOverride = "getInverseModeRequiresReplacement", methodDescription = "Whether Inverse Mode Require Replacement is turned on")
    public boolean getInverseRequiresReplacement() {
        return inverseRequiresReplacement;
    }

    @ComputerMethod(nameOverride = "getInverseModeReplaceTarget", methodDescription = "Get the configured Replacement target item")
    public Item getInverseReplaceTarget() {
        return inverseReplaceTarget;
    }

    private void setSilkTouch(boolean newSilkTouch) {
        if (silkTouch != newSilkTouch) {
            silkTouch = newSilkTouch;
            if (hasLevel() && !isRemote()) {
                energyContainer.updateMinerEnergyPerTick();
            }
        }
    }

    public void toggleSilkTouch() {
        setSilkTouch(!getSilkTouch());
        markForSave();
    }

    public void toggleInverse() {
        inverse = !inverse;
        markForSave();
    }

    public void toggleInverseRequiresReplacement() {
        inverseRequiresReplacement = !inverseRequiresReplacement;
        markForSave();
    }

    public void setInverseReplaceTarget(Item target) {
        if (target != inverseReplaceTarget) {
            inverseReplaceTarget = target;
            markForSave();
        }
    }

    public void toggleAutoEject() {
        doEject = !doEject;
        markForSave();
    }

    public void toggleAutoPull() {
        doPull = !doPull;
        markForSave();
    }

    public void setRadiusFromPacket(int newRadius) {
        setRadius(Mth.clamp(newRadius, 0, MekanismConfig.general.minerMaxRadius.get()));
        //Send a packet to update the visual renderer
        //TODO: Only do this if the renderer is actually active
        sendUpdatePacket();
        markForSave();
    }

    private void setRadius(int newRadius) {
        if (radius != newRadius && newRadius >= 0) {
            radius = newRadius;
            if (hasLevel() && !isRemote()) {
                energyContainer.updateMinerEnergyPerTick();
                // If the radius changed, and we're on the server, go ahead and refresh the chunk set
                getChunkLoader().refreshChunkTickets();
            }
        }
    }

    public void setMinYFromPacket(int newMinY) {
        if (level != null) {
            setMinY(Mth.clamp(newMinY, level.getMinBuildHeight(), getMaxY()));
            //Send a packet to update the visual renderer
            //TODO: Only do this if the renderer is actually active
            sendUpdatePacket();
            markForSave();
        }
    }

    private void setMinY(int newMinY) {
        if (minY != newMinY) {
            minY = newMinY;
            if (hasLevel() && !isRemote()) {
                energyContainer.updateMinerEnergyPerTick();
            }
        }
    }

    public void setMaxYFromPacket(int newMaxY) {
        if (level != null) {
            setMaxY(Mth.clamp(newMaxY, getMinY(), level.getMaxBuildHeight() - 1));
            //Send a packet to update the visual renderer
            //TODO: Only do this if the renderer is actually active
            sendUpdatePacket();
            markForSave();
        }
    }

    private void setMaxY(int newMaxY) {
        if (maxY != newMaxY) {
            maxY = newMaxY;
            if (hasLevel() && !isRemote()) {
                energyContainer.updateMinerEnergyPerTick();
            }
        }
    }

    private void tryMineBlock() {
        BlockPos startingPos = getStartingPos();
        int diameter = getDiameter();
        long target = targetChunk == null ? ChunkPos.INVALID_CHUNK_POS : targetChunk.toLong();
        for (ObjectIterator<Long2ObjectMap.Entry<BitSet>> iterator = Long2ObjectMaps.fastIterator(oresToMine); iterator.hasNext(); ) {
            Long2ObjectMap.Entry<BitSet> entry = iterator.next();
            long chunk = entry.getLongKey();
            BitSet chunkToMine = entry.getValue();
            ChunkPos currentChunk = null;
            if (target == chunk) {
                //If our current chunk is the one we are already targeting, just make it reference it, so we don't need to
                // do any initialization
                currentChunk = targetChunk;
            }
            //Note: We go in reverse order instead of normal order to avoid issues where we break blocks supporting ones
            // that are affected by gravity and then are unable to break them after they have fallen. The reason we do it
            // this way instead of changing how the bits are indexed in correspondence with locations in the world is because
            // it is much more likely for there to be blocks lower down, and this allows us to avoid having to add large indices
            // to our bitset because of all the small indices having been taken up by air
            //Length returns the largest set bit + 1, so we subtract one to get the largest set bit as previousSetBit is inclusive,
            // and if none are set and this becomes -1, previousSetBit will still just return -1
            int previous = chunkToMine.length() - 1;
            while (true) {
                int index = chunkToMine.previousSetBit(previous);
                if (index == -1) {
                    //If there is no found index, remove it and continue on
                    iterator.remove();
                    break;
                } else if (currentChunk == null) {
                    //Lazy init the current chunk so that if it is empty, and we are just going to remove it
                    // we don't need to try and load it
                    updateTargetChunk(currentChunk = new ChunkPos(chunk));
                    target = chunk;
                }
                BlockPos pos = getOffsetForIndex(startingPos, diameter, index);
                BlockState state = WorldUtils.getBlockStateIfLoaded(level, pos);
                if (state != null) {
                    if (!state.isAir() && !state.is(MekanismTags.Blocks.MINER_BLACKLIST)) {
                        //Make sure the block is loaded and is not air, and is not in the blacklist of blocks the miner can break
                        // then check if the block matches one of our filters
                        MinerFilter<?> matchingFilter = null;
                        for (MinerFilter<?> filter : filterManager.getEnabledFilters()) {
                            if (filter.canFilter(state)) {
                                matchingFilter = filter;
                                break;
                            }
                        }
                        //If our hasFilter state matches our inversion state, that means we should try to mine
                        // the block, so we check if we can mine it
                        if (inverse == (matchingFilter == null) && canMine(state, pos)) {
                            //If we can, then validate we can fit the drops and try to see if we can replace it properly as well
                            List<ItemStack> drops = getDrops((ServerLevel) level, state, pos);
                            if (canInsert(drops)) {
                                CommonWorldTickHandler.fallbackItemCollector = overflowCollector;
                                if (setReplace(state, pos, matchingFilter)) {
                                    add(drops);
                                    //Try to add any drops that might have been caused by breaking the block but didn't show up in the loot table.
                                    // This mainly will be the case for some single block multiblocks and also for storage containers like chests
                                    tryAddOverflow();
                                    missingStack = ItemStack.EMPTY;
                                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                                    //Remove the block from our list of blocks to mine, and reduce the number of blocks we have to mine
                                    cachedToMine--;
                                    chunkToMine.clear(index);
                                    if (chunkToMine.isEmpty()) {
                                        // if we are out of stored elements then we remove this chunk and continue to check other chunks
                                        // remove it so that we don't have to check the chunk next time around
                                        iterator.remove();
                                        // we no longer have a chunk we are targeting, so remove it. We might get a new chunk to target
                                        // next time we try to mine but there is no reason to keep the old chunk in memory in the meantime
                                        updateTargetChunk(null);
                                    }
                                }
                                //Reset the global fallback collector to null as we are done collecting for this miner and block
                                CommonWorldTickHandler.fallbackItemCollector = null;
                            }
                            //Exit out. We either mined the block or don't have room so there is no reason to continue checking
                            return;
                        } else if (MekanismAPI.debug) {
                            Mekanism.logger.error("Filter failed or can't mine: {} @ {} {}", state, getWorldNN().dimension().location(), pos);
                        }
                    } else if (MekanismAPI.debug) {
                        Mekanism.logger.error("State was air or was blacklisted (mismatch between search and runtime): {} @ {} {}", state, getWorldNN().dimension().location(), pos);
                    }
                } else if (MekanismAPI.debug) {
                    Mekanism.logger.debug("Block was not loaded {} {}", getWorldNN().dimension().location(), pos);
                }
                //If we failed to mine the block, because it isn't loaded, is air, or we shouldn't mine it
                // remove the block from our list of blocks to mine, and reduce the number of blocks we have to mine
                cachedToMine--;
                chunkToMine.clear(index);
                if (chunkToMine.isEmpty()) {
                    // if we are out of stored elements then we remove this chunk and continue to check other chunks
                    iterator.remove();
                    break;
                }
                // if we still have elements in this chunk that can potentially be mined, decrement our index
                // to the previous one and attempt to mine it
                previous = index - 1;
            }
        }
        //If we didn't exit early due to actually mining a block that means we don't have a target chunk anymore
        updateTargetChunk(null);
    }

    /**
     * @param filter Filter that was matched, if in inverse mode this will be null
     *
     * @return false if unsuccessful
     */
    private boolean setReplace(BlockState state, BlockPos pos, @Nullable MinerFilter<?> filter) {
        if (level == null) {
            return false;
        }
        Item replaceTarget;
        ItemStack stack;
        if (filter == null) {
            stack = getReplace(replaceTarget = inverseReplaceTarget, this::inverseReplaceTargetMatches);
        } else {
            stack = getReplace(replaceTarget = filter.replaceTarget, filter::replaceTargetMatches);
        }
        if (stack.isEmpty()) {
            if (replaceTarget == Items.AIR || (filter == null && !inverseRequiresReplacement) || (filter != null && !filter.requiresReplacement)) {
                level.removeBlock(pos, false);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(null, state));
                return true;
            }
            missingStack = new ItemStack(replaceTarget);
            return false;
        }
        BlockState newState = getStateForPlacement(stack, pos);
        if (newState == null || !newState.canSurvive(level, pos)) {
            //If the spot is not a valid position for the block, then we return that we were unsuccessful
            return false;
        }
        //TODO: We may want to evaluate at some point doing this with our fake player so that it is fired as the "cause"?
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(null, state));
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(null, newState));
        return true;
    }

    private boolean canMine(BlockState state, BlockPos pos) {
        MekFakePlayer dummy = MekFakePlayer.setupFakePlayer((ServerLevel) level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
        dummy.setEmulatingUUID(getOwnerUUID());//pretend to be the owner
        boolean canMine = !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, dummy)).isCanceled();
        if (MekanismAPI.debug && !canMine) {
            Mekanism.logger.debug("Denied mining block: {} @ {} {}", state, level.dimension().location(), pos);
        }
        dummy.cleanupFakePlayer((ServerLevel) level);
        return canMine;
    }

    private BlockState getStateForPlacement(ItemStack stack, BlockPos pos) {
        MekFakePlayer dummy = MekFakePlayer.setupFakePlayer((ServerLevel) level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
        dummy.setEmulatingUUID(getOwnerUUID());//pretend to be the owner
        BlockState result = StackUtils.getStateForPlacement(stack, pos, dummy);
        dummy.cleanupFakePlayer((ServerLevel) level);
        return result;
    }

    private ItemStack getReplace(Item replaceTarget, Predicate<Item> replaceStackMatches) {
        if (replaceTarget == Items.AIR) {
            return ItemStack.EMPTY;
        }
        //Start by sourcing from the miner's inventory
        for (IInventorySlot slot : mainSlots) {
            ItemStack slotStack = slot.getStack();
            if (replaceStackMatches.test(slotStack.getItem())) {
                MekanismUtils.logMismatchedStackSize(slot.shrinkStack(1, Action.EXECUTE), 1);
                return slotStack.copyWithCount(1);
            }
        }
        //Then source from the upgrade if it is installed
        if (replaceTarget == Items.COBBLESTONE || replaceTarget == Items.STONE) {
            if (upgradeComponent.isUpgradeInstalled(Upgrade.STONE_GENERATOR)) {
                return new ItemStack(replaceTarget);
            }
        }
        //And finally source from the inventory on top if auto pull is enabled
        if (doPull) {
            if (pullInventory == null) {
                pullInventory = Capabilities.ITEM.createCache((ServerLevel) level, getBlockPos().above(2), Direction.DOWN);
            }
            IItemHandler pullInv = pullInventory.getCapability();
            if (pullInv != null) {
                TransitRequest request = TransitRequest.definedItem(pullInv, 1, Finder.item(replaceTarget));
                if (!request.isEmpty()) {
                    TransitResponse response = request.createSimpleResponse();
                    if (response.useAll().isEmpty()) {
                        //If the request isn't empty, and we were able to successfully use it all
                        return response.getStack().copyWithCount(1);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void invalidateDirectionCaches(Direction newDirection) {
        super.invalidateDirectionCaches(newDirection);
        //Note: We only need to invalidate the eject inventory on rotation as the center block stays in the same position
        ejectInventory = null;
        selfEjectInventory = null;
    }

    public boolean canInsert(List<ItemStack> toInsert) {
        if (toInsert.isEmpty()) {
            return true;
        }
        int slots = mainSlots.size();
        Int2ObjectMap<ItemCount> cachedStacks = new Int2ObjectOpenHashMap<>(slots);
        for (int i = 0; i < slots; i++) {
            IInventorySlot slot = mainSlots.get(i);
            if (!slot.isEmpty()) {
                //Note: We skip caching the current stack of any empty slots
                cachedStacks.put(i, new ItemCount(slot.getStack(), slot.getCount()));
            }
        }
        for (ItemStack stackToInsert : toInsert) {
            ItemStack stack = simulateInsert(cachedStacks, slots, stackToInsert);
            if (!stack.isEmpty()) {
                //If our stack is not empty that means we could not fit it all inside of our inventory,
                // so we return false to being able to insert all the items.
                return false;
            }
        }
        return true;
    }

    /**
     * Prioritizes "inserting" into slots that have a matching item and then tries to insert into empty slots. This allows for more accurate simulations regarding if it
     * is possible to fit everything in the inventory.
     */
    private ItemStack simulateInsert(Int2ObjectMap<ItemCount> cachedStacks, int slots, ItemStack stackToInsert) {
        if (stackToInsert.isEmpty()) {
            //If the stack is already empty for some reason just return it (aka no remainder)
            return stackToInsert;
        }
        ItemStack stack = stackToInsert.copy();
        //Try to simulate inserting into slots that are not currently empty
        for (int i = 0; i < slots; i++) {
            ItemCount cachedItem = cachedStacks.get(i);
            if (cachedItem != null && ItemStack.isSameItemSameComponents(stack, cachedItem.stack)) {
                //Ensure that our stack can stack with the item that is already in the slot
                IInventorySlot slot = mainSlots.get(i);
                int limit = slot.getLimit(stack);
                if (cachedItem.count < limit) {
                    //If we still have space left before this slot is full, try adding the stacks together
                    cachedItem.count += stack.getCount();
                    if (cachedItem.count <= limit) {
                        //If we can fit it all, return we have no remainder
                        return ItemStack.EMPTY;
                    }
                    //Otherwise, we tried to store more than can fit, update stack to represent the remainder that didn't fit
                    stack = stack.copyWithCount(cachedItem.count - limit);
                    // and update the actual amount stored to the limit of the slot
                    cachedItem.count = limit;
                }
            }
        }
        //Try to simulate inserting into slots that are currently empty
        for (int i = 0; i < slots; i++) {
            if (!cachedStacks.containsKey(i)) {
                //We have no cache of this slot, which means that it is currently empty
                IInventorySlot slot = mainSlots.get(i);
                int stackSize = stack.getCount();
                //Attempt to insert the stack into the slot, the expected outcome given our slots' restrictions is that
                // this will succeed and insert the entire stack
                stack = slot.insertItem(stack, Action.SIMULATE, AutomationType.INTERNAL);
                int remainderSize = stack.getCount();
                if (remainderSize < stackSize) {
                    //If the slot accepted at least some item we are inserting, then cache the item type that we put into that slot
                    // Given the slot is empty the expected result is that we will always end up inserting into the first empty slot
                    // and end up inserting the entire stack
                    cachedStacks.put(i, new ItemCount(stackToInsert, stackSize - remainderSize));
                    if (stack.isEmpty()) {
                        //Stack was fully accepted, return that we have no remainder
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return stack;
    }

    private void add(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            //Try inserting it first where it can stack and then into empty slots
            stack = InventoryUtils.insertItem(mainSlots, stack, Action.EXECUTE, AutomationType.INTERNAL);
            if (!stack.isEmpty()) {
                //Because of the simulated insertion the stack should never be able to be empty here,
                // but in case it is keep track of any excess as overflow
                trackOverflow(stack);
            }
        }
    }

    private boolean trackOverflow(ItemStack stack) {
        //Note: We never expect the stack to be empty but in case it is just don't handle the stack
        if (!stack.isEmpty()) {
            //Note: While we probably could get away by using a raw hashed item given we are removing the item entity for the stack
            // we don't bother in case any other mods are doing weird things with it as this is just an edge case handler so shouldn't
            // be a hotspot in regard to copying stacks
            overflow.mergeInt(HashedItem.create(stack), stack.getCount(), Integer::sum);
            //If we add something to the overflow map, mark that we have overflow
            hasOverflow = true;
            //Mark that we need to recheck if we can insert the overflow as we now have some
            recheckOverflow = true;
            markForSave();
            return true;
        }
        return false;
    }

    private void tryAddOverflow() {
        if (hasOverflow) {
            //Try to add any existing overflow to our inventory
            boolean recheck = false;
            for (ObjectIterator<Object2IntMap.Entry<HashedItem>> iter = Object2IntMaps.fastIterator(overflow); iter.hasNext(); ) {
                Object2IntMap.Entry<HashedItem> entry = iter.next();
                int amount = entry.getIntValue();
                ItemStack stack = entry.getKey().createStack(amount);
                //Note: Inserting properly handles oversized stacks, so we don't have to handle the case that amount might be greater than
                // the max stack size here as the different slots will only accept up to the item's max stack size
                stack = InventoryUtils.insertItem(mainSlots, stack, Action.EXECUTE, AutomationType.INTERNAL);
                //Note: We do not need to mark the miner for saving if something gets moved from overflow to a slot as the slot will do so
                // when it accepts the item, so we can skip marking that we need to save because overflow changed
                if (stack.isEmpty()) {
                    //We were able to fully fit the stack, so we can remove it from our list of overflow
                    iter.remove();
                    recheck = true;
                } else if (stack.getCount() != amount) {
                    //Some was able to fit, update the amount that is actually still part of the overflow
                    entry.setValue(stack.getCount());
                }
            }
            if (recheck) {
                //Update if we still have an overflow as at least one stack was able to fit
                hasOverflow = !overflow.isEmpty();
            }
        }
        //Mark it as not needing to recheck the overflow as we just tried to add it, so we fit whatever we could or didn't even have any overflow
        recheckOverflow = false;
    }

    public void start() {
        if (getLevel() == null) {
            return;
        }
        if (searcher.state == State.IDLE) {
            BlockPos startingPos = getStartingPos();
            int diameter = getDiameter();
            searcher.setChunkCache(new MinerRegionCache((ServerLevel) getLevel(), startingPos, startingPos.offset(diameter, getMaxY() - getMinY() + 1, diameter), this.upgradeComponent.isUpgradeInstalled(Upgrade.ANCHOR)));
            searcher.start();
        }
        running = true;
        markForSave();
    }

    public void stop() {
        if (searcher.state == State.SEARCHING) {
            searcher.interrupt();
            reset();
        } else if (searcher.state == State.FINISHED) {
            running = false;
            markForSave();
            //Reset the target chunk, so it isn't loaded as we might don't want to let the user just have two chunks loaded
            // eternally (or until server restart) by intentionally stopping the miner
            updateTargetChunk(null);
        }
    }

    public void reset() {
        if (searcher != null && searcher.isAlive()) {
            searcher.interrupt();
        }
        searcher = new ThreadMinerSearch(this);
        running = false;
        cachedToMine = 0;
        oresToMine = Long2ObjectMaps.emptyMap();
        missingStack = ItemStack.EMPTY;
        setActive(false);
        updateTargetChunk(null);
        markForSave();
    }

    public static boolean isSavedReplaceTarget(ItemStack stack, Item target) {
        //This method is here to make it easier to maintain parity if we change the logic of isReplaceTarget
        if (stack.getOrDefault(MekanismDataComponents.INVERSE, false)) {
            Item inverseReplaceTarget = stack.getOrDefault(MekanismDataComponents.REPLACE_STACK, Items.AIR);
            return inverseReplaceTarget != Items.AIR && inverseReplaceTarget == target;
        }
        FilterAware filterAware = stack.get(MekanismDataComponents.FILTER_AWARE);
        return filterAware != null && filterAware.anyEnabledMatch(MinerFilter.class, filter -> filter.replaceTargetMatches(target));
    }

    public boolean isReplaceTarget(Item target) {
        if (inverse) {
            //If we are in inverse mode only check our replace target, and not the filter's replace targets
            // as we don't have a matching filter once we are breaking blocks so there wouldn't actually
            // be any cases where it makes sense to skip them due to them being the result of one of the
            // things we are mining
            return inverseReplaceTargetMatches(target);
        }
        return filterManager.anyEnabledMatch(target, MinerFilter::replaceTargetMatches);
    }

    /**
     * @apiNote Assumes that inverse is checked before this is called
     */
    private boolean inverseReplaceTargetMatches(Item target) {
        return inverseReplaceTarget != Items.AIR && inverseReplaceTarget == target;
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        running = nbt.getBoolean(SerializationConstants.RUNNING);
        delay = nbt.getInt(SerializationConstants.DELAY);
        numPowering = nbt.getInt(SerializationConstants.NUM_POWERING);
        NBTUtils.setEnumIfPresent(nbt, SerializationConstants.STATE, State.BY_ID, s -> {
            if (!initCalc && s == State.SEARCHING) {
                //If we loaded and haven't started yet, but we were searching when we saved
                // pretend we had finished searching so that we will start again on the first tick
                s = State.FINISHED;
            }
            searcher.state = s;
        });
        //Update energy per tick in case any of the values changed. It would be slightly cleaner to also validate the fact
        // the values changed, but it would make the code a decent bit messier, as we couldn't use NBTUtils, and it is a
        // rather quick check to update the energy per tick, and in most cases at least one of the settings will not be at
        // the default value
        energyContainer.updateMinerEnergyPerTick();
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(SerializationConstants.NUM_POWERING);
        tag.remove(SerializationConstants.STATE);
    }

    @Override
    public void setLevel(@NotNull Level world) {
        super.setLevel(world);
        //Update miner energy as the world height is likely different compared to the old pre 1.18 values
        energyContainer.updateMinerEnergyPerTick();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putBoolean(SerializationConstants.RUNNING, running);
        nbtTags.putInt(SerializationConstants.DELAY, delay);
        nbtTags.putInt(SerializationConstants.NUM_POWERING, numPowering);
        NBTUtils.writeEnum(nbtTags, SerializationConstants.STATE, searcher.state);
        if (!overflow.isEmpty()) {
            //Persist any items that are stored as overflow
            DataResult<Tag> encoded = OverflowAware.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), new OverflowAware(overflow));
            if (encoded.isSuccess()) {
                nbtTags.put(SerializationConstants.OVERFLOW, encoded.getOrThrow());
            } else {
                encoded.ifError(error -> Mekanism.logger.warn("Failed to encode overflowed items: {}", error.message()));
            }
        }
    }

    public int getTotalSize() {
        int diameter = getDiameter();
        return diameter * diameter * (getMaxY() - getMinY() + 1);
    }

    public int getDiameter() {
        return (radius * 2) + 1;
    }

    public BlockPos getStartingPos() {
        return new BlockPos(getBlockPos().getX() - radius, getMinY(), getBlockPos().getZ() - radius);
    }

    public static BlockPos getOffsetForIndex(BlockPos start, int diameter, int index) {
        return start.offset(index % diameter, index / diameter / diameter, (index / diameter) % diameter);
    }

    @Override
    public boolean isPowered() {
        return redstone || numPowering > 0;
    }

    @Override
    public boolean isClientRendering() {
        return clientRendering;
    }

    @Override
    public void toggleClientRendering() {
        this.clientRendering = !clientRendering;
    }

    @Override
    public boolean canDisplayVisuals() {
        return getRadius() <= 64;
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
    public int getBoundingComparatorSignal(Vec3i offset) {
        //Return the comparator signal if it is one of the horizontal ports
        Direction facing = getDirection();
        Direction back = facing.getOpposite();
        if (offset.equals(new Vec3i(back.getStepX(), 1, back.getStepZ()))) {
            return getCurrentRedstoneLevel();
        }
        Direction left = MekanismUtils.getLeft(facing);
        if (offset.equals(new Vec3i(left.getStepX(), 0, left.getStepZ()))) {
            return getCurrentRedstoneLevel();
        }
        Direction right = left.getOpposite();
        if (offset.equals(new Vec3i(right.getStepX(), 0, right.getStepZ()))) {
            return getCurrentRedstoneLevel();
        }
        return Redstone.SIGNAL_NONE;
    }

    @Override
    protected void notifyComparatorChange() {
        super.notifyComparatorChange();
        Direction facing = getDirection();
        Direction left = MekanismUtils.getLeft(facing);
        BlockBounding boundingBlock = MekanismBlocks.BOUNDING_BLOCK.value();
        //Proxy the comparator updates to the various ports we expose comparators to
        level.updateNeighbourForOutputSignal(worldPosition.relative(left), boundingBlock);
        level.updateNeighbourForOutputSignal(worldPosition.relative(left.getOpposite()), boundingBlock);
        level.updateNeighbourForOutputSignal(worldPosition.relative(facing.getOpposite()).above(), boundingBlock);
    }

    @Override
    public void configurationDataSet() {
        super.configurationDataSet();
        if (isRunning()) {
            //If it was running when we updated the configuration data, stop it, reset it, and start it again
            // to ensure that there are no desyncs in energy cost due to things like the radius changing but
            // having the blocks to mine be calculated based on the old radius
            stop();
            reset();
            start();
        }
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.writeSustainedData(provider, dataMap);
        dataMap.putInt(SerializationConstants.RADIUS, getRadius());
        dataMap.putInt(SerializationConstants.MIN, getMinY());
        dataMap.putInt(SerializationConstants.MAX, getMaxY());
        dataMap.putBoolean(SerializationConstants.EJECT, doEject);
        dataMap.putBoolean(SerializationConstants.PULL, doPull);
        dataMap.putBoolean(SerializationConstants.SILK_TOUCH, getSilkTouch());
        dataMap.putBoolean(SerializationConstants.INVERSE, inverse);
        if (inverseReplaceTarget != Items.AIR) {
            NBTUtils.writeRegistryEntry(dataMap, SerializationConstants.REPLACE_TARGET, BuiltInRegistries.ITEM, inverseReplaceTarget);
        }
        dataMap.putBoolean(SerializationConstants.INVERSE_REQUIRES_REPLACE, inverseRequiresReplacement);
        filterManager.writeToNBT(provider, dataMap);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag dataMap) {
        super.readSustainedData(provider, dataMap);
        setRadius(Math.min(dataMap.getInt(SerializationConstants.RADIUS), MekanismConfig.general.minerMaxRadius.get()));
        NBTUtils.setIntIfPresent(dataMap, SerializationConstants.MIN, newMinY -> {
            if (hasLevel() && !isRemote()) {
                setMinY(Math.max(newMinY, level.getMinBuildHeight()));
            } else {
                setMinY(newMinY);
            }
        });
        NBTUtils.setIntIfPresent(dataMap, SerializationConstants.MAX, newMaxY -> {
            if (hasLevel() && !isRemote()) {
                setMaxY(Math.min(newMaxY, level.getMaxBuildHeight() - 1));
            } else {
                setMaxY(newMaxY);
            }
        });
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.EJECT, eject -> doEject = eject);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.PULL, pull -> doPull = pull);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.SILK_TOUCH, this::setSilkTouch);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.INVERSE, inverse -> this.inverse = inverse);
        inverseReplaceTarget = NBTUtils.readRegistryEntry(dataMap, SerializationConstants.REPLACE_TARGET, BuiltInRegistries.ITEM, Items.AIR);
        NBTUtils.setBooleanIfPresent(dataMap, SerializationConstants.INVERSE_REQUIRES_REPLACE, requiresReplace -> inverseRequiresReplacement = requiresReplace);
        filterManager.readFromNBT(provider, dataMap);
        //Note: We read the overflow information if it is present in sustained data in order to grab the information from the digital miner item
        // when it is placed or when the BE is loaded from NBT, but the corresponding writing of the data is done in the saveAdditional method
        // as opposed to the writeSustainedData method to ensure that configuration cards do not copy overflow data from one miner to another
        NBTUtils.setListIfPresent(dataMap, SerializationConstants.OVERFLOW, Tag.TAG_COMPOUND, overflowTag -> {
            //Clear any existing overflow and read what is the actual overflow from NBT
            overflow.clear();
            DataResult<Object2IntMap<HashedItem>> decoded = OverflowAware.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), overflowTag)
                  .map(OverflowAware::overflow);
            decoded.ifSuccess(overflow::putAll);
            decoded.ifError(error -> Mekanism.logger.warn("Failed to decode overflowed items: {}", error.message()));
            hasOverflow = !overflow.isEmpty();
            //Note: Marking rechecking if any of the overflow can fit probably isn't strictly necessary here as in theory it already tried
            // to insert anything before when it was saving, but it doesn't really hurt and then if the last tick had it get overflow or
            // had the inventory change which caused a save, but the next tick never happened the overflow may actually need to be updated
            recheckOverflow = hasOverflow;
        });
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.RADIUS, getRadius());
        builder.set(MekanismDataComponents.MIN_Y, getMinY());
        builder.set(MekanismDataComponents.MAX_Y, getMaxY());
        builder.set(MekanismDataComponents.EJECT, doEject);
        builder.set(MekanismDataComponents.PULL, doPull);
        builder.set(MekanismDataComponents.SILK_TOUCH, getSilkTouch());
        builder.set(MekanismDataComponents.INVERSE, inverse);
        builder.set(MekanismDataComponents.REPLACE_STACK, inverseReplaceTarget);
        builder.set(MekanismDataComponents.INVERSE_REQUIRES_REPLACE, inverseRequiresReplacement);
        builder.set(MekanismDataComponents.OVERFLOW_AWARE, new OverflowAware(new Object2IntLinkedOpenHashMap<>(overflow)));
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        setRadius(Math.min(input.getOrDefault(MekanismDataComponents.RADIUS, radius), MekanismConfig.general.minerMaxRadius.get()));
        int newMinY = input.getOrDefault(MekanismDataComponents.MIN_Y, minY);
        int newMaxY = input.getOrDefault(MekanismDataComponents.MAX_Y, minY);
        if (level != null && !isRemote()) {
            setMinY(Math.max(newMinY, level.getMinBuildHeight()));
            setMaxY(Math.min(newMaxY, level.getMaxBuildHeight() - 1));
        } else {
            setMinY(newMinY);
            setMaxY(newMaxY);
        }
        doEject = input.getOrDefault(MekanismDataComponents.EJECT, doEject);
        doPull = input.getOrDefault(MekanismDataComponents.PULL, doPull);
        setSilkTouch(input.getOrDefault(MekanismDataComponents.SILK_TOUCH, silkTouch));
        inverse = input.getOrDefault(MekanismDataComponents.INVERSE, inverse);
        inverseReplaceTarget = input.getOrDefault(MekanismDataComponents.REPLACE_STACK, inverseReplaceTarget);
        inverseRequiresReplacement = input.getOrDefault(MekanismDataComponents.INVERSE_REQUIRES_REPLACE, inverseRequiresReplacement);
        //Clear any existing overflow and read what is the actual overflow from the stack
        overflow.clear();
        overflow.putAll(input.getOrDefault(MekanismDataComponents.OVERFLOW_AWARE, OverflowAware.EMPTY).overflow());
        hasOverflow = !overflow.isEmpty();
        //Note: Marking rechecking if any of the overflow can fit probably isn't strictly necessary here as in theory it already tried
        // to insert anything before when it was saving, but it doesn't really hurt and then if the last tick had it get overflow or
        // had the inventory change which caused a save, but the next tick never happened the overflow may actually need to be updated
        recheckOverflow = hasOverflow;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            delayLength = MekanismUtils.getTicks(this, MekanismConfig.general.minerTicksPerMine.get());
        }
    }

    @NotNull
    @Override
    public List<Component> getInfo(@NotNull Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Nullable
    @Override
    public <T> T getOffsetCapabilityIfEnabled(@NotNull BlockCapability<T, @Nullable Direction> capability, Direction side, @NotNull Vec3i offset) {
        if (capability == Capabilities.ITEM.block()) {
            //Get item handler cap directly from here as we disable it entirely for the main block as we only have it enabled from ports
            return Objects.requireNonNull(itemHandlerManager, "Expected to have item handler").resolve(capability, side);
        }
        //Otherwise, we can just grab the capability from the tile normally
        return WorldUtils.getCapability(level, capability, worldPosition, null, this, side);
    }

    @Override
    public boolean isOffsetCapabilityDisabled(@NotNull BlockCapability<?, @Nullable Direction> capability, Direction side, @NotNull Vec3i offset) {
        if (capability == Capabilities.ITEM.block()) {
            return notItemPort(side, offset);
        } else if (EnergyCompatUtils.isEnergyCapability(capability)) {
            return notEnergyPort(side, offset);
        }
        //If we are not an item handler or energy capability, and it is a capability that we can support,
        // but it is one that normally should be disabled for offset capabilities, then expose it but only do so
        // via our ports for things like computer integration capabilities, then we treat the capability as
        // disabled if it is not against one of our ports
        return notItemPort(side, offset) && notEnergyPort(side, offset);
    }

    private boolean notItemPort(Direction side, Vec3i offset) {
        if (offset.equals(new Vec3i(0, 1, 0))) {
            //If input then disable if wrong face of input
            return side != Direction.UP;
        }
        Direction back = getOppositeDirection();
        if (offset.equals(new Vec3i(back.getStepX(), 1, back.getStepZ()))) {
            //If output then disable if wrong face of output
            return side != back;
        }
        return true;
    }

    private boolean notEnergyPort(Direction side, Vec3i offset) {
        if (offset.equals(Vec3i.ZERO)) {
            //Disable if it is the bottom port but wrong side of it
            return side != Direction.DOWN;
        }
        Direction left = getLeftSide();
        if (offset.equals(new Vec3i(left.getStepX(), 0, left.getStepZ()))) {
            //Disable if left power port but wrong side of the port
            return side != left;
        }
        Direction right = left.getOpposite();
        if (offset.equals(new Vec3i(right.getStepX(), 0, right.getStepZ()))) {
            //Disable if right power port but wrong side of the port
            return side != right;
        }
        return true;
    }

    @Override
    public TileComponentChunkLoader<TileEntityDigitalMiner> getChunkLoader() {
        return chunkLoaderComponent;
    }

    /**
     * @apiNote Should only be called on the server, but probably won't cause major issues if called on the client
     */
    private void updateTargetChunk(@Nullable ChunkPos target) {
        if (!Objects.equals(targetChunk, target)) {
            //Only update the target if it has changed
            targetChunk = target;
            getChunkLoader().refreshChunkTickets();
        }
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        ChunkPos minerChunk = new ChunkPos(worldPosition);
        if (targetChunk != null) {
            //If we have a target check to make sure it is in the radius (most likely it is)
            if (SectionPos.blockToSectionCoord(worldPosition.getX() - radius) <= targetChunk.x &&
                targetChunk.x <= SectionPos.blockToSectionCoord(worldPosition.getX() + radius) &&
                SectionPos.blockToSectionCoord(worldPosition.getZ() - radius) <= targetChunk.z &&
                targetChunk.z <= SectionPos.blockToSectionCoord(worldPosition.getZ() + radius)) {
                // if it is, return the chunks we should be loading, provide the chunk the miner is in
                // and the chunk that the miner is currently mining
                //TODO: At some point we may want to change the ticket of the chunk the miner is mining to be
                // at a lower level and not cause tiles in it to actually tick
                if (minerChunk.equals(targetChunk)) {
                    return Set.of(minerChunk);
                }
                return Set.of(minerChunk, targetChunk);
            }
        }
        //Otherwise, just return the miner's chunk
        return Collections.singleton(minerChunk);
    }

    @Override
    public SortableFilterManager<MinerFilter<?>> getFilterManager() {
        return filterManager;
    }

    public MinerEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @ComputerMethod(methodDescription = "Get the count of block found but not yet mined")
    public int getToMine() {
        return !isRemote() && searcher.state == State.SEARCHING ? searcher.found : cachedToMine;
    }

    @ComputerMethod(methodDescription = "Whether the miner is currently running")
    public boolean isRunning() {
        return running;
    }

    @ComputerMethod(nameOverride = "getAutoEject", methodDescription = "Whether Auto Eject is turned on")
    public boolean getDoEject() {
        return doEject;
    }

    @ComputerMethod(nameOverride = "getAutoPull", methodDescription = "Whether Auto Pull is turned on")
    public boolean getDoPull() {
        return doPull;
    }

    public boolean hasOverflow() {
        return hasOverflow;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        addConfigContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getDoEject, value -> doEject = value));
        container.track(SyncableBoolean.create(this::getDoPull, value -> doPull = value));
        container.track(SyncableBoolean.create(this::isRunning, value -> running = value));
        container.track(SyncableBoolean.create(this::getSilkTouch, this::setSilkTouch));
        container.track(SyncableEnum.create(State.BY_ID, State.IDLE, () -> searcher.state, value -> searcher.state = value));
        container.track(SyncableInt.create(this::getToMine, value -> cachedToMine = value));
        container.track(SyncableItemStack.create(() -> missingStack, value -> missingStack = value));
        container.track(SyncableBoolean.create(this::hasOverflow, value -> hasOverflow = value));
    }

    public void addConfigContainerTrackers(MekanismContainer container) {
        container.track(SyncableInt.create(this::getRadius, this::setRadius));
        container.track(SyncableInt.create(this::getMinY, this::setMinY));
        container.track(SyncableInt.create(this::getMaxY, this::setMaxY));
        container.track(SyncableBoolean.create(this::getInverse, value -> inverse = value));
        container.track(SyncableBoolean.create(this::getInverseRequiresReplacement, value -> inverseRequiresReplacement = value));
        container.track(SyncableRegistryEntry.create(BuiltInRegistries.ITEM, this::getInverseReplaceTarget, value -> inverseReplaceTarget = value));
        filterManager.addContainerTrackers(container);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putInt(SerializationConstants.RADIUS, getRadius());
        updateTag.putInt(SerializationConstants.MIN, getMinY());
        updateTag.putInt(SerializationConstants.MAX, getMaxY());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setIntIfPresent(tag, SerializationConstants.RADIUS, this::setRadius);//the client is allowed to use whatever server sends
        NBTUtils.setIntIfPresent(tag, SerializationConstants.MIN, this::setMinY);
        NBTUtils.setIntIfPresent(tag, SerializationConstants.MAX, this::setMaxY);
    }

    private List<ItemStack> getDrops(ServerLevel level, BlockState state, BlockPos pos) {
        if (state.isAir()) {
            return Collections.emptyList();
        }
        ItemStack stack = ItemAtomicDisassembler.fullyChargedStack();
        if (getSilkTouch()) {
            Optional<Reference<Enchantment>> silkTouch = level.holder(Enchantments.SILK_TOUCH);
            //noinspection OptionalIsPresent - Capturing lambda
            if (silkTouch.isPresent()) {
                stack.enchant(silkTouch.get(), 1);
            }
        }
        MekFakePlayer dummy = MekFakePlayer.setupFakePlayer(level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
        dummy.setEmulatingUUID(getOwnerUUID());//pretend to be the owner
        List<ItemStack> drops = WorldUtils.getDrops(state, level, pos, WorldUtils.getTileEntity(level, pos), dummy, stack);
        dummy.cleanupFakePlayer(level);
        return drops;
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    long getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : 0L;
    }

    @ComputerMethod(methodDescription = "Get the size of the Miner's internal inventory")
    int getSlotCount() {
        return mainSlots.size();
    }

    @ComputerMethod(methodDescription = "Get the contents of the internal inventory slot. 0 based.")
    ItemStack getItemInSlot(int slot) throws ComputerException {
        int slots = getSlotCount();
        if (slot < 0 || slot >= slots) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this digital miner only has '%d' slots (zero indexed).", slot, slots);
        }
        return mainSlots.get(slot).getStack();
    }

    @ComputerMethod(methodDescription = "Get the state of the Miner's search")
    State getState() {
        return searcher.state;
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Update the Auto Eject setting")
    void setAutoEject(boolean eject) throws ComputerException {
        validateSecurityIsPublic();
        if (doEject != eject) {
            toggleAutoEject();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Update the Auto Pull setting")
    void setAutoPull(boolean pull) throws ComputerException {
        validateSecurityIsPublic();
        if (doPull != pull) {
            toggleAutoPull();
        }
    }

    @ComputerMethod(nameOverride = "setSilkTouch", requiresPublicSecurity = true, methodDescription = "Update the Silk Touch setting")
    void computerSetSilkTouch(boolean silk) throws ComputerException {
        validateSecurityIsPublic();
        setSilkTouch(silk);
    }

    @ComputerMethod(nameOverride = "start", requiresPublicSecurity = true, methodDescription = "Attempt to start the mining process")
    void computerStart() throws ComputerException {
        validateSecurityIsPublic();
        start();
    }

    @ComputerMethod(nameOverride = "stop", requiresPublicSecurity = true, methodDescription = "Attempt to stop the mining process")
    void computerStop() throws ComputerException {
        validateSecurityIsPublic();
        stop();
    }

    @ComputerMethod(nameOverride = "reset", requiresPublicSecurity = true, methodDescription = "Stop the mining process and reset the Miner to be able to change settings")
    void computerReset() throws ComputerException {
        validateSecurityIsPublic();
        reset();
    }

    @ComputerMethod(methodDescription = "Get the maximum allowable Radius value, determined from the mod's config")
    int getMaxRadius() {
        return MekanismConfig.general.minerMaxRadius.get();
    }

    private void validateCanChangeConfiguration() throws ComputerException {
        validateSecurityIsPublic();
        //Validate the miner is stopped and reset first
        if (searcher.state != State.IDLE) {
            throw new ComputerException("Miner must be stopped and reset before its targeting configuration is changed.");
        }
    }

    @ComputerMethod(nameOverride = "setRadius", requiresPublicSecurity = true, methodDescription = "Update the mining radius (blocks). Requires miner to be stopped/reset first")
    void computerSetRadius(int radius) throws ComputerException {
        validateCanChangeConfiguration();
        if (radius < 0 || radius > MekanismConfig.general.minerMaxRadius.get()) {
            //Validate dimensions even though we can clamp
            throw new ComputerException("Radius '%d' is out of range must be between 0 and %d. (Inclusive)", radius, MekanismConfig.general.minerMaxRadius.get());
        }
        setRadiusFromPacket(radius);
    }

    @ComputerMethod(nameOverride = "setMinY", requiresPublicSecurity = true, methodDescription = "Update the minimum Y level for mining. Requires miner to be stopped/reset first")
    void computerSetMinY(int minY) throws ComputerException {
        validateCanChangeConfiguration();
        if (level != null) {
            int min = level.getMinBuildHeight();
            if (minY < min || minY > getMaxY()) {
                //Validate dimensions even though we can clamp
                throw new ComputerException("Min Y '%d' is out of range must be between %d and %d. (Inclusive)", minY, min, getMaxY());
            }
            setMinYFromPacket(minY);
        }
    }

    @ComputerMethod(nameOverride = "setMaxY", requiresPublicSecurity = true, methodDescription = "Update the maximum Y level for mining. Requires miner to be stopped/reset first")
    void computerSetMaxY(int maxY) throws ComputerException {
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

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Update the Inverse Mode setting. Requires miner to be stopped/reset first")
    void setInverseMode(boolean enabled) throws ComputerException {
        validateCanChangeConfiguration();
        if (inverse != enabled) {
            toggleInverse();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Update the Inverse Mode Requires Replacement setting. Requires miner to be stopped/reset first")
    void setInverseModeRequiresReplacement(boolean requiresReplacement) throws ComputerException {
        validateCanChangeConfiguration();
        if (inverseRequiresReplacement != requiresReplacement) {
            toggleInverseRequiresReplacement();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Update the target for Replacement in Inverse Mode. Requires miner to be stopped/reset first")
    void setInverseModeReplaceTarget(Item target) throws ComputerException {
        validateCanChangeConfiguration();
        setInverseReplaceTarget(target);
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Remove the target for Replacement in Inverse Mode. Requires miner to be stopped/reset first")
    void clearInverseModeReplaceTarget() throws ComputerException {
        setInverseModeReplaceTarget(Items.AIR);
    }

    @ComputerMethod(methodDescription = "Get the current list of Miner Filters")
    Collection<MinerFilter<?>> getFilters() {
        return filterManager.getFilters();
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Add a new filter to the miner. Requires miner to be stopped/reset first")
    boolean addFilter(MinerFilter<?> filter) throws ComputerException {
        validateCanChangeConfiguration();
        return filterManager.addFilter(filter);
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Removes the exactly matching filter from the miner. Requires miner to be stopped/reset first")
    boolean removeFilter(MinerFilter<?> filter) throws ComputerException {
        validateCanChangeConfiguration();
        return filterManager.removeFilter(filter);
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
