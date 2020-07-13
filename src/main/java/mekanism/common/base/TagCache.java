package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.common.block.BlockBounding;
import mekanism.common.lib.WildcardMatcher;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public final class TagCache {

    private static final Map<String, List<ItemStack>> blockTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> itemTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> modIDStacks = new Object2ObjectOpenHashMap<>();

    public static void resetTagCaches() {
        blockTagStacks.clear();
        itemTagStacks.clear();
    }

    public static List<String> getItemTags(ItemStack check) {
        if (check == null) {
            return new ArrayList<>();
        }
        return check.getItem().getTags().stream().map(ResourceLocation::toString).collect(Collectors.toList());
    }

    public static List<ItemStack> getItemTagStacks(String oreName) {
        if (itemTagStacks.get(oreName) != null) {
            return itemTagStacks.get(oreName);
        }
        TagCollection<Item> tagCollection = TagCollectionManager.func_232928_e_().func_232925_b_();
        List<ResourceLocation> keys = tagCollection.getRegisteredTags().stream().filter(rl -> WildcardMatcher.matches(oreName, rl.toString())).collect(Collectors.toList());
        Set<Item> items = new HashSet<>();
        for (ResourceLocation key : keys) {
            ITag<Item> itemTag = tagCollection.get(key);
            if (itemTag != null) {
                items.addAll(itemTag.func_230236_b_());
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
        TagCollection<Block> tagCollection = TagCollectionManager.func_232928_e_().func_232923_a_();
        List<ResourceLocation> keys = tagCollection.getRegisteredTags().stream().filter(rl -> WildcardMatcher.matches(oreName, rl.toString())).collect(Collectors.toList());
        Set<Block> blocks = new HashSet<>();
        for (ResourceLocation key : keys) {
            ITag<Block> blockTag = tagCollection.get(key);
            if (blockTag != null) {
                blocks.addAll(blockTag.func_230236_b_());
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
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should use getRenderType() with a dummy BlockState
                if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BlockBounding) {
                    continue;
                }
                if (WildcardMatcher.matches(modName, item.getRegistryName().getNamespace())) {
                    stacks.add(new ItemStack(item));
                }
            }
        }
        modIDStacks.put(modName, stacks);
        return stacks;
    }
}