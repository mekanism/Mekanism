package mekanism.common.tests.network;

import static mekanism.common.tests.util.TransporterTestUtils.colored;
import static mekanism.common.tests.util.TransporterTestUtils.configured;
import static mekanism.common.tests.util.TransporterTestUtils.containing;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tests.MekanismTests;
import mekanism.common.tests.util.GameTestUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = "network.inventory")
public class InventoryNetworkTest {

    //Note: We make all the tests in this class have a base setup time of 5 ticks to make sure everything has had a chance to load properly
    private static final int SETUP_TICKS = SharedConstants.TICKS_PER_SECOND / 4;
    private static final int TIMEOUT_TICKS = 50 * SharedConstants.TICKS_PER_SECOND;

    private static final String UPGRADEABLE = MekanismTests.MODID + ":upgradeable_transporter";
    //Note: Our template is lazy so that we ensure the transporters are registered
    @RegisterStructureTemplate(UPGRADEABLE)
    public static final Supplier<StructureTemplate> UPGRADEABLE_TEMPLATE = StructureTemplateBuilder.lazy(10, 1, 3, builder -> builder
          //Start barrel
          .set(3, 0, 0, Blocks.BARREL.defaultBlockState(), containing(Items.STONE, 20))
          //End barrels
          .set(2, 0, 0, Blocks.BARREL.defaultBlockState())
          .set(8, 0, 0, Blocks.BARREL.defaultBlockState())

          .set(3, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), configured(Direction.NORTH))

          .fill(0, 0, 0, 1, 0, 0, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
          .set(0, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
          .fill(0, 0, 2, 9, 0, 2, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
          .fill(9, 0, 0, 9, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
    );

    //TODO: Do we want to somehow test the case of when we add a shorter path to the same destination, as the newly pulled items go via the new shorter path
    // but any already en-route ones continue the original way

    //TODO: I am not sure that when breaking transporters (especially basic ones) they reliably drop everything they have in them? Might want to test a bit
    //TODO: Write a test for the following
    // - Cutting off an existing path -> recalculating pathfinding of currently travelling stacks, for example when setting a side to none or breaking a transporter
    // - Color changing on an existing path??
    // - Diversion transporters?? Might basically be just the same as the cutting off existing path
    // - destination is removed
    // - destination becomes inaccessible due to side changes
    // - base path becomes disabled but there is a different path to the same destination
    // - Remove destination and source and let it idle for a bit in the transporter then add a new spot and validate it all enters it??
    // - Make destination no longer able to accept item, validate input doesn't contain stack, and then validate it made its way back to the input
    // - Make destination no longer able to accept item, fill input fully, wait a little, and then allow for room in the destination, validate it makes it there
    // - Single transporter with no destination stays in the transporter, it fails and pops out if there is only a single connection point
    //   Note: We may want to change that behavior so even with a single connection point it just stays idling and only pops out if there are zero connection points
    // -

    @GameTest(setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that newly pulled items will go to the new destination that has a shorter path, "
                              + "but any items that were already en-route will continue to the destination they had already calculated.")
    public static void shorterNewDestination(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(3, 1, 6)
              //Start barrel
              .set(0, 0, 0, Blocks.BARREL.defaultBlockState(), containing(Items.STONE, 20))
              //End barrels
              .set(0, 0, 5, Blocks.BARREL.defaultBlockState())
              .set(2, 0, 2, Blocks.BARREL.defaultBlockState())

              .set(0, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), configured(Direction.NORTH))
              .fill(0, 0, 2, 0, 0, 4, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
        );

        test.onGameTest(helper -> helper.startSequence()
              //Wait a few seconds for it to pull some items out, and add a transporter to create a shorter destination
              .thenExecuteAfter(4 * SharedConstants.TICKS_PER_SECOND, () -> helper.setBlock(new BlockPos(1, 1, 2), MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock()))
              .thenExecuteAfter(10 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(0, 1, 5), 0, new ItemStack(Items.STONE, 8));
                  //Validate new destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 2), 0, new ItemStack(Items.STONE, 12));
                  //Validate start is also empty
                  helper.assertContainerEmpty(0, 1, 0);
              }).thenSucceed()
        );
    }

