package mekanism.common.chunkloading;

import java.util.Comparator;
import java.util.Iterator;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Upgrade;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.tile.component.TileComponentChunkLoader;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
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
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChunkManager extends WorldSavedData {

    private static final String CHUNK_LIST_KEY = "chunks";
    private static final Logger LOGGER = LogManager.getLogger("Mekanism ChunkManager");
    private static final String SAVEDATA_KEY = "mekanism_force_chunks";
    /** Ticket type to keep the chunk loaded initially for a short time, so the Chunkloaders can register theirs */
    private static final TicketType<ChunkPos> INITIAL_LOAD_TICKET_TYPE = TicketType.create("mekanism:initial_chunkload", Comparator.comparingLong(ChunkPos::asLong), 10);

    private ChunkMultimap chunks = new ChunkMultimap();

    public ChunkManager() {
        super(SAVEDATA_KEY);
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.chunks = new ChunkMultimap();
        this.chunks.deserializeNBT(nbt.getList(CHUNK_LIST_KEY, NBT.TAG_COMPOUND));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(CHUNK_LIST_KEY, this.chunks.serializeNBT());
        return compound;
    }

    public void registerChunk(ChunkPos chunk, BlockPos chunkLoaderPos) {
        this.chunks.add(chunk, chunkLoaderPos);
        markDirty();
    }

    public void deregisterChunk(ChunkPos chunk, BlockPos chunkLoaderPos) {
        this.chunks.remove(chunk, chunkLoaderPos);
        markDirty();
    }

    public static void worldLoad(ServerWorld world) {
        ChunkManager savedData = getInstance(world);
        LOGGER.info("Loading {} chunks for dimension {}", savedData.chunks.size(), world.dimension.getType().getRegistryName());
        savedData.chunks.long2ObjectEntrySet().fastForEach(entry -> {
            boolean shouldLoad = false;
            long key = entry.getLongKey();
            //option 1 - for each position, find TE and get it to refresh chunks
            Chunk chunk = world.getChunk((int) key, (int) (key >> 32));
            for (Iterator<BlockPos> iterator = entry.getValue().iterator(); iterator.hasNext(); ) {
                BlockPos blockPos = iterator.next();
                Block block = chunk.getBlockState(blockPos).getBlock();
                if (Attribute.has(block, AttributeUpgradeSupport.class) && Attribute.get(block, AttributeUpgradeSupport.class).getSupportedUpgrades().contains(Upgrade.ANCHOR)) {
                    shouldLoad = true;
                }else {
                    LOGGER.warn("Block at {} was does not support Anchor Upgrades?! Block: {}", blockPos, block.getRegistryName());
                    iterator.remove();
                    savedData.markDirty();
                }
            }
            if (shouldLoad) {
                //option 2 - add a separate ticket (which has a timout) to let the chunk tick for a short while (chunkloader will refresh if it's able)
                ChunkPos pos = new ChunkPos(key);
                world.getChunkProvider().registerTicket(INITIAL_LOAD_TICKET_TYPE, pos, TileComponentChunkLoader.TICKET_DISTANCE, pos);
            }
        });
    }

    public static ChunkManager getInstance(ServerWorld world) {
        return world.getSavedData().getOrCreate(ChunkManager::new, SAVEDATA_KEY);
    }
}