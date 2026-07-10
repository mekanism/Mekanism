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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.Registry;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

//TODO: Try to come up with a better name for this class given it also handles things like modids
public final class TagCache {

    private TagCache() {
    }

    private static final HolderSet.Named<Block> MINER_BLACKLIST_LOOKUP = BuiltInRegistries.BLOCK.getOrThrow(MekanismTags.Blocks.MINER_BLACKLIST);

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

    public static List<String> getItemTags(TypedInstance<Item> check) {
        return getTagsAsStrings(check.tags());
    }

    public static List<String> getTileEntityTypeTags(Block block) {
        List<String> cache = tileEntityTypeTagCache.get(block);
        if (cache == null) {
            if (block instanceof IHasTileEntity<?> hasTileEntity) {
                //If it is one of our blocks, short circuit and just lookup the tile's type directly
                cache = getTagsAsStrings(hasTileEntity.getTileType());
            } else {
                BlockState state = block.defaultBlockState();
                if (state.hasBlockEntity()) {
                    //Otherwise, check if the block has a tile entity and if it does, gather all the tile types the block
                    // is valid for as we don't want to risk initializing a tile for another mod as it may have side effects
                    // that we don't know about and don't handle properly
                    cache = getTagsAsStrings(StreamSupport.stream(BuiltInRegistries.BLOCK_ENTITY_TYPE.spliterator(), false)
                          .filter(type -> type.isValid(state))
                          .flatMap(type -> RegistryUtils.getBEHolder(type).tags())
                          .distinct()
                    );
                } else {
                    cache = Collections.emptyList();
                }
            }
            tileEntityTypeTagCache.put(block, cache);
        }
        return cache;
    }

    public static <TYPE> List<String> getTagsAsStrings(Holder<TYPE> holder) {
        return getTagsAsStrings(holder.tags());
    }

    public static <TYPE> List<String> getTagsAsStrings(Stream<TagKey<TYPE>> tags) {
        return tags.map(tag -> tag.location().toString()).toList();
    }

    public static List<ItemStack> getItemTagStacks(String tagName) {
        return itemTagStacks.computeIfAbsent(tagName, name -> collectTagStacks(BuiltInRegistries.ITEM, name).map(ItemStack::new).filter(stack -> !stack.isEmpty()).toList());
    }

    public static MatchingStacks getBlockTagStacks(String tagName) {
        return blockTagStacks.computeIfAbsent(tagName, name -> {
            Set<Block> blocks = collectTagStacks(BuiltInRegistries.BLOCK, name)
                  .filter(block -> block != MekanismBlocks.BOUNDING_BLOCK.get())
                  .collect(Collectors.toSet());
            return getMatching(blocks);
        });
    }

    private static <TYPE> Stream<TYPE> collectTagStacks(Registry<TYPE> registry, String tagName) {
        return registry.getTags()
              .filter(tag -> WildcardMatcher.matches(tagName, tag.key()))
              .flatMap(ListBacked::stream)
              .map(Holder::value);
    }

    private static MatchingStacks getMatching(Set<Block> blocks) {
        if (blocks.isEmpty()) {
            return MatchingStacks.NONE;
        }
        //Filter out any stacks that are empty such as if we are mining a block that doesn't have a direct item representation
        return new MatchingStacks(true, blocks.stream().map(ItemStack::new).filter(stack -> !stack.isEmpty()).toList());
    }

    public static List<ItemStack> getItemModIDStacks(HolderLookup.Provider registries, String modName) {
        return itemModIDStacks.computeIfAbsent(modName, name -> {
            List<ItemStack> stacks = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                //Note: We get the modid based on the stack so that if there is a mod that has a different modid for an item
                // that isn't based on NBT it can properly change the modid (this is unlikely to happen, but you never know)
                ItemStack stack = new ItemStack(item);
                if (!stack.isEmpty() && WildcardMatcher.matches(name, MekanismUtils.getModId(registries, stack))) {
                    stacks.add(stack);
                }
            }
            return stacks;
        });
    }

    public static MatchingStacks getBlockModIDStacks(String modName) {
        return blockModIDStacks.computeIfAbsent(modName, name -> {
            Set<Block> blocks = new ReferenceOpenHashSet<>();
            for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should maybe just use getRenderShape() with a dummy BlockState
                if (!MekanismBlocks.BOUNDING_BLOCK.is(entry.getKey()) && WildcardMatcher.matches(name, entry.getKey().identifier().getNamespace())) {
                    blocks.add(entry.getValue());
                }
            }
            return getMatching(blocks);
        });
    }

    public static boolean tagHasMinerBlacklisted(String tag) {
        if (MINER_BLACKLIST_LOOKUP.size() == 0) {
            return false;
        }
        return blockTagBlacklistedElements.computeIfAbsent(tag, (String t) -> BuiltInRegistries.BLOCK.getTags()
              .anyMatch(blockTag -> WildcardMatcher.matches(t, blockTag.key()) &&
                                    blockTag.stream().anyMatch(element -> element.is(MekanismTags.Blocks.MINER_BLACKLIST))));
    }

    public static boolean modIDHasMinerBlacklisted(String modName) {
        if (MINER_BLACKLIST_LOOKUP.size() == 0) {
            return false;
        }
        return modIDBlacklistedElements.computeIfAbsent(modName, (String name) -> BuiltInRegistries.BLOCK.listElements()
              .anyMatch(holder -> holder.is(MekanismTags.Blocks.MINER_BLACKLIST) && WildcardMatcher.matches(name, holder.key().identifier().getNamespace())));
    }

    /// @apiNote hasMatch might be true even if stacks is empty in the case there are blocks without a corresponding item form.
    public record MatchingStacks(boolean hasMatch, List<ItemStack> stacks) {

        private static final MatchingStacks NONE = new MatchingStacks(false, Collections.emptyList());
    }
}