    @GameTest(setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that newly pulled items will go to the new destination that has a shorter path now that it has been re-enabled, "
                              + "but any items that were already en-route will continue to the destination they had already calculated.")
    public static void shorterEnabledPath(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(3, 1, 6)
              //Start barrel
              .set(0, 0, 0, Blocks.BARREL.defaultBlockState(), containing(Items.STONE, 20))
              //End barrels
              .set(0, 0, 5, Blocks.BARREL.defaultBlockState())
              .set(2, 0, 2, Blocks.BARREL.defaultBlockState())

              .set(0, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), configured(Direction.NORTH))
              .fill(0, 0, 2, 0, 0, 4, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())

              .set(1, 0, 3, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
              .set(1, 0, 2, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), configured(null, Direction.WEST, ConnectionType.NONE))
        );

        test.onGameTest(helper -> helper.startSequence()
              //Wait a few seconds for it to pull some items out, re-enable a disabled path to make there be a shorter destination
              .thenExecuteAfter(4 * SharedConstants.TICKS_PER_SECOND, () -> {
                  Player player = helper.makeMockPlayer();
                  player.setShiftKeyDown(true);
                  helper.useOn(new BlockPos(1, 1, 2), MekanismItems.CONFIGURATOR.getItemStack(), player, Direction.WEST);
              }).thenExecuteAfter(10 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(0, 1, 5), 0, new ItemStack(Items.STONE, 8));
                  //Validate new destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 2), 0, new ItemStack(Items.STONE, 12));
                  //Validate start is also empty
                  helper.assertContainerEmpty(0, 1, 0);
              }).thenSucceed()
        );
    }

    @GameTest(setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that nothing changes as colorless transporter stacks cannot enter a colored transporter.")
    public static void colorlessIntoColor(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(2, 1, 2)
              //Start barrel
              .set(0, 0, 0, Blocks.BARREL.defaultBlockState(), containing(Items.STONE))
              //End barrel
              .set(1, 0, 0, Blocks.BARREL.defaultBlockState())
              .set(0, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), configured(Direction.NORTH))
              .set(1, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), colored(EnumColor.BLACK))
        );

        //Note: We initialize the starting inventory above
        test.onGameTest(helper -> helper.startSequence()
              //Wait a little to validate nothing is happening
              .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate original destination has the starting amount
                  helper.assertContainerContains(0, 1, 0, Items.STONE);
                  //Validate new destination is still empty
                  helper.assertContainerEmpty(1, 1, 0);
              }).thenSucceed()
        );
    }

    @GameTest(setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that the stack moves to the further chest as the closer one has the color not matching.")
    public static void colorMatches(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(3, 1, 4)
              //Start barrel
              .set(0, 0, 0, Blocks.BARREL.defaultBlockState(), containing(Items.STONE, 2))
              //End barrels
              .set(2, 0, 2, Blocks.BARREL.defaultBlockState())
              .set(2, 0, 3, Blocks.BARREL.defaultBlockState())

              .set(0, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), colored(EnumColor.BLACK, Direction.NORTH))
              .set(1, 0, 2, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), colored(EnumColor.DARK_BLUE))
              .set(1, 0, 3, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), colored(EnumColor.BLACK))
              .fill(0, 0, 2, 0, 0, 3, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
        );

        test.onGameTest(helper -> helper.startSequence()
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(5 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate colored destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 3), 0, new ItemStack(Items.STONE, 2));
                  //Validate wrong color destination is empty
                  helper.assertContainerEmpty(2, 1, 2);
                  //Validate start is also empty
                  helper.assertContainerEmpty(0, 1, 0);
              }).thenSucceed()
        );
    }

    @GameTest(setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that the stack moves to the closer destination as even though it is colorless and the further one has the original color, "
                              + "it is further away.")
    public static void colorIsNotPriority(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(2, 1, 4)
              //Start barrel
              .set(0, 0, 0, Blocks.BARREL.defaultBlockState(), containing(Items.STONE, 2))
              //End barrels
              .set(1, 0, 2, Blocks.BARREL.defaultBlockState())
              .set(1, 0, 3, Blocks.BARREL.defaultBlockState())

              .set(0, 0, 1, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), colored(EnumColor.BLACK, Direction.NORTH))
              .set(0, 0, 2, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState())
              .set(0, 0, 3, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().defaultBlockState(), colored(EnumColor.BLACK))
        );

        test.onGameTest(helper -> helper.startSequence()
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(5 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate colored destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(1, 1, 2), 0, new ItemStack(Items.STONE, 2));
                  //Validate wrong color destination is empty
                  helper.assertContainerEmpty(1, 1, 3);
                  //Validate start is also empty
                  helper.assertContainerEmpty(0, 1, 0);
              }).thenSucceed()
        );
    }

    @GameTest(template = UPGRADEABLE, setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that newly pulled items will go to the destination that had its path upgraded, "
                              + "but any items that were already en-route will continue to the destination they had already calculated.")
    public static void upgradeFurtherPath(final ExtendedGameTestHelper helper) {
        helper.startSequence()
              //Wait a few seconds for it to pull some items out, and upgrade the transporter to the further destination
              .thenExecuteAfter(4 * SharedConstants.TICKS_PER_SECOND, () -> applyAlloyUpgrade(helper, new BlockPos(9, 1, 0), AlloyTier.INFUSED))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(13 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 0), 0, new ItemStack(Items.STONE, 8));
                  //Validate further but now "closer" destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(8, 1, 0), 0, new ItemStack(Items.STONE, 12));
                  //Validate start is also empty
                  helper.assertContainerEmpty(3, 1, 0);
              }).thenSucceed();
    }

    @GameTest(template = UPGRADEABLE, setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that newly pulled items will go to the destination that had its path upgraded, "
                              + "but any items that were already en-route will continue to the destination they had "
                              + "already calculated as the new destination is slightly \"closer\".")
    public static void upgradeFurtherOverlapping(final ExtendedGameTestHelper helper) {
        helper.startSequence()
              //Wait a few seconds for it to pull some items out, and upgrade the transporter to the further destination
              .thenExecuteAfter(4 * SharedConstants.TICKS_PER_SECOND, () -> applyAlloyUpgrade(helper, new BlockPos(6, 1, 2), AlloyTier.INFUSED))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(13 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 0), 0, new ItemStack(Items.STONE, 8));
                  //Validate further but now "closer" destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(8, 1, 0), 0, new ItemStack(Items.STONE, 12));
                  //Validate start is also empty
                  helper.assertContainerEmpty(3, 1, 0);
              }).thenSucceed();
    }

    @GameTest(template = UPGRADEABLE, setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    @TestHolder(description = "Tests that all items pre- and post-upgrade will go to the original destination.")
    public static void upgradeExisting(final ExtendedGameTestHelper helper) {
        helper.startSequence()
              //Wait a few seconds for it to pull some items out, and upgrade the transporter to the further destination
              .thenExecuteAfter(4 * SharedConstants.TICKS_PER_SECOND, () -> applyAlloyUpgrade(helper, new BlockPos(3, 1, 2), AlloyTier.INFUSED))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(6 * SharedConstants.TICKS_PER_SECOND, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 0), 0, new ItemStack(Items.STONE, 20));
                  //Validate further is still empty
                  helper.assertContainerEmpty(8, 1, 0);
                  //Validate start is also empty
                  helper.assertContainerEmpty(3, 1, 0);
              }).thenSucceed();
    }

    private static void applyAlloyUpgrade(ExtendedGameTestHelper helper, BlockPos relativePos, AlloyTier tier) {
        helper.useOn(relativePos, (switch (tier) {
            case INFUSED -> MekanismItems.INFUSED_ALLOY;
            case REINFORCED -> MekanismItems.REINFORCED_ALLOY;
            case ATOMIC -> MekanismItems.ATOMIC_ALLOY;
        }).getItemStack(), helper.makeMockPlayer(), Direction.UP);
    }
}