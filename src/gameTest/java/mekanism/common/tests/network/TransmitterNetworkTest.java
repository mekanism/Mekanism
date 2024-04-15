package mekanism.common.tests.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.functions.TriConsumer;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tests.MekanismTests;
import mekanism.common.tests.util.GameTestUtils;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import org.jetbrains.annotations.NotNull;
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
    public static void reloadIntermediary(final ExtendedGameTestHelper helper) {
        GameTestUtils.succeedIfAfterReload(helper, new ChunkPos(1, 0), new MatchingNetworkValidator(helper));
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
              .thenIdle(5)
              .thenWaitUntil(0, new MatchingNetworkValidator(helper))
              .thenSucceed();
    }

    private static void forEachTransmitter(ExtendedGameTestHelper helper, TriConsumer<TileEntityTransmitter, Transmitter<?, ?, ?>, BlockPos> consumer) {
        forEachTransmitter(helper, true, consumer);
    }

    private static void forEachTransmitter(ExtendedGameTestHelper helper, boolean expectNetwork, TriConsumer<TileEntityTransmitter, Transmitter<?, ?, ?>, BlockPos> consumer) {
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
    private static TileEntityTransmitter getTransmitterAt(ExtendedGameTestHelper helper, BlockPos relativePos) {
        return GameTestUtils.getBlockEntity(helper, TileEntityTransmitter.class, relativePos);
    }

    @NotNull
    private static TileEntityTransmitter getTransmitterNNAt(ExtendedGameTestHelper helper, BlockPos relativePos) {
        TileEntityTransmitter transmitter = getTransmitterAt(helper, relativePos);
        if (transmitter == null) {
            helper.fail("Expected transmitter", relativePos);
        }
        //noinspection ConstantConditions (can't get heve if null as helper#fail throws an exception)
        return transmitter;
    }

    private static class MatchingNetworkValidator implements Runnable {

        private final ExtendedGameTestHelper helper;
        private UUID networkUUID;

        public MatchingNetworkValidator(ExtendedGameTestHelper helper) {
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