package mekanism.common.lib.transmitter;

import java.util.UUID;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.test.GameTestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@PrefixGameTestTemplate(false)
@GameTestHolder(Mekanism.MODID)
public class TransmitterNetworkTest {

    private static final String BASE_PATH = "transmitter/";
    //Note: We make all the tests in this class have a base setup time of 5 ticks to make sure everything has had a chance to load properly
    private static final int SETUP_TICKS = 5;

    @GameTest(template = BASE_PATH + "straight_3c_cable", setupTicks = SETUP_TICKS, batch = "1")
    public static void reloadIntermediary(GameTestHelper helper) {
        GameTestUtils.succeedIfAfterReload(helper, new ChunkPos(1, 0), new MatchingNetworkValidator(helper));
    }

    /**
     * This test represents the issue that was reported in <a href="https://github.com/mekanism/Mekanism/issues/7428">Issue 7428</a> and most likely is also the last
     * remaining cause of <a href="https://github.com/mekanism/Mekanism/issues/6356">Issue 6356</a>.
     */
    @GameTest(template = BASE_PATH + "straight_3c_cable", setupTicks = SETUP_TICKS, batch = "2")
    public static void inaccessibleNotUnloaded(GameTestHelper helper) {
        ChunkPos relativeChunk = new ChunkPos(1, 0);
        BlockPos relativeTargetTransmitter = new BlockPos(0, 1, 0);
        MutableInt lastLevel = new MutableInt();
        //This is used
        MutableObject<BlockState> lastState = new MutableObject<>(Blocks.AIR.defaultBlockState());
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenWaitUntil(() -> lastLevel.setValue(GameTestUtils.setChunkLoadLevel(helper, relativeChunk, GameTestUtils.INACCESSIBLE_LEVEL)))
              //Wait 5 ticks in case anything needs more time to process after the chunk unloads
              .thenIdle(5)
              //Force a rebuild of the network by breaking one transmitter
              .thenExecute(() -> {
                  //Update the last state, so we can set it again afterwards
                  lastState.setValue(helper.getBlockState(relativeTargetTransmitter));
                  helper.setBlock(relativeTargetTransmitter, Blocks.AIR);
              })
              //Wait 5 ticks to ensure it has time to process everything (expected to only take two ticks)
              .thenIdle(5)
              //Set the block back to what it was before (the transmitter)
              .thenExecute(() -> helper.setBlock(relativeTargetTransmitter, lastState.getValue()))
              //Wait 5 ticks to ensure it has time to process everything (expected to only take two ticks)
              .thenIdle(5)
              //Set the chunk level back to what it was before (aka loading it fully again)
              .thenWaitUntil(() -> GameTestUtils.setChunkLoadLevel(helper, relativeChunk, lastLevel.getValue()))
              //Wait 5 ticks in case anything needs more time to process after the chunk loads
              .thenIdle(5)
              .thenWaitUntil(0, new MatchingNetworkValidator(helper))
        );
    }

    private static void forEachTransmitter(GameTestHelper helper, TriConsumer<TileEntityTransmitter, Transmitter<?, ?, ?>, BlockPos> consumer) {
        forEachTransmitter(helper, true, consumer);
    }

    private static void forEachTransmitter(GameTestHelper helper, boolean expectNetwork, TriConsumer<TileEntityTransmitter, Transmitter<?, ?, ?>, BlockPos> consumer) {
        helper.forEveryBlockInStructure(relativePos -> {
            TileEntityTransmitter blockEntity = getTransmitterNNAt(helper, relativePos);
            Transmitter<?, ?, ?> transmitter = blockEntity.getTransmitter();
            if (expectNetwork && !transmitter.hasTransmitterNetwork()) {
                helper.fail("No transmitter network found", relativePos);
            }
            consumer.accept(blockEntity, transmitter, relativePos);
        });
    }

    @Nullable
    private static TileEntityTransmitter getTransmitterAt(GameTestHelper helper, BlockPos relativePos) {
        return GameTestUtils.getBlockEntity(helper, TileEntityTransmitter.class, relativePos);
    }

    @NotNull
    private static TileEntityTransmitter getTransmitterNNAt(GameTestHelper helper, BlockPos relativePos) {
        TileEntityTransmitter transmitter = getTransmitterAt(helper, relativePos);
        if (transmitter == null) {
            helper.fail("Expected transmitter", relativePos);
        }
        //noinspection ConstantConditions (can't get heve if null as helper#fail throws an exception)
        return transmitter;
    }

    private static class MatchingNetworkValidator implements Runnable {

        private final GameTestHelper helper;
        private UUID networkUUID;

        public MatchingNetworkValidator(GameTestHelper helper) {
            this.helper = helper;
        }

        @Override
        public void run() {
            forEachTransmitter(helper, (tile, transmitter, relativePos) -> {
                DynamicNetwork<?, ?, ?> network = transmitter.getTransmitterNetwork();
                if (networkUUID == null) {
                    networkUUID = network.getUUID();
                } else if (!networkUUID.equals(network.getUUID())) {
                    helper.fail("Multiple transmitter networks", relativePos);
                }
            });
        }
    }
}