package mekanism.common.lib.inventory;

import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Finder {

    boolean test(ItemStack stack);

    Finder ANY = stack -> true;
    Finder NONE = stack -> false;

    static Finder item(Item itemType) {
        if (itemType == Items.AIR) {
            return NONE;
        }
        return stack -> stack.is(itemType);
    }

    static boolean item(Item itemType, ItemStack toCheck) {
        if (itemType == Items.AIR) {
            return false;
        }
        return toCheck.is(itemType);
    }

    static boolean item(ItemStack itemType, ItemStack toCheck) {
        return item(itemType.getItem(), toCheck);
    }

    static boolean strict(ItemStack itemType, ItemStack toCheck) {
        return ItemStack.isSameItemSameComponents(itemType, toCheck);
    }

    //todo 26.1 instance of WildcardMatcher/predicate param (stored on filter upon tag change)
    static boolean tag(String tagName, ItemStack toCheck) {
        return !toCheck.isEmpty() && toCheck.tags().anyMatch(tag -> WildcardMatcher.matches(tagName, tag));
    }

    static boolean modID(String modID, @NotNull HolderLookup.Provider registries, ItemStack toCheck) {
        return !toCheck.isEmpty() && WildcardMatcher.matches(modID, MekanismUtils.getModId(registries, toCheck));
    }
}