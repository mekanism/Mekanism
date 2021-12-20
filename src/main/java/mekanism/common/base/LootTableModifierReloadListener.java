package mekanism.common.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.mekatool.ModuleShearingUnit;
import mekanism.common.lib.FieldReflectionHelper;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MinMaxBounds.IntBound;
import net.minecraft.advancements.criterion.NBTPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootPredicateManager;
import net.minecraft.loot.LootPredicateManager.AndCombiner;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ParentedLootEntry;
import net.minecraft.loot.conditions.Alternative;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.Inverted;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.potion.Potion;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

/**
 * Reload listener to modify loot tables and loot predicates after loading to support the shears tool type in places that normally support shears. We need to do this so
 * that the Meka-Tool Shearing Unit is able to conditionally act as shears and have the dropped loot correspond to it, and just adding it to the shears tag does not allow
 * for it to be conditional based on if the module is installed. This is <strong>NOT</strong> a great thing to be doing, and would be <strong>MUCH</strong> better done
 * via a forge patch or PR, but there are currently issues that would need to be resolved related to other places that potentially should use a shears tool type, what
 * harvest levels to pass to it, and the fact that forge already replaces a bunch of vanilla loot tables to use the shears tag instead of the shears item. Once a solution
 * gets to the point where it seems reasonable enough to do, a forge PR <strong>SHOULD</strong> be made and this reload listener removed.
 */
public class LootTableModifierReloadListener implements IResourceManagerReloadListener {//TODO - 1.18: Remove this won't be needed anymore

    //These top three are inaccessible via ATs due to forge patches targeting the lines they are declared on and changing the types
    private static final FieldReflectionHelper<LootPool, List<ILootCondition>> LOOT_POOL_CONDITIONS = new FieldReflectionHelper<>(LootPool.class, "field_186454_b", Collections::emptyList);
    private static final FieldReflectionHelper<LootPool, List<LootEntry>> LOOT_POOL_ENTRIES = new FieldReflectionHelper<>(LootPool.class, "field_186453_a", Collections::emptyList);
    private static final FieldReflectionHelper<LootTable, List<LootPool>> LOOT_TABLE_POOLS = new FieldReflectionHelper<>(LootTable.class, "field_186466_c", Collections::emptyList);
    //The following could in theory be in ATs but aren't because we need to have reflection here anyway, so we may as well do it all via reflection
    private static final FieldReflectionHelper<LootEntry, ILootCondition[]> LOOT_ENTRY_CONDITIONS = new FieldReflectionHelper<>(LootEntry.class, "field_216144_d", () -> new ILootCondition[0]);
    private static final FieldReflectionHelper<ParentedLootEntry, LootEntry[]> PARENTED_LOOT_ENTRY_CHILDREN = new FieldReflectionHelper<>(ParentedLootEntry.class, "field_216147_c", () -> new LootEntry[0]);
    private static final FieldReflectionHelper<Alternative, ILootCondition[]> ALTERNATIVE_CONDITION = new FieldReflectionHelper<>(Alternative.class, "field_215962_a", () -> new ILootCondition[0]);
    private static final FieldReflectionHelper<Inverted, ILootCondition> INVERTED_CONDITION = new FieldReflectionHelper<>(Inverted.class, "field_215981_a", () -> null);
    private static final FieldReflectionHelper<ItemPredicate, ITag<Item>> ITEM_PREDICATE_TAG = new FieldReflectionHelper<>(ItemPredicate.class, "field_200018_b", () -> null);
    private static final FieldReflectionHelper<ItemPredicate, Item> ITEM_PREDICATE_ITEM = new FieldReflectionHelper<>(ItemPredicate.class, "field_192496_b", () -> null);
    private static final FieldReflectionHelper<ItemPredicate, IntBound> ITEM_PREDICATE_COUNT = new FieldReflectionHelper<>(ItemPredicate.class, "field_192498_d", () -> MinMaxBounds.IntBound.ANY);
    private static final FieldReflectionHelper<ItemPredicate, IntBound> ITEM_PREDICATE_DURABILITY = new FieldReflectionHelper<>(ItemPredicate.class, "field_193444_e", () -> MinMaxBounds.IntBound.ANY);
    private static final FieldReflectionHelper<ItemPredicate, EnchantmentPredicate[]> ITEM_PREDICATE_ENCHANTMENTS = new FieldReflectionHelper<>(ItemPredicate.class, "field_192499_e", () -> EnchantmentPredicate.NONE);
    private static final FieldReflectionHelper<ItemPredicate, EnchantmentPredicate[]> ITEM_PREDICATE_STORED_ENCHANTMENTS = new FieldReflectionHelper<>(ItemPredicate.class, "field_226656_g_", () -> EnchantmentPredicate.NONE);
    private static final FieldReflectionHelper<ItemPredicate, Potion> ITEM_PREDICATE_POTION = new FieldReflectionHelper<>(ItemPredicate.class, "field_192500_f", () -> null);
    private static final FieldReflectionHelper<ItemPredicate, NBTPredicate> ITEM_PREDICATE_NBT = new FieldReflectionHelper<>(ItemPredicate.class, "field_193445_h", () -> NBTPredicate.ANY);
    private static final FieldReflectionHelper<LootPredicateManager, Map<ResourceLocation, ILootCondition>> LOOT_PREDICATE_CONDITIONS = new FieldReflectionHelper<>(LootPredicateManager.class, "field_227512_c_", Collections::emptyMap);
    private static final FieldReflectionHelper<AndCombiner, ILootCondition[]> LOOT_PREDICATE_AND_CONDITION = new FieldReflectionHelper<>(LootPredicateManager.AndCombiner.class, "field_237405_a_", () -> new ILootCondition[0]);

