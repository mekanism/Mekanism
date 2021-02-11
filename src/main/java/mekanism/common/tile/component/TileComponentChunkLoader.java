package mekanism.common.tile.component;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.common.world.ForgeChunkManager.LoadingValidationCallback;
import net.minecraftforge.common.world.ForgeChunkManager.TicketHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TileComponentChunkLoader<T extends TileEntityMekanism & IChunkLoader> implements ITileComponent {

    private static final Logger LOGGER = LogManager.getLogger("Mekanism TileComponentChunkLoader");
    /**
     * TileEntity implementing this component.
     */
    private final T tile;
    private final LongSet chunkSet = new LongOpenHashSet();
    @Nullable
    private ServerWorld prevWorld;
    @Nullable
    private BlockPos prevPos;
    private boolean hasRegistered;
    @Deprecated
    private boolean isFirstTick = true;

    public TileComponentChunkLoader(T tile) {
        this.tile = tile;
        this.tile.addComponent(this);
    }

    public boolean canOperate() {
        return MekanismConfig.general.allowChunkloading.get() && tile.supportsUpgrades() && tile.getComponent().isUpgradeInstalled(Upgrade.ANCHOR);
    }

    private void releaseChunkTickets(@Nonnull ServerWorld world, @Nonnull BlockPos pos) {
        int tickets = chunkSet.size();
        LOGGER.debug("Attempting to remove {} chunk tickets. Pos: {} World: {}", tickets, pos, world.getDimensionKey().getLocation());
        if (tickets > 0) {
            for (long chunkPos : chunkSet) {
                ForgeChunkManager.forceChunk(world, Mekanism.MODID, pos, (int) chunkPos, (int) (chunkPos >> 32), false, false);
            }
            chunkSet.clear();
            markDirty();
        }
        hasRegistered = false;
        prevWorld = null;
    }

    private void registerChunkTickets(@Nonnull ServerWorld world) {
        prevPos = tile.getPos();
        prevWorld = world;
        Set<ChunkPos> chunks = tile.getChunkSet();
        int tickets = chunks.size();
        LOGGER.debug("Attempting to add {} chunk tickets. Pos: {} World: {}", tickets, prevPos, world.getDimensionKey().getLocation());
        if (tickets > 0) {
            for (ChunkPos chunkPos : chunks) {
                ForgeChunkManager.forceChunk(world, Mekanism.MODID, prevPos, chunkPos.x, chunkPos.z, true, false);
                chunkSet.add(chunkPos.asLong());
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
            refreshChunkTickets((ServerWorld) Objects.requireNonNull(tile.getWorld()), tile.getPos(), true);
        }
    }

    /**
     * @param ticketsChanged {@code true} if the chunk set of our tile changed and we need to force adjusting our registered tickets.
     *
     * @apiNote Only call server side
     */
    private void refreshChunkTickets(@Nonnull ServerWorld world, @Nonnull BlockPos pos, boolean ticketsChanged) {
        boolean canOperate = canOperate();
        if (hasRegistered && prevWorld != null && prevPos != null) {
            //Note: If we have already registered the previous world and previous position
            // should never be null but we validate this just in case
            if (prevWorld != world || !pos.equals(prevPos)) {
                //If the location changed clear all old tickets
                releaseChunkTickets(prevWorld, prevPos);
                if (canOperate) {
                    //And if we can operate, add any new ones back
                    registerChunkTickets(world);
                }
            } else if (!canOperate) {
                //If we can't operate anymore clear all old tickets
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
                    //Otherwise we need to do some more checks
                    LongSet chunks = getTileChunks();
                    if (chunks.isEmpty()) {
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
                            if (!chunks.contains(chunkPos)) {
                                //If the chunk is no longer in our chunks we want loaded
                                // then we need to unforce the chunk and remove it
                                ForgeChunkManager.forceChunk(world, Mekanism.MODID, pos, (int) chunkPos, (int) (chunkPos >> 32), false, false);
                                chunkIt.remove();
                                removed++;
                            }
                        }
                        //And add any that are valid now that weren't before
                        for (long chunkPos : chunks) {
                            if (chunkSet.add(chunkPos)) {
                                //If we didn't already have it in our chunk set and added actually added it as it is new
                                // then we also need to force the chunk
                                ForgeChunkManager.forceChunk(world, Mekanism.MODID, pos, (int) chunkPos, (int) (chunkPos >> 32), true, false);
                                added++;
                            }
                        }
                        if (removed != 0 || added != 0) {
                            markDirty();
                        }
                        LOGGER.debug("Removed {} no longer valid chunk tickets, and added {} newly valid chunk tickets. Pos: {} World: {}", removed, added, pos,
                              world.getDimensionKey().getLocation());
                    }
                }
            }
        } else if (canOperate) {
            //We haven't registered yet, but we can operate - add all of our tickets
            registerChunkTickets(world);
        }
    }

    @Override
    public void tick() {
        World world = tile.getWorld();
        if (world != null && !world.isRemote) {
            if (isFirstTick) {
                //TODO - 1.17: Remove this if branch - as it is only needed to validate loading old data
                isFirstTick = false;
                if (!canOperate()) {
                    //If we just loaded but are not actually able to operate
                    // release any tickets we have assigned to us that we loaded with
                    releaseChunkTickets((ServerWorld) world, tile.getPos());
                }
            }
            //Update tickets if the position changed or we are no longer able to operate
            refreshChunkTickets((ServerWorld) world, tile.getPos(), false);
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (!chunkSet.isEmpty()) {
            //If we currently have any chunks loaded, remove their tickets and clear them
            if (tile.hasWorld() && !tile.isRemote() && hasRegistered && prevWorld != null && prevPos != null) {
                //If we had any chunks registered remove them. When hasRegistered is true
                // prevWorld and prevPos should both be nonnull, but validate them just in case
                releaseChunkTickets(prevWorld, prevPos);
            } else {
                //Otherwise just clear the set if it somehow has elements but isn't marked as registered
                chunkSet.clear();
            }
        }
        if (nbtTags.contains(NBTConstants.CHUNK_SET, NBT.TAG_LIST)) {
            //TODO - 1.17: Remove this if branch, it is mainly used to load old data
            // so that we can store it a bit more efficiently as a long array
            ListNBT list = nbtTags.getList(NBTConstants.CHUNK_SET, NBT.TAG_LONG);
            for (INBT nbt : list) {
                chunkSet.add(((LongNBT) nbt).getLong());
            }
        } else {
            for (long chunk : nbtTags.getLongArray(NBTConstants.CHUNK_SET)) {
                chunkSet.add(chunk);
            }
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        nbtTags.putLongArray(NBTConstants.CHUNK_SET, chunkSet.toLongArray());
    }

    @Override
    public void invalidate() {
        if (!tile.isRemote() && hasRegistered && prevWorld != null && prevPos != null) {
            //If we have any chunks registered remove them. When hasRegistered is true
            // prevWorld and prevPos should both be nonnull, but validate them just in case
            releaseChunkTickets(prevWorld, prevPos);
        }
    }

    @Override
    public void addToUpdateTag(CompoundNBT updateTag) {
    }

    @Override
    public void readFromUpdateTag(CompoundNBT updateTag) {
    }

    private void markDirty() {
        if (tile.hasWorld()) {
            //Marks the chunk as dirty so it can properly save
            WorldUtils.markChunkDirty(tile.getWorld(), tile.getPos());
        }
    }

    private LongSet getTileChunks() {
        Set<ChunkPos> chunks = tile.getChunkSet();
        if (chunks.isEmpty()) {
            return LongSets.EMPTY_SET;
        }
        LongSet chunksAsLongs = new LongOpenHashSet();
        for (ChunkPos chunkPos : chunks) {
            chunksAsLongs.add(chunkPos.asLong());
        }
        return chunksAsLongs;
    }

    public static class ChunkValidationCallback implements LoadingValidationCallback {

        public static final ChunkValidationCallback INSTANCE = new ChunkValidationCallback();

        private ChunkValidationCallback() {
        }

        @Override
        public void validateTickets(@Nonnull ServerWorld world, @Nonnull TicketHelper ticketHelper) {
            ResourceLocation worldName = world.getDimensionKey().getLocation();
            LOGGER.debug("Validating tickets for: {}. Blocks: {}, Entities: {}", worldName, ticketHelper.getBlockTickets().size(),
                  ticketHelper.getEntityTickets().size());
            for (Map.Entry<BlockPos, Pair<LongSet, LongSet>> entry : ticketHelper.getBlockTickets().entrySet()) {
                //Only bother looking at non ticking chunks as we don't register any "fully" ticking chunks
                BlockPos pos = entry.getKey();
                LongSet forcedChunks = entry.getValue().getFirst();
                int ticketCount = forcedChunks.size();
                LOGGER.debug("Validating tickets for: {}, BlockPos: {}, Forced chunks: {}, Ticking forced chunks: {}", worldName, pos, ticketCount,
                      entry.getValue().getSecond().size());
                if (ticketCount > 0) {
                    //We expect this always be the case but just in case it is empty don't bother looking up the tile
                    //Note: This does not use WorldUtils#getTileEntity as we want to force the chunk to load if it isn't loaded yet
                    // so that we can properly validate it
                    TileEntity tile = world.getTileEntity(pos);
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
                                    if (!chunks.contains(chunkPos)) {
                                        //If the chunk is no longer in our chunks we want loaded, then we mark it for removal
                                        ticketHelper.removeTicket(pos, chunkPos, false);
                                        // and remove it from the set we are keeping track of
                                        chunkIt.remove();
                                        removed++;
                                    }
                                }
                                //And add any that are valid now that weren't before
                                // Note: We can safely call forceChunk here as nothing is iterating the list of forced chunks
                                // as the loading validators get passed a
                                for (long chunkPos : chunks) {
                                    if (chunkLoader.chunkSet.add(chunkPos)) {
                                        //If we didn't already have it in our chunk set and added actually added it as it is new
                                        // then we also need to force the chunk
                                        ForgeChunkManager.forceChunk(world, Mekanism.MODID, pos, (int) chunkPos, (int) (chunkPos >> 32), true, false);
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
                                    //Note: Info level as this may be intended/expected when configs change (for example reducing max radius of digital miner)
                                    LOGGER.info("Removed {} no longer valid chunk tickets, and added {} newly valid chunk tickets. Pos: {} World: {}",
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
                        //Mark that we are no longer the first tick as that is just for data validation
                        // and the data for our chunk loader has already been transitioned over to the
                        // Forge Chunk Manager, then we just finished doing the validation for it above
                        chunkLoader.isFirstTick = false;
                    } else {
                        //Not a valid chunk/tile, remove all positions
                        LOGGER.warn("Block at {}, in {}, is not a valid chunk loader. Removing {} chunk tickets.", pos, worldName, ticketCount);
                        ticketHelper.removeAllTickets(pos);
                    }
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
            //Mark the the chunk as dirty to ensure that it saves the fact the component
            // shouldn't have any chunks loaded
            chunkLoader.markDirty();
        }
    }
}