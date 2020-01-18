package mekanism.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

//TODO: Rewrite this entire class to be more of a tag helper class than a caching class
public final class OreDictCache {

    public static Map<String, List<ItemStack>> blockTagStacks = new HashMap<>();
    public static Map<String, List<ItemStack>> itemTagStacks = new HashMap<>();
    public static Map<String, List<ItemStack>> modIDStacks = new HashMap<>();

    public static List<String> getOreDictName(ItemStack check) {
        //TODO: Switch to other methods
        return new ArrayList<>();
    }

    public static List<ItemStack> getItemTagStacks(String oreName) {
        if (itemTagStacks.get(oreName) != null) {
            return itemTagStacks.get(oreName);
        }
        List<ResourceLocation> keys = new ArrayList<>();
        TagCollection<Item> tagCollection = ItemTags.getCollection();
        Collection<ResourceLocation> registeredTags = tagCollection.getRegisteredTags();
        for (ResourceLocation rl : registeredTags) {
            String rlAsString = rl.toString();
            if (oreName.equals(rlAsString) || oreName.equals("*")) {
                keys.add(rl);
            } else if (oreName.endsWith("*") && !oreName.startsWith("*")) {
                if (rlAsString.startsWith(oreName.substring(0, oreName.length() - 1))) {
                    keys.add(rl);
                }
            } else if (oreName.startsWith("*") && !oreName.endsWith("*")) {
                if (rlAsString.endsWith(oreName.substring(1))) {
                    keys.add(rl);
                }
            } else if (oreName.startsWith("*") && oreName.endsWith("*")) {
                if (rlAsString.contains(oreName.substring(1, oreName.length() - 1))) {
                    keys.add(rl);
                }
            }
        }
        List<Item> items = new ArrayList<>();
        for (ResourceLocation key : keys) {
            Tag<Item> itemTag = tagCollection.get(key);
            if (itemTag != null) {
                for (Item item : itemTag.getAllElements()) {
                    if (!items.contains(item)) {
                        items.add(item);
                    }
                }
            }
        }
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        itemTagStacks.put(oreName, stacks);
        return stacks;
    }

    public static List<ItemStack> getBlockTagStacks(String oreName) {
        if (blockTagStacks.get(oreName) != null) {
            return blockTagStacks.get(oreName);
        }

        List<ResourceLocation> keys = new ArrayList<>();
        TagCollection<Block> tagCollection = BlockTags.getCollection();
        Collection<ResourceLocation> registeredTags = tagCollection.getRegisteredTags();
        for (ResourceLocation rl : registeredTags) {
            String rlAsString = rl.toString();
            if (oreName.equals(rlAsString) || oreName.equals("*")) {
                keys.add(rl);
            } else if (oreName.endsWith("*") && !oreName.startsWith("*")) {
                if (rlAsString.startsWith(oreName.substring(0, oreName.length() - 1))) {
                    keys.add(rl);
                }
            } else if (oreName.startsWith("*") && !oreName.endsWith("*")) {
                if (rlAsString.endsWith(oreName.substring(1))) {
                    keys.add(rl);
                }
            } else if (oreName.startsWith("*") && oreName.endsWith("*")) {
                if (rlAsString.contains(oreName.substring(1, oreName.length() - 1))) {
                    keys.add(rl);
                }
            }
        }

        List<Block> blocks = new ArrayList<>();
        for (ResourceLocation key : keys) {
            Tag<Block> blockTag = tagCollection.get(key);
            if (blockTag != null) {
                for (Block block : blockTag.getAllElements()) {
                    if (!blocks.contains(block)) {
                        blocks.add(block);
                    }
                }
            }
        }
        List<ItemStack> stacks = blocks.stream().map(ItemStack::new).collect(Collectors.toList());
        blockTagStacks.put(oreName, stacks);
        return stacks;
    }

    public static List<ItemStack> getModIDStacks(String modName, boolean forceBlock) {
        if (modIDStacks.get(modName) != null) {
            return modIDStacks.get(modName);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!forceBlock || item instanceof BlockItem) {
                String id = item.getRegistryName().getNamespace();
                if (modName.equals(id) || modName.equals("*")) {
                    stacks.add(new ItemStack(item));
                } else if (modName.endsWith("*") && !modName.startsWith("*")) {
                    if (id.startsWith(modName.substring(0, modName.length() - 1))) {
                        stacks.add(new ItemStack(item));
                    }
                } else if (modName.startsWith("*") && !modName.endsWith("*")) {
                    if (id.endsWith(modName.substring(1))) {
                        stacks.add(new ItemStack(item));
                    }
                } else if (modName.startsWith("*") && modName.endsWith("*")) {
                    if (id.contains(modName.substring(1, modName.length() - 1))) {
                        stacks.add(new ItemStack(item));
                    }
                }
            }
        }
        modIDStacks.put(modName, stacks);
        return stacks;
    }
}