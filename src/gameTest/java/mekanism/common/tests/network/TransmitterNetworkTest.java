package mekanism.common.tests.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tests.MekanismTests;
import mekanism.common.tests.util.GameTestUtils;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import org.jetbrains.annotations.Nullable;

@ForEachTest(groups = "network.transmitter")
public class TransmitterNetworkTest {

    private static final String STRAIGHT_CABLE = MekanismTests.MODID + ":straight_cable";
    //Note: Our template is lazy so that we ensure the universal cable is registered
    @RegisterStructureTemplate(STRAIGHT_CABLE)
    public static final Supplier<StructureTemplate> STRAIGHT_CABLE_TEMPLATE = StructureTemplateBuilder.lazy(3 * 16, 1, 1,
          builder -> builder.fill(0, 0, 0, 3 * 16 - 1, 0, 0, MekanismBlocks.BASIC_UNIVERSAL_CABLE.getBlock())
    );

    //Note: We make all the tests in this class have a base setup time of 5 ticks to make sure everything has had a chance to load properly
    private static final int SETUP_TICKS = SharedConstants.TICKS_PER_SECOND / 4;

    @GameTest(template = STRAIGHT_CABLE, setupTicks = SETUP_TICKS, batch = "1")
    @TestHolder(description = "Tests that reloading intermediary chunks does not cause a network to break.")
    public static void reloadIntermediary(final DynamicTest test) {
        final ChunkData chunkData = new ChunkData(1, 0);
        test.eventListeners().forge().addListener((final ChunkEvent.Unload event) -> chunkData.updateChunk(event, false));
        test.eventListeners().forge().addListener((final ChunkEvent.Load event) -> chunkData.updateChunk(event, true));

        test.onGameTest(helper -> helper.startSequence()
              .thenMap(() -> chunkData.updateChunkLoading(helper, false, GameTestUtils.UNLOAD_LEVEL))
              .thenWaitUntil(() -> chunkData.waitFor(helper, false))
              //Wait 5 ticks in case anything needs more time to process after the chunk unloads
              .thenExecuteAfter(5, level -> chunkData.updateChunkLoading(helper, true, level))
              .thenWaitUntil(() -> chunkData.waitFor(helper, true))
              //Wait 5 ticks in case anything needs more time to process after the chunk loads
              .thenExecuteAfter(5, new MatchingNetworkValidator(helper))
              .thenSucceed()
        );
    }

    /**
     * This test represents the issue that was reported in <a href="https://github.com/mekanism/Mekanism/issues/7428">Issue 7428</a> and most likely is also the last
     * remaining cause of <a href="https://github.com/mekanism/Mekanism/issues/6356">Issue 6356</a>.
     */
    @GameTest(template = STRAIGHT_CABLE, setupTicks = SETUP_TICKS, batch = "2")
    @TestHolder(description = "Tests that when part of a network becomes inaccessible but still loaded, "
                              + "we are able to properly remove the transmitters and then recover when the chunk becomes accessible again.")
    public static void inaccessibleNotUnloaded(final ExtendedGameTestHelper helper) {
        ChunkPos relativeChunk = new ChunkPos(1, 0);
        BlockPos relativeTargetTransmitter = new BlockPos(0, 1, 0);
        helper.startSequence()
              .thenMap(() -> GameTestUtils.setChunkLoadLevel(helper, relativeChunk, GameTestUtils.INACCESSIBLE_LEVEL))
              //Wait 5 ticks in case anything needs more time to process after the chunk unloads
              .thenIdle(5)
              .thenSequence(sequence -> sequence.thenMap(() -> helper.getBlockState(relativeTargetTransmitter))
                    //Force a rebuild of the network by breaking one transmitter
                    .thenExecute(() -> helper.setBlock(relativeTargetTransmitter, Blocks.AIR))
                    //Wait 5 ticks to ensure it has time to process everything (expected to only take two ticks)
                    //Set the block back to what it was before (the transmitter)
                    .thenExecuteAfter(5, state -> helper.setBlock(relativeTargetTransmitter, state))
              )
              //Wait 5 ticks to ensure it has time to process everything (expected to only take two ticks)
              //Set the chunk level back to what it was before (aka loading it fully again)
              .thenExecuteAfter(5, level -> GameTestUtils.setChunkLoadLevel(helper, relativeChunk, level))
              //Wait 5 ticks in case anything needs more time to process after the chunk loads
              .thenExecuteAfter(5, new MatchingNetworkValidator(helper))
              .thenSucceed();
    }

    private static class MatchingNetworkValidator implements Runnable {

        private final ExtendedGameTestHelper helper;
        private UUID networkUUID;

        public MatchingNetworkValidator(ExtendedGameTestHelper helper) {
            this.helper = helper;
        }

