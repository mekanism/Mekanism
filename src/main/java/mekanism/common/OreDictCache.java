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

    public static Map<Item, Collection<ResourceLocation>> cachedItemKeys = new HashMap<>();
    public static Map<Block, Collection<ResourceLocation>> cachedBlockKeys = new HashMap<>();
    public static Map<String, List<ItemStack>> oreDictStacks = new HashMap<>();
    public static Map<String, List<ItemStack>> modIDStacks = new HashMap<>();

    public static List<String> getOreDictName(ItemStack check) {
        //TODO: Switch to other methods
        return new ArrayList<>();
    }

    public static Collection<ResourceLocation> getTagsForItem(ItemStack check) {
        if (check.isEmpty()) {
            return new ArrayList<>();
        }
        Item info = check.getItem();
        Collection<ResourceLocation> cached = cachedItemKeys.get(info);
        if (cached != null) {
            return cached;
        }
        Collection<ResourceLocation> owningTags = ItemTags.getCollection().getOwningTags(check.getItem());
        cachedItemKeys.put(info, owningTags);
        return owningTags;
    }

    public static Collection<ResourceLocation> getTagsForBlock(Block check) {
        Collection<ResourceLocation> cached = cachedBlockKeys.get(check);
        if (cached != null) {
            return cached;
        }
        Collection<ResourceLocation> owningTags = BlockTags.getCollection().getOwningTags(check);
        cachedBlockKeys.put(check, owningTags);
        return owningTags;
    }

    //TODO: Decide if we should have something to check the
    public static List<ItemStack> getItemTagStacks(String oreName, boolean forceBlock) {
        if (oreDictStacks.get(oreName) != null) {
            return oreDictStacks.get(oreName);
        }

        List<ResourceLocation> keys = new ArrayList<>();
        TagCollection<Item> tagCollection = ItemTags.getCollection();
        Collection<ResourceLocation> registeredTags = tagCollection.getRegisteredTags();
        for (ResourceLocation rl : registeredTags) {
            String key = rl.getPath();
            if (oreName.equals(key) || oreName.equals("*")) {
                keys.add(rl);
            } else if (oreName.endsWith("*") && !oreName.startsWith("*")) {
                if (key.startsWith(oreName.substring(0, oreName.length() - 1))) {
                    keys.add(rl);
                }
            } else if (oreName.startsWith("*") && !oreName.endsWith("*")) {
                if (key.endsWith(oreName.substring(1))) {
                    keys.add(rl);
                }
            } else if (oreName.startsWith("*") && oreName.endsWith("*")) {
                if (key.contains(oreName.substring(1, oreName.length() - 1))) {
                    keys.add(rl);
                }
            }
        }

        List<Item> items = new ArrayList<>();
        for (ResourceLocation key : keys) {
            Tag<Item> itemTag = tagCollection.get(key);
            if (itemTag != null) {
                for (Item item : itemTag.getAllElements()) {
                    if (!items.contains(item) && (!forceBlock || item instanceof BlockItem)) {
                        items.add(item);
                    }
                }
            }
        }
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        oreDictStacks.put(oreName, stacks);
        return stacks;
    }

    public static List<ItemStack> getOreDictStacks(String oreName, boolean forceBlock) {
        //TODO: Replace with others, we also need to make this sometimes go off of the block's tags not the item tags
        return getItemTagStacks(oreName, forceBlock);
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
                    if(id.contains(modName.substring(1, modName.length() - 1))) {
                        stacks.add(new ItemStack(item));
                    }
                }
            }
        }
        modIDStacks.put(modName, stacks);
        return stacks;
    }
}