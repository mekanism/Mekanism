package mekanism.common.tests;

import java.util.UUID;
import java.util.function.Function;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.tests.helpers.MekGameTestHelper;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "hasheditem")
public class HashedItemTest {
    private static Runnable getTest(MekGameTestHelper helper, Function<ItemStack, HashedItem> hashConstructor1, Function<ItemStack, HashedItem> hashConstructor2, String testType, ItemStack stack1, ItemStack stack2, boolean sameExpected) {
        return ()->{
            HashedItem hashed = hashConstructor1.apply(stack1);
            HashedItem hashed2 = hashConstructor2.apply(stack2);
            helper.assertValueEqual(hashed.hashCode() == hashed2.hashCode(), sameExpected, "Hashcodes for " + testType);
            helper.assertValueEqual(hashed.equals(hashed2), sameExpected, "Equals for " + testType);
            helper.assertValueEqual(ItemStack.isSameItemSameComponents(stack1, hashed.createStack(stack1.getCount())), true, "Reconstituted stack1");
            helper.assertValueEqual(ItemStack.isSameItemSameComponents(stack2, hashed2.createStack(stack2.getCount())), true, "Reconstituted stack2");
        };
    }

    private static void doSequence(MekGameTestHelper helper, Function<ItemStack, HashedItem> hashConstructor1, Function<ItemStack, HashedItem> hashConstructor2, boolean allShouldFail) {
        helper.startSequence()
              .thenExecute(getTest(helper, hashConstructor1, hashConstructor2, "identical item", new ItemStack(Items.STICK, 1), new ItemStack(Items.STICK, 1), !allShouldFail))
              .thenExecute(getTest(helper, hashConstructor1, hashConstructor2, "identical item, one with components", new ItemStack(Items.STICK, 1), new ItemStack(Items.STICK.builtInRegistryHolder(), 1, DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 16).build()), false))
              .thenExecute(getTest(helper, hashConstructor1, hashConstructor2, "identical item, 1st with components", new ItemStack(Items.STICK.builtInRegistryHolder(), 1, DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 16).build()), new ItemStack(Items.STICK, 1), false))
              .thenExecute(getTest(helper, hashConstructor1, hashConstructor2, "identical item with count", new ItemStack(Items.STICK, 50), new ItemStack(Items.STICK, 50), !allShouldFail))
              .thenExecute(getTest(helper, hashConstructor1, hashConstructor2, "identical item with count", new ItemStack(Items.STICK, 1), new ItemStack(Items.STICK, 50), !allShouldFail))
              .thenExecute(getTest(helper, hashConstructor1, hashConstructor2, "different item", new ItemStack(Items.PORKCHOP, 1), new ItemStack(Items.BAKED_POTATO, 1), false))
              .thenSucceed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "HashedItem.raw")
    public static void validateRaw(final MekGameTestHelper helper) {
        doSequence(helper, HashedItem::raw, HashedItem::raw, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "HashedItem.create")
    public static void validateRegular(final MekGameTestHelper helper) {
        doSequence(helper, HashedItem::create, HashedItem::create, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "HashedItem.create and HashedItem.raw equivalent")
    public static void validateRawAndRegular(final MekGameTestHelper helper) {
        doSequence(helper, HashedItem::raw, HashedItem::create, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "UUID aware equivalent")
    public static void validateUUID(final MekGameTestHelper helper) {
        UUID randomUUID = UUID.randomUUID();
        Function<ItemStack, HashedItem> uuidSupplier = s -> new UUIDAwareHashedItem(s, randomUUID);
        doSequence(helper, uuidSupplier, uuidSupplier, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "UUID aware after unwrapping. Tests copying hashed items")
    public static void validateUUIDUnwrapped(final MekGameTestHelper helper) {
        UUID randomUUID = UUID.randomUUID();
        Function<ItemStack, HashedItem> uuidSupplier = s -> new UUIDAwareHashedItem(s, randomUUID).asRawHashedItem();
        doSequence(helper, uuidSupplier, uuidSupplier, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "UUID aware but different UUIDs")
    public static void validateUUIDDiffer(final MekGameTestHelper helper) {
        Function<ItemStack, HashedItem> uuidSupplier = s -> new UUIDAwareHashedItem(s, UUID.randomUUID());
        doSequence(helper, uuidSupplier, uuidSupplier, true);
    }
}
