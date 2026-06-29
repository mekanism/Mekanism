package mekanism.common.world;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Mekanism.MODID)
public class QueuedRegenLevelData {

    private static final long maximumDeltaTimeNanoSecs = Duration.ofMillis(16).toNanos();

    private final Queue<ChunkPos> chunksToGen = new LinkedList<>();

    public void addRegenChunk(ChunkPos chunk) {
        if (!chunksToGen.contains(chunk)) {
            chunksToGen.add(chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ChunkAccess chunk = event.getChunk();
            int userGenVersion = MekanismConfig.world.userGenVersion.get();
            if (event.isNewChunk()) {
                //New chunks just get the chunk version set to the current version
                if (userGenVersion > 0) {
                    chunk.setData(MekanismAttachmentTypes.CHUNK_VERSION, userGenVersion);
                }
                return;
            }
            if (MekanismConfig.world.enableRegeneration.get()) {
                int version = chunk.getData(MekanismAttachmentTypes.CHUNK_VERSION);
                //When a chunk is loaded, if it has an older version than the latest one and retrogen is enabled
                if (version < userGenVersion) {
                    //Track what version it has so that when we save it, if we haven't gotten a chance to update
                    // the chunk yet, then we are able to properly save that we still will need to update it
                    level.getData(MekanismAttachmentTypes.QUEUED_REGEN_LEVEL_DATA).addRegenChunk(chunk.getPos());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tickLevel(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && MekanismConfig.world.enableRegeneration.get()) {
            QueuedRegenLevelData queuedRegenData = level.getExistingDataOrNull(MekanismAttachmentTypes.QUEUED_REGEN_LEVEL_DATA);
            if (queuedRegenData != null) {
                //Credit to E. Beef
                int version = MekanismConfig.world.userGenVersion.get();
                long startTime = Util.getNanos();
                //TODO - 26.2: Do we want to check event.hasTime()?
                while (Util.getNanos() - startTime < maximumDeltaTimeNanoSecs && !queuedRegenData.chunksToGen.isEmpty()) {
                    ChunkPos nextChunk = queuedRegenData.chunksToGen.poll();
                    //Ensure the chunk actually exists and is still loaded before trying to retrogen it
                    // Similar to WorldUtils#isChunkLoaded
                    ChunkAccess chunk = level.getChunk(nextChunk.x(), nextChunk.z(), ChunkStatus.FULL, false);
                    if (chunk != null) {
                        if (GenHandler.generate(level, nextChunk)) {
                            Mekanism.logger.info("Regenerating ores and salt at chunk {}", nextChunk);
                        }
                        //Regardless of whether we were able to generate anything in the chunk, now that we have handled it, update the chunk version.
                        chunk.setData(MekanismAttachmentTypes.CHUNK_VERSION, version);
                    }
                }
            }
        }
    }
}