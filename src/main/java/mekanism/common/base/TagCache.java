package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

//TODO: Try to come up with a better name for this class given it also handles things like materials, and modids
public final class TagCache {

    private TagCache() {
    }

    private static final Map<String, MatchingStacks> blockTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> itemTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> itemModIDStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, MatchingStacks> blockModIDStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<Material, List<ItemStack>> materialStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<Block, List<String>> tileEntityTypeTagCache = new Object2ObjectOpenHashMap<>();

    private static final Object2BooleanMap<String> blockTagBlacklistedElements = new Object2BooleanOpenHashMap<>();
    private static final Object2BooleanMap<String> modIDBlacklistedElements = new Object2BooleanOpenHashMap<>();
    private static final Object2BooleanMap<Material> materialBlacklistedElements = new Object2BooleanOpenHashMap<>();

    public static void resetTagCaches() {
        blockTagStacks.clear();
        itemTagStacks.clear();
        tileEntityTypeTagCache.clear();
        //These maps have the boolean value be based on if an element is in a given tag
        blockTagBlacklistedElements.clear();
        modIDBlacklistedElements.clear();
        materialBlacklistedElements.clear();
    }

    public static List<String> getItemTags(@Nonnull ItemStack check) {
        return getTagsAsStrings(check.getTags());
    }

    public static List<String> getTileEntityTypeTags(@Nonnull Block block) {
        if (tileEntityTypeTagCache.containsKey(block)) {
            return tileEntityTypeTagCache.get(block);
        }
        List<String> tagsAsString;
        if (block instanceof IHasTileEntity<?> hasTileEntity) {
            //If it is one of our blocks, short circuit and just lookup the tile's type directly
            tagsAsString = getTagsAsStrings(TagUtils.tagsStream(ForgeRegistries.BLOCK_ENTITIES, hasTileEntity.getTileType().get()));
        } else {
            BlockState state = block.defaultBlockState();
            if (state.hasBlockEntity()) {
                //Otherwise, check if the block has a tile entity and if it does, gather all the tile types the block
                // is valid for as we don't want to risk initializing a tile for another mod as it may have side effects
                // that we don't know about and don't handle properly
                ITagManager<BlockEntityType<?>> manager = TagUtils.manager(ForgeRegistries.BLOCK_ENTITIES);
                tagsAsString = getTagsAsStrings(StreamSupport.stream(ForgeRegistries.BLOCK_ENTITIES.spliterator(), false)
                      .filter(type -> type.isValid(state))
                      .flatMap(type -> TagUtils.tagsStream(manager, type))
                      .distinct()
                );
            } else {
                tagsAsString = Collections.emptyList();
            }
        }
        tileEntityTypeTagCache.put(block, tagsAsString);
        return tagsAsString;
    }

    public static <TYPE> List<String> getTagsAsStrings(@Nonnull Stream<TagKey<TYPE>> tags) {
        return tags.map(tag -> tag.location().toString()).toList();
    }

    public static List<ItemStack> getItemTagStacks(@Nonnull String tagName) {
        if (itemTagStacks.containsKey(tagName)) {
            return itemTagStacks.get(tagName);
        }
        Set<Item> items = collectTagStacks(TagUtils.manager(ForgeRegistries.ITEMS), tagName, item -> item != MekanismBlocks.BOUNDING_BLOCK.asItem());
        List<ItemStack> stacks = items.stream().map(ItemStack::new).filter(stack -> !stack.isEmpty()).toList();
        itemTagStacks.put(tagName, stacks);
        return stacks;
    }

    public static MatchingStacks getBlockTagStacks(@Nonnull String tagName) {
        if (blockTagStacks.containsKey(tagName)) {
            return blockTagStacks.get(tagName);
        }
        Set<Block> blocks = collectTagStacks(TagUtils.manager(ForgeRegistries.BLOCKS), tagName, block -> block != MekanismBlocks.BOUNDING_BLOCK.getBlock());
        return getMatching(blockTagStacks, blocks, tagName);
    }

    private static <TYPE extends IForgeRegistryEntry<TYPE> & ItemLike> Set<TYPE> collectTagStacks(ITagManager<TYPE> tagManager, String tagName, Predicate<TYPE> validElement) {
        return tagManager.stream().filter(tag -> WildcardMatcher.matches(tagName, tag.getKey()))
              .flatMap(ITag::stream)
              .filter(validElement)
              .collect(Collectors.toSet());
    }

    private static MatchingStacks getMatching(Map<String, MatchingStacks> cache, Set<Block> blocks, String name) {
        if (blocks.isEmpty()) {
            return MatchingStacks.NONE;
        }
        //Filter out any stacks that are empty such as if we are mining a block that doesn't have a direct item representation
        MatchingStacks matchingStacks = new MatchingStacks(true, blocks.stream().map(ItemStack::new).filter(stack -> !stack.isEmpty()).toList());
        cache.put(name, matchingStacks);
        return matchingStacks;
    }

