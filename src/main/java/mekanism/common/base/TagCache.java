package mekanism.common.base;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public final class TagCache {

    private TagCache() {
    }

    private static final Map<String, List<ItemStack>> blockTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> itemTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> modIDStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<Material, List<ItemStack>> materialStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<Block, List<String>> tileEntityTypeTagCache = new Object2ObjectOpenHashMap<>();

    public static void resetVanillaTagCaches() {
        blockTagStacks.clear();
        itemTagStacks.clear();
    }

    public static void resetCustomTagCaches() {
        tileEntityTypeTagCache.clear();
    }

    public static List<String> getItemTags(@Nonnull ItemStack check) {
        return getTagsAsStrings(check.getItem().getTags());
    }

    public static List<String> getTileEntityTypeTags(@Nonnull Block block) {
        if (tileEntityTypeTagCache.containsKey(block)) {
            return tileEntityTypeTagCache.get(block);
        }
        List<String> tagsAsString;
        if (block instanceof IHasTileEntity) {
            //If it is one of our blocks, short circuit and just lookup the tile's type directly
            tagsAsString = getTagsAsStrings(((IHasTileEntity<?>) block).getTileType().getTags());
        } else if (block.hasTileEntity(block.getDefaultState())) {
            //Otherwise check if the block has a tile entity and if it does, gather all the tile types the block
            // is valid for as we don't want to risk initializing a tile for another mod as it may have side effects
            // that we don't know about and don't handle properly
            Set<ResourceLocation> tileEntityTags = new HashSet<>();
            for (TileEntityType<?> tileEntityType : ForgeRegistries.TILE_ENTITIES) {
                if (tileEntityType.isValidBlock(block)) {
                    tileEntityTags.addAll(tileEntityType.getTags());
                }
            }
            tagsAsString = getTagsAsStrings(tileEntityTags);
        } else {
            tagsAsString = Collections.emptyList();
        }
        tileEntityTypeTagCache.put(block, tagsAsString);
        return tagsAsString;
    }

    public static List<String> getTagsAsStrings(@Nonnull Set<ResourceLocation> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<String> asStrings = ImmutableList.builder();
        for (ResourceLocation tag : tags) {
            asStrings.add(tag.toString());
        }
        return asStrings.build();
    }

    public static List<ItemStack> getItemTagStacks(@Nonnull String oreName) {
        if (itemTagStacks.containsKey(oreName)) {
            return itemTagStacks.get(oreName);
        }
        ITagCollection<Item> tagCollection = ItemTags.getCollection();
        List<ResourceLocation> keys = tagCollection.getRegisteredTags().stream().filter(rl -> WildcardMatcher.matches(oreName, rl.toString())).collect(Collectors.toList());
        Set<Item> items = new HashSet<>();
        for (ResourceLocation key : keys) {
            ITag<Item> itemTag = tagCollection.get(key);
            if (itemTag != null) {
                items.addAll(itemTag.getAllElements());
            }
        }
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        itemTagStacks.put(oreName, stacks);
        return stacks;
    }

    public static List<ItemStack> getBlockTagStacks(@Nonnull String oreName) {
        if (blockTagStacks.containsKey(oreName)) {
            return blockTagStacks.get(oreName);
        }
        ITagCollection<Block> tagCollection = BlockTags.getCollection();
        List<ResourceLocation> keys = tagCollection.getRegisteredTags().stream().filter(rl -> WildcardMatcher.matches(oreName, rl.toString())).collect(Collectors.toList());
        Set<Block> blocks = new HashSet<>();
        for (ResourceLocation key : keys) {
            ITag<Block> blockTag = tagCollection.get(key);
            if (blockTag != null) {
                blocks.addAll(blockTag.getAllElements());
            }
        }
        List<ItemStack> stacks = blocks.stream().map(ItemStack::new).collect(Collectors.toList());
        blockTagStacks.put(oreName, stacks);
        return stacks;
    }

    public static List<ItemStack> getModIDStacks(@Nonnull String modName, boolean forceBlock) {
        if (modIDStacks.containsKey(modName)) {
            return modIDStacks.get(modName);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!forceBlock || item instanceof BlockItem) {
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should use getRenderType() with a dummy BlockState
                if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BlockBounding) {
                    continue;
                }
                //Note: We get the modid based on the stack so that if there is a mod that has a different modid for an item
                // that isn't based on NBT it can properly change the modid (this is unlikely to happen, but you never know)
                ItemStack stack = new ItemStack(item);
                if (WildcardMatcher.matches(modName, MekanismUtils.getModId(stack))) {
                    stacks.add(stack);
                }
            }
        }
        modIDStacks.put(modName, stacks);
        return stacks;
    }

    public static List<ItemStack> getMaterialStacks(@Nonnull ItemStack stack) {
        return getMaterialStacks(Block.getBlockFromItem(stack.getItem()).getDefaultState().getMaterial());
    }

    public static List<ItemStack> getMaterialStacks(@Nonnull Material material) {
        if (materialStacks.containsKey(material)) {
            return materialStacks.get(material);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should use getRenderType() with a dummy BlockState
                //noinspection ConstantConditions getBlock is nonnull, but if something "goes wrong" it returns null, just skip it
                if (block == null || block instanceof BlockBounding) {
                    continue;
                }
                if (block.getDefaultState().getMaterial() == material) {
                    stacks.add(new ItemStack(item));
                }
            }
        }
        materialStacks.put(material, stacks);
        return stacks;
    }
}