    private final DataPackRegistries dataPackRegistries;

    public LootTableModifierReloadListener(DataPackRegistries dataPackRegistries) {
        this.dataPackRegistries = dataPackRegistries;
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        //Grab the current shears tag instance as we will be doing identity compares and even if we weren't
        // the named wrapper for the tag won't be populated yet
        ITag<Item> shearsTag = dataPackRegistries.getTags().getItems().getTag(new ResourceLocation("forge", "shears"));
        LootTableManager lootTableManager = dataPackRegistries.getLootTables();
        for (ResourceLocation id : lootTableManager.getIds()) {
            LootTable lootTable = lootTableManager.get(id);
            boolean modified = false;
            //Scan all the loot tables in the upcoming/"current" loot table manager and evaluate if their pools need modifying
            for (LootPool pool : LOOT_TABLE_POOLS.getValue(lootTable)) {
                //Loot pools have two places loot conditions come into play:
                // 1. As part of the various loot entries
                // 2. As the conditions to the loot pool itself
                for (LootEntry entry : LOOT_POOL_ENTRIES.getValue(pool)) {
                    modified |= modifyEntry(entry, shearsTag);
                }
                for (ILootCondition condition : LOOT_POOL_CONDITIONS.getValue(pool)) {
                    modified |= modifyCondition(condition, shearsTag);
                }
            }
            if (modified) {
                //If we changed any of the conditions in this loot table, add a debug message stating that we did so
                Mekanism.logger.debug("Modified and wrapped condition(s) in loot table '{}' to support items with the tool type of shears.", id);
            }
        }
        //We also need to scan all loot predicate in the upcoming/"current" manager, given even though vanilla doesn't add
        // any predicates by default that have to do with shears, we want to make sure we catch any that may be defined by
        // a mod or data pack
        LootPredicateManager predicateManager = dataPackRegistries.getPredicateManager();
        for (Map.Entry<ResourceLocation, ILootCondition> entry : LOOT_PREDICATE_CONDITIONS.getValue(predicateManager).entrySet()) {
            if (modifyCondition(entry.getValue(), shearsTag)) {
                //If we changed any of the conditions in this loot predicate, add a debug message stating that we did so
                Mekanism.logger.debug("Modified and wrapped condition for loot predicate '{}' to support items with the tool type of shears.", entry.getKey());
            }
        }
    }

    private static boolean modifyEntry(LootEntry entry, ITag<Item> shearsTag) {
        boolean modified = false;
        if (entry instanceof ParentedLootEntry) {
            //If the entry is a parented loot entry, check if any of the child entries need their conditions modified
            for (LootEntry child : PARENTED_LOOT_ENTRY_CHILDREN.getValue((ParentedLootEntry) entry)) {
                modified |= modifyEntry(child, shearsTag);
            }
        }
        //Check if the entry has any conditions that need modifying. This will happen for all entry types including parented ones
        modified |= modifyConditions(LOOT_ENTRY_CONDITIONS.getValue(entry), shearsTag);
        //Note: We don't necessarily catch all the conditions if a mod introduces a custom entry that keeps track of conditions
        // in a different way but there isn't much that can be done about that
        return modified;
    }