        @Override
        public void run() {
            helper.forEveryBlockInStructure(relativePos -> {
                if (WorldUtils.isBlockLoaded(helper.getLevel(), helper.absolutePos(relativePos))) {
                    Transmitter<?, ?, ?> transmitter = helper.requireBlockEntity(relativePos, TileEntityTransmitter.class).getTransmitter();
                    if (!transmitter.hasTransmitterNetwork()) {
                        helper.fail("No transmitter network found", relativePos);
                    }
                    DynamicNetwork<?, ?, ?> network = transmitter.getTransmitterNetwork();
                    if (networkUUID == null) {
                        networkUUID = network.getUUID();
                    } else if (!networkUUID.equals(network.getUUID())) {
                        helper.fail("Multiple transmitter networks", relativePos);
                    }
                } else {
                    helper.fail("Expected expected position to be loaded", relativePos);
                }
            });
        }
    }

    private static class ChunkData {

        private static final boolean DEBUG_CHUNK_LOADING = false;

        private final ChunkPos relativePos;
        @Nullable
        private ChunkPos absolutePos;
        private long absPos;
        private boolean isLoaded;

        public ChunkData(int x, int y) {
            this.relativePos = new ChunkPos(x, y);
        }

        //TODO - GameTest: Can we make unloads not cause the game to crash if a player tries to run them in world? It crashes as we force unload a chunk that would be near the player and it crashes from a null pointer
        public int updateChunkLoading(ExtendedGameTestHelper helper, boolean load, int level) {
            if (absolutePos == null) {
                absolutePos = GameTestUtils.absolutePos(helper, relativePos);
                absPos = absolutePos.toLong();
            }
            ServerLevel serverLevel = helper.getLevel();
            if (WorldUtils.isChunkLoaded(serverLevel, absolutePos) != load) {
                //If the chunk isn't watched and is loaded we want to try and unload it
                ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
                DistanceManager distanceManager = chunkMap.getDistanceManager();
                ChunkHolder holder = distanceManager.getChunk(absPos);
                //Watch the chunk and mark whether it is currently loaded or not
                if ((holder == null) == load) {
                    //If it is loaded then we need to try and unload it
                    isLoaded = !load;
                    if (DEBUG_CHUNK_LOADING) {
                        Mekanism.logger.info("Trying to {} chunk at: {}", load ? "load" : "unload", absolutePos);
                    }
                    if (load) {
                        //Load the chunk to the level it was unloaded at
                        holder = distanceManager.updateChunkScheduling(absPos, level, holder, GameTestUtils.UNLOAD_LEVEL);
                        if (holder == null) {//Should never happen unless start value was unloaded
                            fail(helper, "Error loading chunk");
                        } else {
                            //And ensure we schedule it based on the status (in general this should be ChunkStatus.FULL)
                            chunkMap.schedule(holder, ChunkLevel.generationStatus(holder.getTicketLevel()));
                        }
                    } else {
                        //If it is currently loaded, queue it for unload
                        level = holder.getTicketLevel();
                        distanceManager.updateChunkScheduling(absPos, GameTestUtils.UNLOAD_LEVEL, holder, level);
                        //And then unload it
                        chunkMap.processUnloads(ConstantPredicates.ALWAYS_TRUE);
                    }
                } else if (DEBUG_CHUNK_LOADING) {
                    //Note: Even with debug logging enabled odds are this case isn't even possible due to the earlier check to skip if unloaded
                    Mekanism.logger.info("Trying to {} already {} chunk at: {}", load ? "load" : "unload", load ? "loaded" : "unloaded", absolutePos);
                }
            } else if (DEBUG_CHUNK_LOADING) {
                Mekanism.logger.info("Chunk at: {} is already {}", absolutePos, load ? "unloaded" : "loaded");
            }
            return level;
        }

        public void updateChunk(ChunkEvent event, boolean loaded) {
            if (!event.getLevel().isClientSide() && event.getChunk().getPos().equals(absolutePos)) {
                //If we are watching the chunk and the loaded state isn't what we already had it as
                if (isLoaded != loaded) {
                    isLoaded = loaded;
                    if (DEBUG_CHUNK_LOADING) {
                        Mekanism.logger.info("Chunk {}: {}", loaded ? "loaded" : "unloaded", absolutePos);
                    }
                } else if (DEBUG_CHUNK_LOADING) {
                    Mekanism.logger.info("Chunk was already {}: {}", loaded ? "loaded" : "unloaded", absolutePos);
                }
            }
        }

        public void fail(ExtendedGameTestHelper helper, String message) {
            helper.fail(message + " at " + absolutePos + " (relative: " + relativePos + ")");
        }

        public void waitFor(ExtendedGameTestHelper helper, boolean loaded) {
            if (isLoaded != loaded) {//If our loaded status does not match our desired one, keep throwing an exception until it does
                fail(helper, "Chunk has not been marked as " + (loaded ? "loaded" : "unloaded") + " yet");
            }
        }
    }
}