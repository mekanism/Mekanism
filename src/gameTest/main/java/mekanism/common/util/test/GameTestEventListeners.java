package mekanism.common.util.test;

import it.unimi.dsi.fastutil.longs.Long2BooleanArrayMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import mekanism.common.Mekanism;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Mekanism.MODID)
public class GameTestEventListeners {

    //TODO: Do we want to eventually try and make this work for multiple dimensions??
    //TODO: Do we want to make it so that we have some unique identifier for purposes of removing no longer watched chunks??
    // This would effectively make it a table, but currently we just make sure things that adjust chunks are in their own
    // game test batch
    //Note: We use an array map as we are unlikely to be watching a large number of chunks at any one time
    static final Long2BooleanMap watchedChunks = new Long2BooleanArrayMap();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        chunkLoadStateChange(event, true);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        chunkLoadStateChange(event, false);
    }

    private static void chunkLoadStateChange(ChunkEvent event, boolean loaded) {
        LevelAccessor level = event.getLevel();
        if (level != null && !level.isClientSide()) {
            ChunkPos pos = event.getChunk().getPos();
            long chunkPos = pos.toLong();
            //If we are watching the chunk and the loaded state isn't what we already had it as
            if (watchedChunks.getOrDefault(chunkPos, loaded) != loaded) {
                watchedChunks.put(chunkPos, loaded);
                if (GameTestUtils.DEBUG_CHUNK_LOADING) {
                    Mekanism.logger.info("Chunk {}: {}, {}", loaded ? "loaded" : "unloaded", pos.x, pos.z);
                }
            } else if (GameTestUtils.DEBUG_CHUNK_LOADING && watchedChunks.containsKey(chunkPos)) {
                Mekanism.logger.info("Chunk was already {}: {}, {}", loaded ? "loaded" : "unloaded", pos.x, pos.z);
            }
        }
    }
}