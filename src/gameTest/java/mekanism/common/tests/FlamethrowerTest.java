package mekanism.common.tests;

import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.tests.helpers.FlamethrowerTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "flamethrower")
public class FlamethrowerTest {

    @GameTest
    @EmptyTemplate("5x5x5")
    @TestHolder(description = "Heat mode smelts a dropped stack after its grace period.")
    public static void smeltDroppedItems(final FlamethrowerTestHelper helper) {
        helper.succeedIf(() -> {
            ItemEntity item = helper.fireAtItem(FlamethrowerMode.HEAT, true, null);
            helper.assertTrue(item.getItem().is(Items.STONE) && item.getItem().count() == 3, "The dropped stack should be smelted");
            helper.assertTrue(item.tickCount == 0, "Smelting should reset the item's grace period");
        });
    }

    @GameTest
    @EmptyTemplate("5x5x5")
    @TestHolder(description = "Heat mode leaves freshly dropped items unchanged.")
    public static void preserveFreshItems(final FlamethrowerTestHelper helper) {
        helper.succeedIf(() -> {
            ItemEntity item = helper.fireAtItem(FlamethrowerMode.HEAT, false, null);
            helper.assertTrue(!item.isRemoved() && item.getItem().is(Items.COBBLESTONE), "Freshly dropped items should not be smelted or destroyed");
        });
    }

    @GameTest
    @EmptyTemplate("5x5x5")
    @TestHolder(description = "Combat mode destroys dropped items when enabled.")
    public static void burnDroppedItems(final FlamethrowerTestHelper helper) {
        helper.succeedIf(() -> {
            helper.assertTrue(helper.fireAtItem(FlamethrowerMode.COMBAT, true, true).isRemoved(), "Combat mode should destroy dropped items");
        });
    }

    @GameTest
    @EmptyTemplate("5x5x5")
    @TestHolder(description = "Combat mode respects the dropped-item protection setting.")
    public static void preserveProtectedItems(final FlamethrowerTestHelper helper) {
        helper.succeedIf(() -> {
            helper.assertTrue(!helper.fireAtItem(FlamethrowerMode.COMBAT, true, false).isRemoved(), "Protected dropped items should survive combat mode");
        });
    }
}
