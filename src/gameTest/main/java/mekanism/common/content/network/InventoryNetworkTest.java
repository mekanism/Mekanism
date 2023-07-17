package mekanism.common.content.network;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigurable;
import mekanism.api.tier.AlloyTier;
import mekanism.common.Mekanism;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.test.GameTestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

@PrefixGameTestTemplate(false)
@GameTestHolder(Mekanism.MODID)
public class InventoryNetworkTest {

    private static final String BASE_PATH = "transporter/";
    //Note: We make all the tests in this class have a base setup time of 5 ticks to make sure everything has had a chance to load properly
    private static final int SETUP_TICKS = 5;
    private static final int TIMEOUT_TICKS = 1_000;

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

    /**
     * Expected behavior: Any newly pulled items will go to the new destination that has a shorter path, but any items that were already en-route will continue to the
     * destination they had already calculated.
     */
    @GameTest(template = BASE_PATH + "shorter_new_destination", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void shorter_new_destination(GameTestHelper helper) {
        BlockPos start = new BlockPos(0, 1, 0);
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenExecute(() -> initializeStarterInventory(helper, start, new ItemStack(Items.STONE, 20)))
              //Wait a few seconds for it to pull some items out, and add a transporter to create a shorter destination
              .thenExecuteAfter(5 * 20, () -> helper.setBlock(new BlockPos(1, 1, 2), MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock()))
              .thenExecuteAfter(9 * 20, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(0, 1, 5), 0, new ItemStack(Items.STONE, 9));
                  //Validate new destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 2), 0, new ItemStack(Items.STONE, 11));
                  //Validate start is also empty
                  GameTestUtils.validateContainerEmpty(helper, start, 0);
              })
        );
    }

    /**
     * Expected behavior: Any newly pulled items will go to the new destination that has a shorter path now that it has been re-enabled, but any items that were already
     * en-route will continue to the destination they had already calculated.
     */
    @GameTest(template = BASE_PATH + "shorter_enabled_path", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void shorter_enabled_path(GameTestHelper helper) {
        BlockPos start = new BlockPos(0, 1, 0);
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenExecute(() -> initializeStarterInventory(helper, start, new ItemStack(Items.STONE, 20)))
              //Wait a few seconds for it to pull some items out, re-enable a disabled path to make there be a shorter destination
              .thenExecuteAfter(5 * 20, () -> {
                  IConfigurable configurable = GameTestUtils.getCapability(helper, new BlockPos(1, 1, 2), Capabilities.CONFIGURABLE, Direction.WEST);
                  MekFakePlayer.withFakePlayer(helper.getLevel(), configurable::onSneakRightClick);
              })
              .thenExecuteAfter(9 * 20, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(0, 1, 5), 0, new ItemStack(Items.STONE, 9));
                  //Validate new destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 2), 0, new ItemStack(Items.STONE, 11));
                  //Validate start is also empty
                  GameTestUtils.validateContainerEmpty(helper, start, 0);
              })
        );
    }

    /**
     * Expected behavior: Nothing changes as colorless transporter stacks cannot enter a colored transporter.
     */
    @GameTest(template = BASE_PATH + "colorless_into_color", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void colorless_into_color(GameTestHelper helper) {
        //Note: We don't need to initialize the starting inventory as given nothing can transfer our NBT file already contains the stored items
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              //Wait a little to validate nothing is happening
              .thenExecuteAfter(2 * 20, () -> {
                  //Validate original destination has the starting amount
                  GameTestUtils.validateContainerHas(helper, new BlockPos(0, 1, 0), 0, new ItemStack(Items.STONE));
                  //Validate new destination is still empty
                  GameTestUtils.validateContainerEmpty(helper, new BlockPos(1, 1, 0), 0);
              })
        );
    }

    /**
     * Expected behavior: The stack moves to the further chest as the closer one has the color not matching.
     */
    @GameTest(template = BASE_PATH + "color_matches", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void color_matches(GameTestHelper helper) {
        BlockPos start = new BlockPos(0, 1, 0);
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenExecute(() -> initializeStarterInventory(helper, start, new ItemStack(Items.STONE, 2)))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(5 * 20, () -> {
                  //Validate colored destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 3), 0, new ItemStack(Items.STONE, 2));
                  //Validate wrong color destination is empty
                  GameTestUtils.validateContainerEmpty(helper, new BlockPos(2, 1, 2), 0);
                  //Validate start is also empty
                  GameTestUtils.validateContainerEmpty(helper, start, 0);
              })
        );
    }

    /**
     * Expected behavior: The stack moves to the closer destination as even though it is colorless and the further one has the original color, it is further away.
     */
    @GameTest(template = BASE_PATH + "color_not_priority", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void color_not_priority(GameTestHelper helper) {
        BlockPos start = new BlockPos(0, 1, 0);
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenExecute(() -> initializeStarterInventory(helper, start, new ItemStack(Items.STONE, 2)))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(5 * 20, () -> {
                  //Validate colored destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(1, 1, 2), 0, new ItemStack(Items.STONE, 2));
                  //Validate wrong color destination is empty
                  GameTestUtils.validateContainerEmpty(helper, new BlockPos(1, 1, 3), 0);
                  //Validate start is also empty
                  GameTestUtils.validateContainerEmpty(helper, start, 0);
              })
        );
    }

    /**
     * Expected behavior: Any newly pulled items will go to the destination that had its path upgraded, but any items that were already en-route will continue to the
     * destination they had already calculated.
     */
    @GameTest(template = BASE_PATH + "upgradeable", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void upgrade_further_path(GameTestHelper helper) {
        BlockPos start = new BlockPos(3, 1, 0);
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenExecute(() -> initializeStarterInventory(helper, start, new ItemStack(Items.STONE, 20)))
              //Wait a few seconds for it to pull some items out, and upgrade the transporter to the further destination
              .thenExecuteAfter(5 * 20, () -> applyAlloyUpgrade(helper, new BlockPos(9, 1, 0), AlloyTier.INFUSED))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(12 * 20, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 0), 0, new ItemStack(Items.STONE, 9));
                  //Validate further but now "closer" destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(8, 1, 0), 0, new ItemStack(Items.STONE, 11));
                  //Validate start is also empty
                  GameTestUtils.validateContainerEmpty(helper, start, 0);
              })
        );
    }

    /**
     * Expected behavior: Any newly pulled items will go to the destination that had its path upgraded, but any items that were already en-route will continue to the
     * destination they had already calculated as the new destination is slightly "closer".
     */
    @GameTest(template = BASE_PATH + "upgradeable", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void upgrade_further_overlapping(GameTestHelper helper) {
        BlockPos start = new BlockPos(3, 1, 0);
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenExecute(() -> initializeStarterInventory(helper, start, new ItemStack(Items.STONE, 20)))
              //Wait a few seconds for it to pull some items out, and upgrade the transporter to the further destination
              .thenExecuteAfter(5 * 20, () -> applyAlloyUpgrade(helper, new BlockPos(6, 1, 2), AlloyTier.INFUSED))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(12 * 20, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 0), 0, new ItemStack(Items.STONE, 9));
                  //Validate further but now "closer" destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(8, 1, 0), 0, new ItemStack(Items.STONE, 11));
                  //Validate start is also empty
                  GameTestUtils.validateContainerEmpty(helper, start, 0);
              })
        );
    }

    /**
     * Expected behavior: All items pre- and post-upgrade will go to the original destination.
     */
    @GameTest(template = BASE_PATH + "upgradeable", setupTicks = SETUP_TICKS, timeoutTicks = TIMEOUT_TICKS)
    public static void upgrade_existing(GameTestHelper helper) {
        BlockPos start = new BlockPos(3, 1, 0);
        GameTestUtils.succeedIfSequence(helper, sequence -> sequence
              .thenExecute(() -> initializeStarterInventory(helper, start, new ItemStack(Items.STONE, 20)))
              //Wait a few seconds for it to pull some items out, and upgrade the transporter to the further destination
              .thenExecuteAfter(5 * 20, () -> applyAlloyUpgrade(helper, new BlockPos(3, 1, 2), AlloyTier.INFUSED))
              //Wait a few seconds for transferring to happen then validate stuff
              .thenExecuteAfter(6 * 20, () -> {
                  //Validate original destination has expected count
                  GameTestUtils.validateContainerHas(helper, new BlockPos(2, 1, 0), 0, new ItemStack(Items.STONE, 20));
                  //Validate further is still empty
                  GameTestUtils.validateContainerEmpty(helper, new BlockPos(8, 1, 0), 0);
                  //Validate start is also empty
                  GameTestUtils.validateContainerEmpty(helper, start, 0);
              })
        );
    }

    private static void initializeStarterInventory(GameTestHelper helper, BlockPos relativePos, ItemStack stack) {
        IItemHandler handler = GameTestUtils.getCapability(helper, relativePos, ForgeCapabilities.ITEM_HANDLER);
        handler.insertItem(0, stack, false);
    }

    private static void applyAlloyUpgrade(GameTestHelper helper, BlockPos relativePos, AlloyTier tier) {
        IAlloyInteraction handler = GameTestUtils.getCapability(helper, relativePos, Capabilities.ALLOY_INTERACTION);
        ItemStack stack = new ItemStack(switch (tier) {
            case INFUSED -> MekanismItems.INFUSED_ALLOY;
            case REINFORCED -> MekanismItems.REINFORCED_ALLOY;
            case ATOMIC -> MekanismItems.ATOMIC_ALLOY;
        });
        MekFakePlayer.withFakePlayer(helper.getLevel(), player -> {
            handler.onAlloyInteraction(player, stack, tier);
            return null;
        });
    }
}