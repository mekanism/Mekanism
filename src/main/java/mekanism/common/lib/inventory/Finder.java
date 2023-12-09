package mekanism.common.lib.inventory;

import java.util.function.Predicate;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemHandlerHelper;

@FunctionalInterface
public interface Finder extends Predicate<ItemStack> {

    Finder ANY = stack -> true;

    static Finder item(Item itemType) {
        return stack -> itemType != Items.AIR && itemType == stack.getItem();
    }

    static Finder item(ItemStack itemType) {
        return item(itemType.getItem());
    }

    static Finder strict(ItemStack itemType) {
        return stack -> ItemHandlerHelper.canItemStacksStack(itemType, stack);
    }

    static Finder tag(String tagName) {
        return stack -> !stack.isEmpty() && stack.getTags().anyMatch(tag -> WildcardMatcher.matches(tagName, tag));
    }

    static Finder modID(String modID) {
        return stack -> !stack.isEmpty() && WildcardMatcher.matches(modID, MekanismUtils.getModId(stack));
    }
}