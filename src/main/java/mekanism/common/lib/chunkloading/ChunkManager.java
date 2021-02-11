package mekanism.common.lib.chunkloading;

import java.util.Comparator;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Saved data for managing Mekanism chunkloaders.
 *
 * Stores a MultiMap style Map of ChunkPos(long) -> List of block positions of Chunkloaders.
 *
 * Removes the risk of vanilla forced chunks being unforced on us
 *
 * @deprecated Exists to allow loading old data, but is no longer used otherwise as Forge's Chunk Manager does what we need
 */
@Deprecated
public class ChunkManager extends WorldSavedData {//TODO - 1.17: Remove this class as it mainly exists to transition the old data over to Forge's Chunk Manager

    private static final String CHUNK_LIST_KEY = "chunks";
    private static final Logger LOGGER = LogManager.getLogger("Mekanism ChunkManager");
    private static final String SAVEDATA_KEY = "mekanism_force_chunks";
    /** Ticket type to keep the chunk loaded initially for a short time, so the Chunkloaders can register theirs */
    private static final TicketType<ChunkPos> INITIAL_LOAD_TICKET_TYPE = TicketType.create("mekanism:initial_chunkload", Comparator.comparingLong(ChunkPos::asLong), 10);

    private ChunkMultimap chunks = new ChunkMultimap();

    private ChunkManager() {
        super(SAVEDATA_KEY);
    }

    @Override
    public void read(@Nonnull CompoundNBT nbt) {
        this.chunks = new ChunkMultimap();
        this.chunks.deserializeNBT(nbt.getList(CHUNK_LIST_KEY, NBT.TAG_COMPOUND));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound.put(CHUNK_LIST_KEY, this.chunks.serializeNBT());
        return compound;
    }

    public static void worldLoad(ServerWorld world) {
        //Only get, don't create as missing as we don't actually use this anymore
        DimensionSavedDataManager savedDataManager = world.getSavedData();
        ChunkManager savedData = savedDataManager.get(ChunkManager::new, SAVEDATA_KEY);
        if (savedData != null) {
            int chunks = savedData.chunks.size();
            ResourceLocation dimension = world.getDimensionKey().getLocation();
            if (chunks > 0) {
                LOGGER.info("Loading {} chunks for dimension {}", chunks, dimension);
                savedData.chunks.long2ObjectEntrySet().fastForEach(entry -> {
                    //Add a separate ticket (which has a timout) to let the chunk tick for a short while (chunkloader will refresh if it's able)
                    // This is required as we are too early with this old system to do any validation about tiles (or blocks) being valid still or not,
                    // due to the multithreading of world loading and some potential thread locking that exists from querying the world during load.
                    // This gives enough time for our chunk loader to properly add and switch over to the new system
                    ChunkPos pos = new ChunkPos(entry.getLongKey());
                    world.getChunkProvider().registerTicket(INITIAL_LOAD_TICKET_TYPE, pos, 2, pos);
                });
                //Now that we loaded it, clear all the old data so that we don't try to load it again on the next start
                savedData.chunks.clear();
                savedData.markDirty();
                LOGGER.info("Loaded {} chunks for dimension {}. The {} data file for this dimension can now be safely removed as the data has been transferred to "
                            + "Forge's Chunk Manager.", chunks, dimension, SAVEDATA_KEY);
            } else {
                LOGGER.info("The {} data file for {} dimension can now be safely removed as it is no longer in use.", SAVEDATA_KEY, dimension);
            }
        }
    }
}