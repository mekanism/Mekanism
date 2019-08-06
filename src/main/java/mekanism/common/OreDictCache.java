package mekanism.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.common.util.ItemRegistryUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictCache {

    public static Map<Item, List<String>> cachedKeys = new HashMap<>();
    public static Map<String, List<ItemStack>> oreDictStacks = new HashMap<>();
    public static Map<String, List<ItemStack>> modIDStacks = new HashMap<>();

    public static List<String> getOreDictName(ItemStack check) {
        if (check.isEmpty()) {
            return new ArrayList<>();
        }
        Item info = check.getItem();
        List<String> cached = cachedKeys.get(info);
        if (cached != null) {
            return cached;
        }

        int[] idsFound = OreDictionary.getOreIDs(check);
        List<String> ret = new ArrayList<>();
        for (Integer id : idsFound) {
            ret.add(OreDictionary.getOreName(id));
        }
        cachedKeys.put(info, ret);
        return ret;
    }

    public static List<ItemStack> getOreDictStacks(String oreName, boolean forceBlock) {
        if (oreDictStacks.get(oreName) != null) {
            return oreDictStacks.get(oreName);
        }

        List<String> keys = new ArrayList<>();
        for (String s : OreDictionary.getOreNames()) {
            if (s == null) {
                continue;
            }
            if (oreName.equals(s) || oreName.equals("*")) {
                keys.add(s);
            } else if (oreName.endsWith("*") && !oreName.startsWith("*")) {
                if (s.startsWith(oreName.substring(0, oreName.length() - 1))) {
                    keys.add(s);
                }
            } else if (oreName.startsWith("*") && !oreName.endsWith("*")) {
                if (s.endsWith(oreName.substring(1))) {
                    keys.add(s);
                }
            } else if (oreName.startsWith("*") && oreName.endsWith("*")) {
                if (s.contains(oreName.substring(1, oreName.length() - 1))) {
                    keys.add(s);
                }
            }
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (String key : keys) {
            for (ItemStack stack : OreDictionary.getOres(key, false)) {
                ItemStack toAdd = stack.copy();
                if (!stacks.contains(stack) && (!forceBlock || toAdd.getItem() instanceof BlockItem)) {
                    stacks.add(stack.copy());
                }
            }
        }
        oreDictStacks.put(oreName, stacks);
        return stacks;
    }

    public static List<ItemStack> getModIDStacks(String modName, boolean forceBlock) {
        if (modIDStacks.get(modName) != null) {
            return modIDStacks.get(modName);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (String key : OreDictionary.getOreNames()) {
            for (ItemStack stack : OreDictionary.getOres(key, false)) {
                ItemStack toAdd = stack.copy();
                String s = ItemRegistryUtils.getMod(toAdd);
                if (!stacks.contains(stack) && toAdd.getItem() instanceof BlockItem) {
                    if (modName.equals(s) || modName.equals("*")) {
                        stacks.add(stack.copy());
                    } else if (modName.endsWith("*") && !modName.startsWith("*")) {
                        if (s.startsWith(modName.substring(0, modName.length() - 1))) {
                            stacks.add(stack.copy());
                        }
                    } else if (modName.startsWith("*") && !modName.endsWith("*")) {
                        if (s.endsWith(modName.substring(1))) {
                            stacks.add(stack.copy());
                        }
                    } else if (modName.startsWith("*") && modName.endsWith("*")) {
                        if (s.contains(modName.substring(1, modName.length() - 1))) {
                            stacks.add(stack.copy());
                        }
                    }
                }
            }
        }
        modIDStacks.put(modName, stacks);
        return stacks;
    }
}