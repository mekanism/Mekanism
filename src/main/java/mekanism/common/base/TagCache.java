package mekanism.common.base;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
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
import mekanism.common.tags.MekanismTags;
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
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

//TODO: Try to come up with a better name for this class given it also handles things like materials, and modids
public final class TagCache {

    private TagCache() {
    }

    private static final Map<String, List<ItemStack>> blockTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> itemTagStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> modIDStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<Material, List<ItemStack>> materialStacks = new Object2ObjectOpenHashMap<>();
    private static final Map<Block, List<String>> tileEntityTypeTagCache = new Object2ObjectOpenHashMap<>();

    private static final Object2BooleanMap<String> blockTagBlacklistedElements = new Object2BooleanOpenHashMap<>();
    private static final Object2BooleanMap<String> modIDBlacklistedElements = new Object2BooleanOpenHashMap<>();
    private static final Object2BooleanMap<Material> materialBlacklistedElements = new Object2BooleanOpenHashMap<>();

    public static void resetVanillaTagCaches() {
        blockTagStacks.clear();
        itemTagStacks.clear();
        //These maps have the boolean value be based on a tag
        blockTagBlacklistedElements.clear();
        modIDBlacklistedElements.clear();
        materialBlacklistedElements.clear();
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
        } else if (block.hasTileEntity(block.defaultBlockState())) {
            //Otherwise, check if the block has a tile entity and if it does, gather all the tile types the block
            // is valid for as we don't want to risk initializing a tile for another mod as it may have side effects
            // that we don't know about and don't handle properly
            Set<ResourceLocation> tileEntityTags = new HashSet<>();
            for (TileEntityType<?> tileEntityType : ForgeRegistries.TILE_ENTITIES) {
                if (tileEntityType.isValid(block)) {
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
        return getTagStacks(itemTagStacks, ItemTags.getAllTags(), oreName);
    }

    public static List<ItemStack> getBlockTagStacks(@Nonnull String oreName) {
        return getTagStacks(blockTagStacks, BlockTags.getAllTags(), oreName);
    }

    private static <TYPE extends IItemProvider> List<ItemStack> getTagStacks(Map<String, List<ItemStack>> cache, ITagCollection<TYPE> tagCollection,
          @Nonnull String oreName) {
        if (cache.containsKey(oreName)) {
            return cache.get(oreName);
        }
        Set<TYPE> items = new HashSet<>();
        for (Map.Entry<ResourceLocation, ITag<TYPE>> entry : tagCollection.getAllTags().entrySet()) {
            if (WildcardMatcher.matches(oreName, entry.getKey().toString())) {
                items.addAll(entry.getValue().getValues());
            }
        }
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        cache.put(oreName, stacks);
        return stacks;
    }

    public static List<ItemStack> getModIDStacks(@Nonnull String modName, boolean forceBlock) {
        if (modIDStacks.containsKey(modName)) {
            return modIDStacks.get(modName);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!forceBlock || item instanceof BlockItem) {
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should use getRenderShape() with a dummy BlockState
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
        return getMaterialStacks(Block.byItem(stack.getItem()).defaultBlockState().getMaterial());
    }

    public static List<ItemStack> getMaterialStacks(@Nonnull Material material) {
        if (materialStacks.containsKey(material)) {
            return materialStacks.get(material);
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                //Ugly check to make sure we don't include our bounding block in render list. Eventually this should use getRenderShape() with a dummy BlockState
                //noinspection ConstantConditions getBlock is nonnull, but if something "goes wrong" it returns null, just skip it
                if (block == null || block instanceof BlockBounding) {
                    continue;
                }
                if (block.defaultBlockState().getMaterial() == material) {
                    stacks.add(new ItemStack(item));
                }
            }
        }
        materialStacks.put(material, stacks);
        return stacks;
    }

    public static boolean tagHasMinerBlacklisted(@Nonnull String tag) {
        if (MekanismTags.Blocks.MINER_BLACKLIST.getValues().isEmpty()) {
            return false;
        } else if (blockTagBlacklistedElements.containsKey(tag)) {
            return blockTagBlacklistedElements.getBoolean(tag);
        }
        boolean hasBlacklisted = false;
        for (Map.Entry<ResourceLocation, ITag<Block>> entry : BlockTags.getAllTags().getAllTags().entrySet()) {
            if (WildcardMatcher.matches(tag, entry.getKey().toString()) && entry.getValue().getValues().stream().anyMatch(MekanismTags.Blocks.MINER_BLACKLIST::contains)) {
                hasBlacklisted = true;
                break;
            }
        }
        blockTagBlacklistedElements.put(tag, hasBlacklisted);
        return hasBlacklisted;
    }

    public static boolean modIDHasMinerBlacklisted(@Nonnull String modName) {
        if (MekanismTags.Blocks.MINER_BLACKLIST.getValues().isEmpty()) {
            return false;
        } else if (modIDBlacklistedElements.containsKey(modName)) {
            return modIDBlacklistedElements.getBoolean(modName);
        }
        boolean hasBlacklisted = false;
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (MekanismTags.Blocks.MINER_BLACKLIST.contains(block) && WildcardMatcher.matches(modName, block.getRegistryName().getNamespace())) {
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
        if (MekanismTags.Blocks.MINER_BLACKLIST.getValues().isEmpty()) {
            return false;
        } else if (materialBlacklistedElements.containsKey(material)) {
            return materialBlacklistedElements.getBoolean(material);
        }
        boolean hasBlacklisted = false;
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block.defaultBlockState().getMaterial() == material && MekanismTags.Blocks.MINER_BLACKLIST.contains(block)) {
                hasBlacklisted = true;
                break;
            }
        }
        materialBlacklistedElements.put(material, hasBlacklisted);
        return hasBlacklisted;
    }
}