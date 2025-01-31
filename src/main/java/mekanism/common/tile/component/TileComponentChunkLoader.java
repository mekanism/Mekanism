package mekanism.common.tile.component;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.common.world.chunk.TicketSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TileComponentChunkLoader<T extends TileEntityMekanism & IChunkLoader> implements ITileComponent {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final TicketController TICKET_CONTROLLER = new TicketController(Mekanism.rl("chunk_loader"), ChunkValidationCallback.INSTANCE);

    /**
     * TileEntity implementing this component.
     */
    private final T tile;
    private final LongSet chunkSet = new LongOpenHashSet();
    private final boolean forceTicks;
    @Nullable
    private ServerLevel prevWorld;
    @Nullable
    private BlockPos prevPos;
    private boolean hasRegistered;

    public TileComponentChunkLoader(T tile) {
        this(tile, false);
    }

    public TileComponentChunkLoader(T tile, boolean forceTicks) {
        this.tile = tile;
        this.tile.addComponent(this);
        this.forceTicks = forceTicks;
    }

    public boolean canOperate() {
        return MekanismConfig.general.allowChunkloading.get() && tile.supportsUpgrades() && tile.getComponent().isUpgradeInstalled(Upgrade.ANCHOR);
    }

    private void releaseChunkTickets(@NotNull ServerLevel world, @NotNull BlockPos pos) {
        int tickets = chunkSet.size();
        LOGGER.debug("Attempting to remove {} chunk tickets. Pos: {} World: {}", tickets, pos, world.dimension().location());
        if (tickets > 0) {
            for (long chunkPos : chunkSet) {
                boolean success = TICKET_CONTROLLER.forceChunk(world, pos, ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), false, forceTicks);
                if (!success) {
                    LOGGER.warn("Failed to release chunk ticket for {}", chunkPos);
                }
            }
            chunkSet.clear();
            markDirty();
        }
        hasRegistered = false;
        prevWorld = null;
    }

    private void registerChunkTickets(@NotNull ServerLevel world) {
        prevPos = tile.getBlockPos();
        prevWorld = world;
        Set<ChunkPos> chunks = tile.getChunkSet();
        int tickets = chunks.size();
        LOGGER.debug("Attempting to add {} chunk tickets. Pos: {} World: {}", tickets, prevPos, world.dimension().location());
        if (tickets > 0) {
            for (ChunkPos chunkPos : chunks) {
                boolean success = TICKET_CONTROLLER.forceChunk(world, prevPos, chunkPos.x, chunkPos.z, true, forceTicks);
                chunkSet.add(chunkPos.toLong());
                if (!success) {
                    LOGGER.error("Failed to force chunk during registration {}", chunkPos);
                }
            }
            markDirty();
        }
        hasRegistered = true;
    }

    /**
     * Release and re-register tickets, call when chunk set changes
     */
    public void refreshChunkTickets() {
        if (!tile.isRemote()) {
            refreshChunkTickets((ServerLevel) Objects.requireNonNull(tile.getLevel()), tile.getBlockPos(), true);
        }
    }

    /**
     * @param ticketsChanged {@code true} if the chunk set of our tile changed, and we need to force adjusting our registered tickets.
     *
     * @apiNote Only call server side
     */
    private void refreshChunkTickets(@NotNull ServerLevel world, @NotNull BlockPos pos, boolean ticketsChanged) {
        boolean canOperate = canOperate();
        if (MekanismAPI.debug) {
            LOGGER.debug("refreshChunkTickets called for {}. Can operate = {}", pos, canOperate);
        }
        if (hasRegistered && prevWorld != null && prevPos != null) {
            //Note: If we have already registered the previous world and previous position
            // should never be null, but we validate this just in case
            if (prevWorld != world || !pos.equals(prevPos)) {
                //If the location changed clear all old tickets
                releaseChunkTickets(prevWorld, prevPos);
                if (canOperate) {
                    //And if we can operate, add any new ones back
                    registerChunkTickets(world);
                }
            } else if (!canOperate) {
                //If we can't operate any more clear all old tickets
                releaseChunkTickets(world, pos);
            } else if (ticketsChanged) {
                //We can operate, the location didn't change. If the tickets changed, remove ones that are no longer valid
                // and add in any that are now valid but were not before. This lets us not have to remove and then add back
                // any tickets that are still valid as it is unnecessary.
                if (chunkSet.isEmpty()) {
                    //If we registered but don't actually have any loaded, which is somewhat unlikely, but worth checking;
                    // just register our tickets normally
                    registerChunkTickets(world);
                } else {
                    //Otherwise, we need to do some more checks
                    LongSet newChunks = getTileChunks();
                    if (newChunks.isEmpty()) {
                        //Probably never the case, but if we have no chunks that should be loaded anymore;
                        // just release them all normally
                        releaseChunkTickets(world, pos);
                    } else {
                        //Otherwise, we need to do calculate the differences to properly adjust which chunks are loaded and which ones are not
                        int removed = 0;
                        int added = 0;
                        //Remove any chunk tickets that are not valid anymore
                        LongIterator chunkIt = chunkSet.iterator();
                        while (chunkIt.hasNext()) {
                            long chunkPos = chunkIt.nextLong();
                            if (!newChunks.contains(chunkPos)) {
                                //If the chunk is no longer in our chunks we want loaded
                                // then we need to unforce the chunk and remove it
                                boolean success = TICKET_CONTROLLER.forceChunk(world, pos, ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), false, forceTicks);
                                if (!success) {
                                    LOGGER.warn("Failed to remove forced chunk {}", chunkPos);
                                }
                                chunkIt.remove();
                                removed++;
                            }
                        }
                        //And add any that are valid now that weren't before
                        for (long chunkPos : newChunks) {
                            if (chunkSet.add(chunkPos)) {
                                //If we didn't already have it in our chunk set and added actually added it as it is new
                                // then we also need to force the chunk
                                boolean success = TICKET_CONTROLLER.forceChunk(world, pos, ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), true, forceTicks);
                                if (!success) {
                                    LOGGER.error("Failed to force chunk during refresh {}", chunkPos);
                                }
                                added++;
                            }
                        }
                        if (removed != 0 || added != 0) {
                            markDirty();
                        }
                        LOGGER.debug("refreshChunkTickets(): Removed {} no longer valid chunk tickets, and added {} newly valid chunk tickets. Pos: {} World: {}", removed, added, pos,
                              world.dimension().location());
                        if (MekanismAPI.debug) {
                            LOGGER.debug("Current set: {}", chunkSet);
                            LOGGER.debug("Tile chunk: {}", ChunkPos.asLong(tile.getBlockPos()));
                            LOGGER.debug("Tile requested: {}", newChunks);
                        }
                    }
                }
            }
        } else if (canOperate) {
            //We haven't registered yet, but we can operate - add all of our tickets
            registerChunkTickets(world);
        }
    }

    public void tickServer() {
        Level world = tile.getLevel();
        if (world != null) {
            //Update tickets if the position changed, or we are no longer able to operate
            refreshChunkTickets((ServerLevel) world, tile.getBlockPos(), false);
        }
    }

    @Override
    public String getComponentKey() {
        //Unused
        return "componentChunkLoader";
    }

    @Override
    public void deserialize(CompoundTag componentTag, HolderLookup.Provider provider) {
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void read(CompoundTag nbtTags, HolderLookup.Provider provider) {
        if (!chunkSet.isEmpty()) {
            //If we currently have any chunks loaded, remove their tickets and clear them
            if (tile.hasLevel() && !tile.isRemote() && hasRegistered && prevWorld != null && prevPos != null) {
                //If we had any chunks registered remove them. When hasRegistered is true
                // prevWorld and prevPos should both be nonnull, but validate them just in case
                releaseChunkTickets(prevWorld, prevPos);
            } else {
                //Otherwise, just clear the set if it somehow has elements but isn't marked as registered
                chunkSet.clear();
            }
        }
        for (long chunk : nbtTags.getLongArray(SerializationConstants.CHUNK_SET)) {
            chunkSet.add(chunk);
        }
    }

    @Override
    public void write(CompoundTag nbtTags, HolderLookup.Provider provider) {
        if (!chunkSet.isEmpty()) {
            nbtTags.putLongArray(SerializationConstants.CHUNK_SET, chunkSet.toLongArray());
        }
    }

    @Override
    public void removed() {
        if (!tile.isRemote() && hasRegistered && prevWorld != null && prevPos != null) {
            //If we have any chunks registered remove them. When hasRegistered is true
            // prevWorld and prevPos should both be nonnull, but validate them just in case
            releaseChunkTickets(prevWorld, prevPos);
        }
    }

    private void markDirty() {
        //Marks the chunk as dirty so it can properly save
        tile.markForSave();
    }

    private LongSet getTileChunks() {
        Set<ChunkPos> chunks = tile.getChunkSet();
        if (chunks.isEmpty()) {
            return LongSets.EMPTY_SET;
        }
        LongSet chunksAsLongs = new LongOpenHashSet(chunks.size());
        for (ChunkPos chunkPos : chunks) {
            chunksAsLongs.add(chunkPos.toLong());
        }
        return chunksAsLongs;
    }

    public static class ChunkValidationCallback implements LoadingValidationCallback {

        public static final ChunkValidationCallback INSTANCE = new ChunkValidationCallback();

        private ChunkValidationCallback() {
        }

        @Override
        public void validateTickets(@NotNull ServerLevel world, @NotNull TicketHelper ticketHelper) {
            ResourceLocation worldName = world.dimension().location();
            LOGGER.debug("Validating tickets for: {}. Blocks: {}, Entities: {}", worldName, ticketHelper.getBlockTickets().size(),
                  ticketHelper.getEntityTickets().size());
            for (Map.Entry<BlockPos, TicketSet> entry : ticketHelper.getBlockTickets().entrySet()) {
                //Only bother looking at non ticking chunks as we don't register any "fully" ticking chunks
                BlockPos pos = entry.getKey();
                LongSet forcedChunks = entry.getValue().nonTicking();
                LongSet tickingForcedChunks = entry.getValue().ticking();
                LOGGER.debug("Validating tickets for: {}, BlockPos: {}, Forced chunks: {}, Ticking forced chunks: {}", worldName, pos, forcedChunks.size(),
                      tickingForcedChunks.size());
                validateTickets(world, worldName, pos, ticketHelper, forcedChunks, false);
                validateTickets(world, worldName, pos, ticketHelper, tickingForcedChunks, true);
            }
        }

        private void validateTickets(ServerLevel world, ResourceLocation worldName, BlockPos pos, TicketHelper ticketHelper, LongSet forcedChunks, boolean ticking) {
            int ticketCount = forcedChunks.size();
            if (ticketCount > 0) {
                //We expect this always be the case but just in case it is empty don't bother looking up the tile
                //Note: This does not use WorldUtils#getTileEntity as we want to force the chunk to load if it isn't loaded yet
                // so that we can properly validate it
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof IChunkLoader) {
                    TileComponentChunkLoader<?> chunkLoader = ((IChunkLoader) tile).getChunkLoader();
                    if (chunkLoader.canOperate()) {
                        if (!forcedChunks.equals(chunkLoader.chunkSet)) {
                            //If there is a mismatch between the chunkSet and actual chunks
                            // update the chunk set to trust what chunks the loader actually has registered
                            LOGGER.debug("Mismatched chunkSet for chunk loader at position: {} in {}. Correcting.", pos, worldName);
                            chunkLoader.chunkSet.clear();
                            chunkLoader.chunkSet.addAll(forcedChunks);
                            chunkLoader.markDirty();
                        }
                        //Next we validate that all the chunks are still properly contained and the chunks we want to load
                        // didn't change (such as from the max radius of the digital miner becoming lower)
                        LongSet chunks = chunkLoader.getTileChunks();
                        if (chunks.isEmpty()) {
                            //Probably never the case, but if we have no chunks that should be loaded anymore;
                            // just release them all
                            LOGGER.warn("Removing {} chunk tickets as they are no longer valid as this loader does not expect to have any tickets even "
                                        + "though it is can operate. Pos: {} World: {}", ticketCount, pos, worldName);
                            releaseAllTickets(chunkLoader, pos, ticketHelper);
                        } else {
                            //Calculate the differences to properly adjust which chunks are loaded and which ones are not
                            int removed = 0;
                            int added = 0;
                            //Remove any chunk tickets that are not valid anymore
                            LongIterator chunkIt = chunkLoader.chunkSet.iterator();
                            while (chunkIt.hasNext()) {
                                long chunkPos = chunkIt.nextLong();
                                if (!chunks.contains(chunkPos) || ticking != chunkLoader.forceTicks) {
                                    //If the chunk is no longer in our chunks we want loaded or restarting changed how it should tick,
                                    // then we mark it for removal
                                    ticketHelper.removeTicket(pos, chunkPos, ticking);
                                    // and remove it from the set we are keeping track of
                                    chunkIt.remove();
                                    removed++;
                                }
                            }
                            //And add any that are valid now that weren't before
                            // Note: We can safely call forceChunk here as nothing is iterating the list of forced chunks
                            // as the loading validators get past a
                            for (long chunkPos : chunks) {
                                if (chunkLoader.chunkSet.add(chunkPos) || ticking != chunkLoader.forceTicks) {
                                    //If we didn't already have it in our chunk set and added, or we had removed it due to it fully ticking changing,
                                    // then we also need to force the chunk
                                    TICKET_CONTROLLER.forceChunk(world, pos, ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), true, chunkLoader.forceTicks);
                                    added++;
                                }
                            }
                            //Mark the chunk loader as being initialized
                            chunkLoader.hasRegistered = true;
                            chunkLoader.prevWorld = world;
                            chunkLoader.prevPos = pos;
                            if (removed == 0 && added == 0) {
                                LOGGER.debug("Tickets for position: {} in {}, successfully validated.", pos, worldName);
                            } else {
                                chunkLoader.markDirty();
                                //Note: Info level as this may be intended/expected when configs change (for example reducing max radius of digital miner),
                                // or if some of it needs to be recalculated such as the miner no longer having a target chunk
                                LOGGER.info("validateTickets(): Removed {} no longer valid chunk tickets, and added {} newly valid chunk tickets. Pos: {} World: {}",
                                      removed, added, pos, worldName);

                            }
                        }
                    } else {
                        //Chunk loader can't operate anymore, release any tickets we have assigned to us that we loaded with
                        // Note: Info level as this may be intended/expected when if the chunk loading config changed
                        LOGGER.info("Removing {} chunk tickets as they are no longer valid as this loader cannot operate. Pos: {} World: {}", ticketCount,
                              pos, worldName);
                        releaseAllTickets(chunkLoader, pos, ticketHelper);
                    }
                } else {
                    //Not a valid chunk/tile, remove all positions
                    LOGGER.warn("Block at {}, in {}, is not a valid chunk loader. Removing {} chunk tickets.", pos, worldName, ticketCount);
                    ticketHelper.removeAllTickets(pos);
                }
            }
        }

        private void releaseAllTickets(TileComponentChunkLoader<?> chunkLoader, BlockPos pos, TicketHelper ticketHelper) {
            //Release any tickets we have assigned to us that we loaded with
            ticketHelper.removeAllTickets(pos);
            // and also clear any of the tickets the chunk loader thinks it has
            // making sure to mark it as not registered and having no previous world
            // even though those values are likely already at the proper values
            chunkLoader.chunkSet.clear();
            chunkLoader.hasRegistered = false;
            chunkLoader.prevWorld = null;
            //Mark the chunk as dirty to ensure that it saves the fact the component
            // shouldn't have any chunks loaded
            chunkLoader.markDirty();
        }
    }
}