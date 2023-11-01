package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.tags.ITag;
import net.neoforged.neoforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

//TODO: Try to come up with a better name for this class given it also handles things like modids
public final class TagCache {

    private TagCache() {
    }

    private static final Map<String, MatchingStacks> blockTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> itemTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> itemModIDStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, MatchingStacks> blockModIDStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<Block, List<String>> tileEntityTypeTagCache = new IdentityHashMap<>();

    private static final Object2BooleanMap<String> blockTagBlacklistedElements = new Object2BooleanOpenHashMap<>();
    private static final Object2BooleanMap<String> modIDBlacklistedElements = new Object2BooleanOpenHashMap<>();

    public static void resetTagCaches() {
        blockTagStacks.clear();
        itemTagStacks.clear();
        tileEntityTypeTagCache.clear();
        //These maps have the boolean value be based on if an element is in a given tag
        blockTagBlacklistedElements.clear();
        modIDBlacklistedElements.clear();
    }

    public static List<String> getItemTags(@NotNull ItemStack check) {
        return getTagsAsStrings(check.getTags());
    }

    public static List<String> getTileEntityTypeTags(@NotNull Block block) {
        return tileEntityTypeTagCache.computeIfAbsent(block, b -> {
            if (b instanceof IHasTileEntity<?> hasTileEntity) {
                //If it is one of our blocks, short circuit and just lookup the tile's type directly
                return getTagsAsStrings(TagUtils.tagsStream(ForgeRegistries.BLOCK_ENTITY_TYPES, hasTileEntity.getTileType().get()));
            }
            BlockState state = b.defaultBlockState();
            if (state.hasBlockEntity()) {
                //Otherwise, check if the block has a tile entity and if it does, gather all the tile types the block
                // is valid for as we don't want to risk initializing a tile for another mod as it may have side effects
                // that we don't know about and don't handle properly
                ITagManager<BlockEntityType<?>> manager = TagUtils.manager(ForgeRegistries.BLOCK_ENTITY_TYPES);
                return getTagsAsStrings(StreamSupport.stream(ForgeRegistries.BLOCK_ENTITY_TYPES.spliterator(), false)
                      .filter(type -> type.isValid(state))
                      .flatMap(type -> TagUtils.tagsStream(manager, type))
                      .distinct()
                );
            }
            return Collections.emptyList();
        });
    }

    public static <TYPE> List<String> getTagsAsStrings(@NotNull Stream<TagKey<TYPE>> tags) {
        return tags.map(tag -> tag.location().toString()).toList();
    }

    public static List<ItemStack> getItemTagStacks(@NotNull String tagName) {
        return itemTagStacks.computeIfAbsent(tagName, name -> {
            Set<Item> items = collectTagStacks(TagUtils.manager(ForgeRegistries.ITEMS), name, item -> item != MekanismBlocks.BOUNDING_BLOCK.asItem());
            return items.stream().map(ItemStack::new).filter(stack -> !stack.isEmpty()).toList();
        });
    }

    public static MatchingStacks getBlockTagStacks(@NotNull String tagName) {
        return blockTagStacks.computeIfAbsent(tagName, name -> {
            Set<Block> blocks = collectTagStacks(TagUtils.manager(ForgeRegistries.BLOCKS), name, block -> block != MekanismBlocks.BOUNDING_BLOCK.getBlock());
            return getMatching(blocks);
        });
    }

    private static <TYPE extends ItemLike> Set<TYPE> collectTagStacks(ITagManager<TYPE> tagManager, String tagName, Predicate<TYPE> validElement) {
        return tagManager.stream().filter(tag -> WildcardMatcher.matches(tagName, tag.getKey()))
              .flatMap(ITag::stream)
              .filter(validElement)
              .collect(Collectors.toSet());
    }

    private static MatchingStacks getMatching(Set<Block> blocks) {
        if (blocks.isEmpty()) {
            return MatchingStacks.NONE;
        }
        //Filter out any stacks that are empty such as if we are mining a block that doesn't have a direct item representation
        return new MatchingStacks(true, blocks.stream().map(ItemStack::new).filter(stack -> !stack.isEmpty()).toList());
    }

    public static List<ItemStack> getItemModIDStacks(@NotNull String modName) {
        return itemModIDStacks.computeIfAbsent(modName, name -> {
            List<ItemStack> stacks = new ArrayList<>();
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should maybe just use getRenderShape() with a dummy BlockState
                if (item != MekanismBlocks.BOUNDING_BLOCK.asItem()) {
                    //Note: We get the modid based on the stack so that if there is a mod that has a different modid for an item
                    // that isn't based on NBT it can properly change the modid (this is unlikely to happen, but you never know)
                    ItemStack stack = new ItemStack(item);
                    if (!stack.isEmpty() && WildcardMatcher.matches(name, MekanismUtils.getModId(stack))) {
                        stacks.add(stack);
                    }
                }
            }
            return stacks;
        });
    }

    public static MatchingStacks getBlockModIDStacks(@NotNull String modName) {
        return blockModIDStacks.computeIfAbsent(modName, name -> {
            Set<Block> blocks = new ReferenceOpenHashSet<>();
            for (Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
                Block block = entry.getValue();
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should maybe just use getRenderShape() with a dummy BlockState
                if (block != MekanismBlocks.BOUNDING_BLOCK.getBlock() && WildcardMatcher.matches(name, entry.getKey().location().getNamespace())) {
                    blocks.add(block);
                }
            }
            return getMatching(blocks);
        });
    }

    public static boolean tagHasMinerBlacklisted(@NotNull String tag) {
        if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.isEmpty()) {
            return false;
        }
        return blockTagBlacklistedElements.computeIfAbsent(tag, (String t) -> TagUtils.manager(ForgeRegistries.BLOCKS).stream().anyMatch(blockTag ->
              WildcardMatcher.matches(t, blockTag.getKey()) && blockTag.stream().anyMatch(MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP::contains)
        ));
    }

    public static boolean modIDHasMinerBlacklisted(@NotNull String modName) {
        if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.isEmpty()) {
            return false;
        }
        return modIDBlacklistedElements.computeIfAbsent(modName, (String name) -> {
            for (Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
                Block block = entry.getValue();
                if (MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.contains(block) && WildcardMatcher.matches(name, entry.getKey().location().getNamespace())) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * @apiNote hasMatch might be true even if stacks is empty in the case there are blocks without a corresponding item form.
     */
    public record MatchingStacks(boolean hasMatch, List<ItemStack> stacks) {

        private static final MatchingStacks NONE = new MatchingStacks(false, Collections.emptyList());
    }
}