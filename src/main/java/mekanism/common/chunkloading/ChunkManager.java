package mekanism.common.chunkloading;

import java.util.function.LongConsumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Saved data for managing Mekanism chunkloaders.
 *
 * Stores a MultiMap style Map of ChunkPos(long) -> List of block positions of Chunkloaders.
 *
 * Removes the risk of vanilla forced chunks being unforced on us
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = Mekanism.MODID)
public class ChunkManager extends WorldSavedData {

    private static final String CHUNK_LIST_KEY = "chunks";
    private static final Logger LOGGER = LogManager.getLogger("Mekanism ChunkManager");
    private static final String SAVEDATA_KEY = "mekanism_force_chunks";
    ///** Ticket type to keep the chunk loaded initially for a short time, so the Chunkloaders can register theirs */
    //private static final TicketType<ChunkPos> INITIAL_LOAD_TICKET_TYPE = TicketType.create("mekanism:initial_chunkload", Comparator.comparingLong(ChunkPos::asLong), 10);

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

    @SubscribeEvent
    public static void worldLoadEvent(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            ChunkManager savedData = getInstance(world);
            LOGGER.info("Loading {} chunks for dimension {}", savedData.chunks.size(), world.dimension.getType().getRegistryName());
            savedData.chunks.keySet().forEach((LongConsumer) key -> {
                //option 1 - for each position, find TE and get it to refresh chunks
                Chunk chunk = world.getChunk((int) key, (int) (key >> 32));
                for (BlockPos blockPos : savedData.chunks.get(key)) {
                    TileEntity tileEntity = chunk.getTileEntity(blockPos);
                    if (tileEntity instanceof IChunkLoader) {
                        ((IChunkLoader) tileEntity).getChunkLoader().refreshChunkTickets();
                    } else {
                        LOGGER.warn("Tile at {} was either null or not an IChunkLoader?! Tile: {}", blockPos, tileEntity);
                    }
                }

                //option 2 - add a separate ticket (which has a timout) to let the chunk tick for a short while (chunkloader will refresh if it's able)
                /*ChunkPos pos = new ChunkPos(key);
                world.getChunkProvider().registerTicket(INITIAL_LOAD_TICKET_TYPE, pos, TileComponentChunkLoader.TICKET_DISTANCE, pos);*/
            });
        }
    }

    public static ChunkManager getInstance(ServerWorld world) {
        return world.getSavedData().getOrCreate(ChunkManager::new, SAVEDATA_KEY);
    }
}