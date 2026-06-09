package mekanism.common.lib.inventory;

import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.transfer.item.ItemResource;

@FunctionalInterface
public interface Finder {

    boolean test(ItemResource itemType);

    Finder ANY = _ -> true;
    Finder NONE = _ -> false;

    static boolean item(ItemResource itemType, ItemResource toCheck) {
        return !itemType.isEmpty() && toCheck.is(itemType.value());
    }

    //TODO - 26.1 instance of WildcardMatcher/predicate param (stored on filter upon tag change)
    static boolean tag(String tagName, ItemResource toCheck) {
        return !toCheck.isEmpty() && toCheck.tags().anyMatch(tag -> WildcardMatcher.matches(tagName, tag));
    }

    static boolean modID(String modID, HolderLookup.Provider registries, ItemResource toCheck) {
        return !toCheck.isEmpty() && WildcardMatcher.matches(modID, MekanismUtils.getModId(registries, toCheck.toStack()));
    }
}