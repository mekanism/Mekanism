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
import java.util.function.Predicate;
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
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

//TODO: Try to come up with a better name for this class given it also handles things like modids
public final class TagCache {

    private TagCache() {
    }

    private static final HolderSet.Named<Block> MINER_BLACKLIST_LOOKUP = BuiltInRegistries.BLOCK.getOrCreateTag(MekanismTags.Blocks.MINER_BLACKLIST);

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

    public static <TYPE> List<String> getTagsAsStrings(@NotNull Holder<TYPE> holder) {
        return getTagsAsStrings(holder.tags());
    }

    public static <TYPE> List<String> getTagsAsStrings(@NotNull Stream<TagKey<TYPE>> tags) {
        return tags.map(tag -> tag.location().toString()).toList();
    }

    public static List<ItemStack> getItemTagStacks(@NotNull String tagName) {
        return itemTagStacks.computeIfAbsent(tagName, name -> {
            Set<Item> items = collectTagStacks(BuiltInRegistries.ITEM, name, item -> !MekanismBlocks.BOUNDING_BLOCK.isSecondary(item));
            return items.stream().map(ItemStack::new).filter(stack -> !stack.isEmpty()).toList();
        });
    }

    public static MatchingStacks getBlockTagStacks(@NotNull String tagName) {
        return blockTagStacks.computeIfAbsent(tagName, name -> {
            Set<Block> blocks = collectTagStacks(BuiltInRegistries.BLOCK, name, block -> !MekanismBlocks.BOUNDING_BLOCK.is(block));
            return getMatching(blocks);
        });
    }

    private static <TYPE> Set<TYPE> collectTagStacks(Registry<TYPE> registry, String tagName, Predicate<TYPE> validElement) {
        return registry.getTags()
              .filter(pair -> WildcardMatcher.matches(tagName, pair.getFirst()))
              .flatMap(pair -> pair.getSecond().stream())
              .map(Holder::value)
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
            for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should maybe just use getRenderShape() with a dummy BlockState
                if (!MekanismBlocks.BOUNDING_BLOCK.getItemHolder().is(entry.getKey())) {
                    //Note: We get the modid based on the stack so that if there is a mod that has a different modid for an item
                    // that isn't based on NBT it can properly change the modid (this is unlikely to happen, but you never know)
                    ItemStack stack = new ItemStack(entry.getValue());
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
            for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should maybe just use getRenderShape() with a dummy BlockState
                if (!MekanismBlocks.BOUNDING_BLOCK.is(entry.getKey()) && WildcardMatcher.matches(name, entry.getKey().location().getNamespace())) {
                    blocks.add(entry.getValue());
                }
            }
            return getMatching(blocks);
        });
    }

    public static boolean tagHasMinerBlacklisted(@NotNull String tag) {
        if (MINER_BLACKLIST_LOOKUP.size() == 0) {
            return false;
        }
        return blockTagBlacklistedElements.computeIfAbsent(tag, (String t) -> BuiltInRegistries.BLOCK.getTags()
              .anyMatch(pair -> WildcardMatcher.matches(t, pair.getFirst()) &&
                                pair.getSecond().stream().anyMatch(element -> element.is(MekanismTags.Blocks.MINER_BLACKLIST))));
    }

    public static boolean modIDHasMinerBlacklisted(@NotNull String modName) {
        if (MINER_BLACKLIST_LOOKUP.size() == 0) {
            return false;
        }
        return modIDBlacklistedElements.computeIfAbsent(modName, (String name) -> BuiltInRegistries.BLOCK.holders()
              .anyMatch(holder -> holder.is(MekanismTags.Blocks.MINER_BLACKLIST) && WildcardMatcher.matches(name, holder.key().location().getNamespace())));
    }

    /**
     * @apiNote hasMatch might be true even if stacks is empty in the case there are blocks without a corresponding item form.
     */
    public record MatchingStacks(boolean hasMatch, List<ItemStack> stacks) {

        private static final MatchingStacks NONE = new MatchingStacks(false, Collections.emptyList());
    }
}