    private static boolean modifyCondition(ILootCondition condition, ITag<Item> shearsTag) {
        if (condition instanceof Alternative) {
            //If the condition is an alternative condition (a bunch of ORs), then check if any of the inner conditions need to be modified
            return modifyConditions(ALTERNATIVE_CONDITION.getValue((Alternative) condition), shearsTag);
        } else if (condition instanceof LootPredicateManager.AndCombiner) {
            //If the condition is a loot predicate and condition (a bunch of ANDs) that can only happen for loot predicates
            // handle it as we also need to make sure we handle loot predicates
            return modifyConditions(LOOT_PREDICATE_AND_CONDITION.getValue((LootPredicateManager.AndCombiner) condition), shearsTag);
        } else if (condition instanceof Inverted) {
            //If the condition is an inverted condition (NOT condition), check if the inner condition needs to be modified
            ILootCondition inner = INVERTED_CONDITION.getValue((Inverted) condition);
            return inner != null && modifyCondition(inner, shearsTag);
        } else if (condition instanceof MatchTool) {
            //If the condition is a MatchTool condition, check if it matches shears or the shears tag
            MatchTool matchToolCondition = (MatchTool) condition;
            ItemPredicate predicate = matchToolCondition.predicate;
            if (ITEM_PREDICATE_ITEM.getValue(predicate) == Items.SHEARS || ITEM_PREDICATE_TAG.getValue(predicate) == shearsTag) {
                //If it does match, overwrite the predicate with a custom one that wraps and also allows for items that expose/declare the shears
                // tool type. While this predicate is normally private and final, we can override it as it is not exposed anywhere except via
                // test methods, so things interacting with the MatchTool condition will just interact with our replaced predicate via it,
                // and there is no danger of anything having tried to cache the predicate elsewhere that we then would have to update as well
                matchToolCondition.predicate = new ShearsItemPredicate(predicate);
                return true;
            }
        }
        return false;
    }

    private static boolean modifyConditions(ILootCondition[] conditions, ITag<Item> shearsTag) {
        boolean modified = false;
        for (ILootCondition condition : conditions) {
            //Check each condition for if it needs to be modified, and modify it if so
            modified |= modifyCondition(condition, shearsTag);
        }
        return modified;
    }

    public static void registerCustomPredicate() {
        //Registers our custom ItemPredicate in case serialization does happen/is needed, though I believe in most cases this will be unused
        ItemPredicate.register(ShearsItemPredicate.NAME, ShearsItemPredicate::fromJson);
    }

    private static class ShearsItemPredicate extends ItemPredicate {

        public static ResourceLocation NAME = Mekanism.rl("custom_shears");

        private final ItemPredicate inner;

        private ShearsItemPredicate(ItemPredicate inner) {
            //Pass all the values except for item and tag to our own predicate, so that if it is a predicate that wants
            // shears with specific enchantments or something then our check will also ensure the tool type has those
            // enchantments. We don't pass the item or tag though as we don't want it to fail to match due to them not matching
            super(null, null, ITEM_PREDICATE_COUNT.getValue(inner), ITEM_PREDICATE_DURABILITY.getValue(inner),
                  ITEM_PREDICATE_ENCHANTMENTS.getValue(inner), ITEM_PREDICATE_STORED_ENCHANTMENTS.getValue(inner),
                  ITEM_PREDICATE_POTION.getValue(inner), ITEM_PREDICATE_NBT.getValue(inner));
            // we also store the inner or source item predicate so that we can test against it and properly respect the item or tag it wants to check
            // and not deny it if it isn't exposing the tool type
            this.inner = inner;
        }

        @Override
        public boolean matches(@Nonnull ItemStack stack) {
            //If original predicate matches, or it is a shears tool type and the rest of the predicate matches
            return inner.matches(stack) || stack.getToolTypes().contains(ModuleShearingUnit.SHEARS_TOOL_TYPE) && super.matches(stack);
        }

        @Nonnull
        public static ShearsItemPredicate fromJson(JsonObject json) {
            if (!json.has("inner")) {
                throw new JsonParseException("Missing inner item predicate.");
            }
            return new ShearsItemPredicate(ItemPredicate.fromJson(json.get("inner")));
        }

        @Nonnull
        @Override
        public JsonElement serializeToJson() {
            JsonObject json = (JsonObject) super.serializeToJson();
            json.addProperty(JsonConstants.TYPE, NAME.toString());
            json.add("inner", inner.serializeToJson());
            return json;
        }
    }
}