package mekanism.common.tests;

import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.tests.helpers.MekGameTestHelper;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "flamethrower")
public class FlamethrowerTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Heat mode smelts a dropped stack after its grace period.")
    public static void smeltDroppedItems(final MekGameTestHelper helper) {
        helper.succeedIf(() -> {
            ItemEntity item = fireAtItem(helper, FlamethrowerMode.HEAT, true);
            helper.assertTrue(item.getItem().is(Items.STONE) && item.getItem().count() == 3, "The dropped stack should be smelted");
            helper.assertTrue(item.tickCount == 0, "Smelting should reset the item's grace period");
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Heat mode leaves freshly dropped items unchanged.")
    public static void preserveFreshItems(final MekGameTestHelper helper) {
        helper.succeedIf(() -> {
            ItemEntity item = fireAtItem(helper, FlamethrowerMode.HEAT, false);
            helper.assertTrue(!item.isRemoved() && item.getItem().is(Items.COBBLESTONE), "Freshly dropped items should not be smelted or destroyed");
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Combat mode destroys dropped items when enabled.")
    public static void burnDroppedItems(final MekGameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean destroyItems = MekanismConfig.gear.flamethrowerDestroyItems.get();
            try {
                MekanismConfig.gear.flamethrowerDestroyItems.set(true);
                helper.assertTrue(fireAtItem(helper, FlamethrowerMode.COMBAT, true).isRemoved(), "Combat mode should destroy dropped items");
            } finally {
                MekanismConfig.gear.flamethrowerDestroyItems.set(destroyItems);
            }
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Combat mode respects the dropped-item protection setting.")
    public static void preserveProtectedItems(final MekGameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean destroyItems = MekanismConfig.gear.flamethrowerDestroyItems.get();
            try {
                MekanismConfig.gear.flamethrowerDestroyItems.set(false);
                helper.assertTrue(!fireAtItem(helper, FlamethrowerMode.COMBAT, true).isRemoved(), "Protected dropped items should survive combat mode");
            } finally {
                MekanismConfig.gear.flamethrowerDestroyItems.set(destroyItems);
            }
        });
    }

    private static ItemEntity fireAtItem(MekGameTestHelper helper, FlamethrowerMode mode, boolean aged) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ItemEntity item = new ItemEntity(helper.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(Items.COBBLESTONE, 3));
        item.setNoGravity(true);
        item.setDeltaMovement(0, 0, 0);
        if (aged) {
            item.tickCount = 5 * SharedConstants.TICKS_PER_SECOND + 1;
        }
        helper.assertTrue(helper.getLevel().addFreshEntity(item), "The dropped item should be spawned");

        EntityFlame flame = new EntityFlame(MekanismEntityTypes.FLAME.get(), helper.getLevel());
        flame.setPos(item.position().add(-1, 0.1, 0));
        flame.setDeltaMovement(2, 0, 0);
        flame.setData(MekanismAttachmentTypes.FLAMETHROWER_MODE, mode);
        helper.assertTrue(helper.getLevel().addFreshEntity(flame), "The flame should be spawned");
        flame.tick();
        helper.assertTrue(flame.isRemoved(), "The flame should collide with the dropped item");
        return item;
    }
}
