package mekanism.tools.common.recipe;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedSmithingRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.DoubleLine;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.material.MaterialType;
import mekanism.tools.common.registration.ArmorCollection;
import mekanism.tools.common.registration.ToolCollection;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

public class ToolsRecipeProvider extends BaseRecipeProvider {

    private static final char AXE_CHAR = 'A';
    private static final char PICKAXE_CHAR = 'P';
    private static final char ROD_CHAR = 'R';
    private static final char SHOVEL_CHAR = 'S';

    //Armor patterns
    private static final RecipePattern HELMET = RecipePattern.createPattern(
          TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT));
    private static final RecipePattern CHESTPLATE = RecipePattern.createPattern(
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT));
    private static final RecipePattern LEGGINGS = RecipePattern.createPattern(
          TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT));
    private static final RecipePattern BOOTS = RecipePattern.createPattern(
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT));
    private static final RecipePattern SHIELD = RecipePattern.createPattern(
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY));
    //Tool Patterns
    private static final RecipePattern AXE = RecipePattern.createPattern(
          DoubleLine.of(Pattern.INGOT, Pattern.INGOT),
          DoubleLine.of(Pattern.INGOT, ROD_CHAR),
          DoubleLine.of(Pattern.EMPTY, ROD_CHAR));
    private static final RecipePattern HOE = RecipePattern.createPattern(
          DoubleLine.of(Pattern.INGOT, Pattern.INGOT),
          DoubleLine.of(Pattern.EMPTY, ROD_CHAR),
          DoubleLine.of(Pattern.EMPTY, ROD_CHAR));
    private static final RecipePattern PICKAXE = RecipePattern.createPattern(
          TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY),
          TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY));
    private static final RecipePattern SHOVEL = RecipePattern.createPattern(Pattern.INGOT, ROD_CHAR, ROD_CHAR);
    private static final RecipePattern SWORD = RecipePattern.createPattern(Pattern.INGOT, Pattern.INGOT, ROD_CHAR);
    private static final RecipePattern SPEAR = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.EMPTY, Pattern.INGOT),
          TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY),
          TripleLine.of(ROD_CHAR, Pattern.EMPTY, Pattern.EMPTY));
    private static final RecipePattern PAXEL = RecipePattern.createPattern(
          TripleLine.of(AXE_CHAR, PICKAXE_CHAR, SHOVEL_CHAR),
          TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY),
          TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY));

    public ToolsRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(output, registries);
    }

    @Override
    protected void addRecipes(HolderLookup.Provider registries) {
        registerRecipeSet(MaterialType.BRONZE, MekanismTags.Items.INGOTS_BRONZE, MekanismItems.BRONZE_NUGGET);
        registerRecipeSet(MaterialType.LAPIS_LAZULI, Tags.Items.GEMS_LAPIS, null);
        registerRecipeSet(MaterialType.OSMIUM, MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM),
              MekanismItems.getProcessedResource(ResourceType.NUGGET, PrimaryResource.OSMIUM));
        registerRecipeSet(MaterialType.REFINED_GLOWSTONE, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_NUGGET);
        registerRecipeSet(MaterialType.REFINED_OBSIDIAN, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_NUGGET);
        registerRecipeSet(MaterialType.STEEL, MekanismTags.Items.INGOTS_STEEL, MekanismItems.STEEL_NUGGET);
        registerVanillaPaxels();
    }

    private void registerRecipeSet(MaterialType material, TagKey<Item> ingot, @Nullable Holder<Item> nugget) {
        String name = material.getSerializedName();
        ArmorCollection armor = material.armor;
        ToolCollection tools = material.tools;
        String baseArmorPath = name + "/armor/";
        armor(HELMET, armor.helmet(), ingot).save(output, MekanismTools.rl(baseArmorPath + "helmet"));
        armor(CHESTPLATE, armor.chestplate(), ingot).save(output, MekanismTools.rl(baseArmorPath + "chestplate"));
        armor(LEGGINGS, armor.leggings(), ingot).save(output, MekanismTools.rl(baseArmorPath + "leggings"));
        armor(BOOTS, armor.boots(), ingot).save(output, MekanismTools.rl(baseArmorPath + "boots"));
        ItemRegistryObject<ShieldItem> shield = material.tools.shield();
        ExtendedShapedRecipeBuilder.shapedRecipe(shield)
              .pattern(SHIELD)
              .key(Pattern.PREVIOUS, this.items, ItemIds.SHIELD)
              .key(Pattern.INGOT, this.items, ingot)
              .category(RecipeCategory.COMBAT)
              .save(output, MekanismTools.rl(name + "/shield"));
        SpecialRecipeBuilder.special(() -> new ShieldDecorationRecipe(
              tag(ItemTags.BANNERS),
              Ingredient.of(shield),
              new ItemStackTemplate(shield)
        )).save(output, name + "/shield_decoration");
        String baseToolsPath = name + "/tools/";
        tool(SWORD, tools.sword(), ingot).category(RecipeCategory.COMBAT).save(output, MekanismTools.rl(baseToolsPath + "sword"));
        tool(SPEAR, tools.spear(), ingot).category(RecipeCategory.COMBAT).save(output, MekanismTools.rl(baseToolsPath + "spear"));
        tool(PICKAXE, tools.pickaxe(), ingot).category(RecipeCategory.TOOLS).save(output, MekanismTools.rl(baseToolsPath + "pickaxe"));
        tool(AXE, tools.axe(), ingot).category(RecipeCategory.TOOLS).save(output, MekanismTools.rl(baseToolsPath + "axe"));
        tool(SHOVEL, tools.shovel(), ingot).category(RecipeCategory.TOOLS).save(output, MekanismTools.rl(baseToolsPath + "shovel"));
        tool(HOE, tools.hoe(), ingot).category(RecipeCategory.TOOLS).save(output, MekanismTools.rl(baseToolsPath + "hoe"));
        //TODO - 1.20.5: Do we care this no longer accepts tools from other mods?
        PaxelShapedRecipeBuilder.shapedRecipe(tools.paxel())
              .pattern(PAXEL)
              .key(AXE_CHAR, tools.axe())
              .key(PICKAXE_CHAR, tools.pickaxe())
              .key(SHOVEL_CHAR, tools.shovel())
              .key(ROD_CHAR, this.items, Tags.Items.RODS_WOODEN)
              .save(output, MekanismTools.rl(baseToolsPath + "paxel"));
        //If we have a nugget that means we also want to add recipes for smelting tools/armor into the nugget
        if (nugget != null) {
            String baseNuggetFrom = name + "/nugget_from_";
            List<Holder<Item>> inputs = new ArrayList<>(armor.asList());
            inputs.addAll(tools.asList());
            RecipeProviderUtil.addSmeltingBlastingRecipes(output, Ingredient.of(HolderSet.direct(inputs)),
                  nugget, 0.1F, 200, MekanismTools.rl(baseNuggetFrom + "blasting"), MekanismTools.rl(baseNuggetFrom + "smelting"));
        }
    }

    private void registerVanillaPaxels() {
        registerVanillaPaxel(ToolsItems.WOOD_PAXEL, ItemIds.WOODEN_AXE, ItemIds.WOODEN_PICKAXE, ItemIds.WOODEN_SHOVEL, null);
        registerVanillaPaxel(ToolsItems.STONE_PAXEL, ItemIds.STONE_AXE, ItemIds.STONE_PICKAXE, ItemIds.STONE_SHOVEL, null);
        registerVanillaPaxel(ToolsItems.COPPER_PAXEL, ItemIds.COPPER_AXE, ItemIds.COPPER_PICKAXE, ItemIds.COPPER_SHOVEL, ItemIds.COPPER_NUGGET);
        registerVanillaPaxel(ToolsItems.IRON_PAXEL, ItemIds.IRON_AXE, ItemIds.IRON_PICKAXE, ItemIds.IRON_SHOVEL, ItemIds.IRON_NUGGET);
        registerVanillaPaxel(ToolsItems.GOLD_PAXEL, ItemIds.GOLDEN_AXE, ItemIds.GOLDEN_PICKAXE, ItemIds.GOLDEN_SHOVEL, ItemIds.GOLD_NUGGET);
        registerVanillaPaxel(ToolsItems.DIAMOND_PAXEL, ItemIds.DIAMOND_AXE, ItemIds.DIAMOND_PICKAXE, ItemIds.DIAMOND_SHOVEL, null);
        ExtendedSmithingRecipeBuilder.smithing(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, ToolsItems.DIAMOND_PAXEL, Items.NETHERITE_INGOT, ToolsItems.NETHERITE_PAXEL).save(output);
    }

    private void registerVanillaPaxel(Holder<Item> paxel, ResourceKey<Item> axe, ResourceKey<Item> pickaxe, ResourceKey<Item> shovel, @Nullable ResourceKey<Item> nugget) {
        PaxelShapedRecipeBuilder.shapedRecipe(paxel)
              .pattern(PAXEL)
              .key(AXE_CHAR, this.items, axe)
              .key(PICKAXE_CHAR, this.items, pickaxe)
              .key(SHOVEL_CHAR, this.items, shovel)
              .key(ROD_CHAR, this.items, Tags.Items.RODS_WOODEN)
              .save(output);
        //If we have a nugget that means we also want to add recipes for smelting tools/armor into the nugget
        if (nugget != null) {
            String baseNuggetFrom = nugget.identifier().getPath() + "_from_";
            RecipeProviderUtil.addSmeltingBlastingRecipes(output, createIngredient(paxel), items.getOrThrow(nugget), 0.1F, 200,
                  MekanismTools.rl(baseNuggetFrom + "blasting"), MekanismTools.rl(baseNuggetFrom + "smelting"));
        }
    }

    private ExtendedShapedRecipeBuilder armor(RecipePattern pattern, Holder<Item> armor, TagKey<Item> ingot) {
        return ExtendedShapedRecipeBuilder.shapedRecipe(armor)
              .pattern(pattern)
              .key(Pattern.INGOT, this.items, ingot)
              .category(RecipeCategory.COMBAT);
    }

    private ExtendedShapedRecipeBuilder tool(RecipePattern pattern, Holder<Item> tool, TagKey<Item> ingot) {
        return ExtendedShapedRecipeBuilder.shapedRecipe(tool)
              .pattern(pattern)
              .key(Pattern.INGOT, this.items, ingot)
              .key(ROD_CHAR, this.items, Tags.Items.RODS_WOODEN);
    }
}