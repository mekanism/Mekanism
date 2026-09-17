package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collections;
import java.util.HashMap;
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
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.Registry;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.TagSlotDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

//TODO: Try to come up with a better name for this class given it also handles things like modids
public final class TagCache {

    private TagCache() {
    }

    private static final HolderSet.Named<Block> MINER_BLACKLIST_LOOKUP = BuiltInRegistries.BLOCK.getOrThrow(MekanismTags.Blocks.MINER_BLACKLIST);

    private static final Map<String, MatchingStacks> blockTagStacks = new HashMap<>();
    private static final Map<String, MatchingStacks> itemTagStacks = new HashMap<>();
    private static final Map<String, MatchingStacks> itemModIDStacks = new HashMap<>();
    private static final Map<String, MatchingStacks> blockModIDStacks = new HashMap<>();
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

    public static MatchingStacks getTagItems(HolderLookup.Provider registries, String tagName) {
        if (tagName.isEmpty()) {
            return MatchingStacks.NONE;
        }
        MatchingStacks matchingTargets = itemTagStacks.get(tagName);
        if (matchingTargets == null) {
            //Note: We use this instead of computeIfAbsent, to avoid the capturing lambdas for the already cached path
            List<Named<Item>> matchingTags = registries.lookupOrThrow(Registries.ITEM)
                  .listTags()
                  .filter(element -> WildcardMatcher.matches(tagName, element.key()))
                  .toList();
            if (matchingTags.isEmpty()) {
                matchingTargets = MatchingStacks.NONE;
            } else if (matchingTags.size() == 1) {
                Named<Item> tag = matchingTags.getFirst();
                matchingTargets = tag.isBound() && tag.size() > 0 ? new MatchingStacks(true, new TagSlotDisplay(tag)) : MatchingStacks.NONE;
            } else {
                List<SlotDisplay> displays = matchingTags
                      .stream()
                      .flatMap(ListBacked::stream)
                      .distinct()
                      .<SlotDisplay>map(ItemSlotDisplay::new)
                      .toList();
                matchingTargets = displays.isEmpty() ? MatchingStacks.NONE : new MatchingStacks(true, MekanismUtils.compactDisplay(displays));
            }
            itemTagStacks.put(tagName, matchingTargets);
        }
        return matchingTargets;
    }

    public static MatchingStacks getBlockTagStacks(String tagName) {
        if (tagName.isEmpty()) {
            return MatchingStacks.NONE;
        }
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
        List<SlotDisplay> slotDisplays = blocks.stream()
              .map(block -> block.asItem().builtInRegistryHolder())
              .filter(item -> !item.is(BlockItemIds.AIR.item()))
              .<SlotDisplay>map(ItemSlotDisplay::new)
              .toList();
        return new MatchingStacks(true, MekanismUtils.compactDisplay(slotDisplays));
    }

    public static MatchingStacks getModIdItems(HolderLookup.Provider registries, String modName) {
        if (modName.isEmpty()) {
            return MatchingStacks.NONE;
        }
        MatchingStacks matchingTargets = itemModIDStacks.get(modName);
        if (matchingTargets == null) {
            //Note: We use this instead of computeIfAbsent, to avoid the capturing lambdas for the already cached path
            List<SlotDisplay> modItems = registries.lookupOrThrow(Registries.ITEM)
                  .listElements()
                  .filter(element -> {
                      if (element.is(BlockItemIds.AIR.item())) {
                          //Exclude the empty item
                          return false;
                      }
                      //Note: We get the modid based on the stack so that if there is a mod that has a different modid for an item
                      // that isn't based on NBT it can properly change the modid (this is unlikely to happen, but you never know)
                      return WildcardMatcher.matches(modName, MekanismUtils.getModId(registries, element));
                  }).<SlotDisplay>map(ItemSlotDisplay::new)
                  .toList();
            if (modItems.isEmpty()) {
                matchingTargets = MatchingStacks.NONE;
            } else {
                matchingTargets = new MatchingStacks(true, MekanismUtils.compactDisplay(modItems));
            }
            itemModIDStacks.put(modName, matchingTargets);
        }
        return matchingTargets;
    }

    public static MatchingStacks getBlockModIDStacks(String modName) {
        if (modName.isEmpty()) {
            return MatchingStacks.NONE;
        }
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
    public record MatchingStacks(boolean hasMatch, SlotDisplay display) {

        private static final MatchingStacks NONE = new MatchingStacks(false, SlotDisplay.Empty.INSTANCE);
    }
}