    public static List<ItemStack> getItemModIDStacks(@Nonnull String modName) {
        if (itemModIDStacks.containsKey(modName)) {
            return itemModIDStacks.get(modName);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            //Ugly check to make sure we don't include our bounding block in render list. Eventually this should maybe just use getRenderShape() with a dummy BlockState
            if (item != MekanismBlocks.BOUNDING_BLOCK.asItem()) {
                //Note: We get the modid based on the stack so that if there is a mod that has a different modid for an item
                // that isn't based on NBT it can properly change the modid (this is unlikely to happen, but you never know)
                ItemStack stack = new ItemStack(item);
                if (!stack.isEmpty() && WildcardMatcher.matches(modName, MekanismUtils.getModId(stack))) {
                    stacks.add(stack);
                }
            }
        }
        itemModIDStacks.put(modName, stacks);
        return stacks;
    }

    public static MatchingStacks getBlockModIDStacks(@Nonnull String modName) {
        if (blockModIDStacks.containsKey(modName)) {
            return blockModIDStacks.get(modName);
        }
        Set<Block> blocks = new HashSet<>();
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            //Ugly check to make sure we don't include our bounding block in render list. Eventually this should maybe just use getRenderShape() with a dummy BlockState
            if (block != MekanismBlocks.BOUNDING_BLOCK.getBlock() && WildcardMatcher.matches(modName, block.getRegistryName().getNamespace())) {
                blocks.add(block);
            }
        }
        return getMatching(blockModIDStacks, blocks, modName);
    }

    public static List<ItemStack> getMaterialStacks(@Nonnull ItemStack stack) {
        return getMaterialStacks(Block.byItem(stack.getItem()).defaultBlockState().getMaterial());
    }

    public static List<ItemStack> getMaterialStacks(@Nonnull Material material) {
        if (materialStacks.containsKey(material)) {
            return materialStacks.get(material);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            //Ugly check to make sure we don't include our bounding block in render list. Eventually this should use getRenderShape() with a dummy BlockState
            if (block != MekanismBlocks.BOUNDING_BLOCK.getBlock() && block.defaultBlockState().getMaterial() == material) {
                ItemStack stack = new ItemStack(block);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        }
        materialStacks.put(material, stacks);
        return stacks;
    }

    public static boolean tagHasMinerBlacklisted(@Nonnull String tag) {
        if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.isEmpty()) {
            return false;
        } else if (blockTagBlacklistedElements.containsKey(tag)) {
            return blockTagBlacklistedElements.getBoolean(tag);
        }
        boolean hasBlacklisted = TagUtils.manager(ForgeRegistries.BLOCKS).stream().anyMatch(blockTag ->
              WildcardMatcher.matches(tag, blockTag.getKey()) &&
              blockTag.stream().anyMatch(MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP::contains)
        );
        blockTagBlacklistedElements.put(tag, hasBlacklisted);
        return hasBlacklisted;
    }

    public static boolean modIDHasMinerBlacklisted(@Nonnull String modName) {
        if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.isEmpty()) {
            return false;
        } else if (modIDBlacklistedElements.containsKey(modName)) {
            return modIDBlacklistedElements.getBoolean(modName);
        }
        boolean hasBlacklisted = false;
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.contains(block) && WildcardMatcher.matches(modName, block.getRegistryName().getNamespace())) {
                hasBlacklisted = true;
                break;
            }
        }
        modIDBlacklistedElements.put(modName, hasBlacklisted);
        return hasBlacklisted;
    }

    public static boolean materialHasMinerBlacklisted(@Nonnull ItemStack stack) {
        return materialHasMinerBlacklisted(Block.byItem(stack.getItem()).defaultBlockState().getMaterial());
    }

    public static boolean materialHasMinerBlacklisted(@Nonnull Material material) {
        if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.isEmpty()) {
            return false;
        } else if (materialBlacklistedElements.containsKey(material)) {
            return materialBlacklistedElements.getBoolean(material);
        }
        boolean hasBlacklisted = false;
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block.defaultBlockState().getMaterial() == material && MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.contains(block)) {
                hasBlacklisted = true;
                break;
            }
        }
        materialBlacklistedElements.put(material, hasBlacklisted);
        return hasBlacklisted;
    }

    /**
     * @apiNote hasMatch might be true even if stacks is empty in the case there are blocks without a corresponding item form.
     */
    public record MatchingStacks(boolean hasMatch, List<ItemStack> stacks) {

        private static final MatchingStacks NONE = new MatchingStacks(false, Collections.emptyList());
    }
}