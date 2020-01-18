package mekanism.tools.common;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.providers.IItemProvider;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.Criterion;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.Pattern;
import mekanism.common.recipe.RecipePattern;
import mekanism.common.recipe.RecipePattern.DoubleLine;
import mekanism.common.recipe.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

@ParametersAreNonnullByDefault
public class ToolsRecipeProvider extends BaseRecipeProvider {

    private static final char AXE_CHAR = 'A';
    private static final char PICKAXE_CHAR = 'P';
    public static final char ROD_CHAR = 'R';
    private static final char SHOVEL_CHAR = 'S';

    //Armor patterns
    private static final RecipePattern HELMET = RecipePattern.createPattern(TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT));
    private static final RecipePattern CHESTPLATE = RecipePattern.createPattern(TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT), TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT));
    private static final RecipePattern LEGGINGS = RecipePattern.createPattern(TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT), TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT));
    private static final RecipePattern BOOTS = RecipePattern.createPattern(TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
          TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT));
    //Tool Patterns
    private static final RecipePattern AXE = RecipePattern.createPattern(DoubleLine.of(Pattern.INGOT, Pattern.INGOT), DoubleLine.of(Pattern.INGOT, ROD_CHAR),
          DoubleLine.of(Pattern.EMPTY, ROD_CHAR));
    private static final RecipePattern HOE = RecipePattern.createPattern(DoubleLine.of(Pattern.INGOT, Pattern.INGOT), DoubleLine.of(Pattern.EMPTY, ROD_CHAR),
          DoubleLine.of(Pattern.EMPTY, ROD_CHAR));
    private static final RecipePattern PICKAXE = RecipePattern.createPattern(TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
          TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY), TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY));
    private static final RecipePattern SHOVEL = RecipePattern.createPattern(Pattern.INGOT, ROD_CHAR, ROD_CHAR);
    private static final RecipePattern SWORD = RecipePattern.createPattern(Pattern.INGOT, Pattern.INGOT, ROD_CHAR);
    private static final RecipePattern PAXEL = RecipePattern.createPattern(TripleLine.of(AXE_CHAR, PICKAXE_CHAR, SHOVEL_CHAR),
          TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY), TripleLine.of(Pattern.EMPTY, ROD_CHAR, Pattern.EMPTY));

    public ToolsRecipeProvider(DataGenerator gen) {
        super(gen, MekanismTools.MODID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        registerRecipeSet(consumer, "bronze", ToolsItems.BRONZE_HELMET, ToolsItems.BRONZE_CHESTPLATE, ToolsItems.BRONZE_LEGGINGS, ToolsItems.BRONZE_BOOTS,
              ToolsItems.BRONZE_SWORD, ToolsItems.BRONZE_PICKAXE, ToolsItems.BRONZE_AXE, ToolsItems.BRONZE_SHOVEL, ToolsItems.BRONZE_HOE, ToolsItems.BRONZE_PAXEL,
              MekanismTags.Items.INGOTS_BRONZE, Tags.Items.RODS_WOODEN, Criterion.HAS_BRONZE, MekanismItems.BRONZE_NUGGET);
        registerRecipeSet(consumer, "lapis_lazuli", ToolsItems.LAPIS_LAZULI_HELMET, ToolsItems.LAPIS_LAZULI_CHESTPLATE, ToolsItems.LAPIS_LAZULI_LEGGINGS,
              ToolsItems.LAPIS_LAZULI_BOOTS, ToolsItems.LAPIS_LAZULI_SWORD, ToolsItems.LAPIS_LAZULI_PICKAXE, ToolsItems.LAPIS_LAZULI_AXE, ToolsItems.LAPIS_LAZULI_SHOVEL,
              ToolsItems.LAPIS_LAZULI_HOE, ToolsItems.LAPIS_LAZULI_PAXEL, Tags.Items.GEMS_LAPIS, Tags.Items.RODS_WOODEN, Criterion.HAS_LAPIS_LAZULI, null);
        registerRecipeSet(consumer, "osmium", ToolsItems.OSMIUM_HELMET, ToolsItems.OSMIUM_CHESTPLATE, ToolsItems.OSMIUM_LEGGINGS, ToolsItems.OSMIUM_BOOTS,
              ToolsItems.OSMIUM_SWORD, ToolsItems.OSMIUM_PICKAXE, ToolsItems.OSMIUM_AXE, ToolsItems.OSMIUM_SHOVEL, ToolsItems.OSMIUM_HOE, ToolsItems.OSMIUM_PAXEL,
              MekanismTags.Items.INGOTS_OSMIUM, Tags.Items.RODS_WOODEN, Criterion.HAS_OSMIUM, MekanismItems.OSMIUM_NUGGET);
        registerRecipeSet(consumer, "refined_glowstone", ToolsItems.REFINED_GLOWSTONE_HELMET, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE,
              ToolsItems.REFINED_GLOWSTONE_LEGGINGS, ToolsItems.REFINED_GLOWSTONE_BOOTS, ToolsItems.REFINED_GLOWSTONE_SWORD, ToolsItems.REFINED_GLOWSTONE_PICKAXE,
              ToolsItems.REFINED_GLOWSTONE_AXE, ToolsItems.REFINED_GLOWSTONE_SHOVEL, ToolsItems.REFINED_GLOWSTONE_HOE, ToolsItems.REFINED_GLOWSTONE_PAXEL,
              MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, Tags.Items.RODS_WOODEN, Criterion.HAS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_NUGGET);
        registerRecipeSet(consumer, "refined_obsidian", ToolsItems.REFINED_OBSIDIAN_HELMET, ToolsItems.REFINED_OBSIDIAN_CHESTPLATE,
              ToolsItems.REFINED_OBSIDIAN_LEGGINGS, ToolsItems.REFINED_OBSIDIAN_BOOTS, ToolsItems.REFINED_OBSIDIAN_SWORD, ToolsItems.REFINED_OBSIDIAN_PICKAXE,
              ToolsItems.REFINED_OBSIDIAN_AXE, ToolsItems.REFINED_OBSIDIAN_SHOVEL, ToolsItems.REFINED_OBSIDIAN_HOE, ToolsItems.REFINED_OBSIDIAN_PAXEL,
              MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, Tags.Items.RODS_WOODEN, Criterion.HAS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_NUGGET);
        registerRecipeSet(consumer, "steel", ToolsItems.STEEL_HELMET, ToolsItems.STEEL_CHESTPLATE, ToolsItems.STEEL_LEGGINGS, ToolsItems.STEEL_BOOTS,
              ToolsItems.STEEL_SWORD, ToolsItems.STEEL_PICKAXE, ToolsItems.STEEL_AXE, ToolsItems.STEEL_SHOVEL, ToolsItems.STEEL_HOE, ToolsItems.STEEL_PAXEL,
              MekanismTags.Items.INGOTS_STEEL, Tags.Items.INGOTS_IRON, Criterion.HAS_STEEL, MekanismItems.STEEL_NUGGET);
        registerVanillaPaxels(consumer);
    }

    private void registerRecipeSet(Consumer<IFinishedRecipe> consumer, String name, IItemProvider helmet, IItemProvider chestplate, IItemProvider leggings,
          IItemProvider boots, IItemProvider sword, IItemProvider pickaxe, IItemProvider axe, IItemProvider shovel, IItemProvider hoe, IItemProvider paxel,
          Tag<Item> ingot, Tag<Item> rod, RecipeCriterion criterion, @Nullable IItemProvider nugget) {
        String baseArmorPath = name + "/armor/";
        armor(HELMET, helmet, ingot, criterion).build(consumer, MekanismTools.rl(baseArmorPath + "helmet"));
        armor(CHESTPLATE, chestplate, ingot, criterion).build(consumer, MekanismTools.rl(baseArmorPath + "chestplate"));
        armor(LEGGINGS, leggings, ingot, criterion).build(consumer, MekanismTools.rl(baseArmorPath + "leggings"));
        armor(BOOTS, boots, ingot, criterion).build(consumer, MekanismTools.rl(baseArmorPath + "boots"));
        String baseToolsPath = name + "/tools/";
        tool(SWORD, sword, ingot, rod, criterion).build(consumer, MekanismTools.rl(baseToolsPath + "sword"));
        tool(PICKAXE, pickaxe, ingot, rod, criterion).build(consumer, MekanismTools.rl(baseToolsPath + "pickaxe"));
        tool(AXE, axe, ingot, rod, criterion).build(consumer, MekanismTools.rl(baseToolsPath + "axe"));
        tool(SHOVEL, shovel, ingot, rod, criterion).build(consumer, MekanismTools.rl(baseToolsPath + "shovel"));
        tool(HOE, hoe, ingot, rod, criterion).build(consumer, MekanismTools.rl(baseToolsPath + "hoe"));
        RecipeCriterion hasAxe = Criterion.has(axe);
        RecipeCriterion hasPickaxe = Criterion.has(pickaxe);
        RecipeCriterion hasShovel = Criterion.has(shovel);
        ExtendedShapedRecipeBuilder.shapedRecipe(paxel).pattern(PAXEL).key(AXE_CHAR, axe).key(PICKAXE_CHAR, pickaxe).key(SHOVEL_CHAR, shovel).key(ROD_CHAR, rod)
              .addCriterion(hasAxe).addCriterion(hasPickaxe).addCriterion(hasShovel).build(consumer, MekanismTools.rl(baseToolsPath + "paxel"));
        //If we have a nugget that means we also want to add recipes for smelting tools/armor into the nugget
        if (nugget != null) {
            String baseNuggetFrom = name + "/nugget_from_";
            addSmeltingBlastingRecipes(consumer, Ingredient.fromItems(helmet, chestplate, leggings, boots, sword, pickaxe, axe, shovel, hoe, paxel), nugget,
                  0.1F, 200, MekanismTools.rl(baseNuggetFrom + "blasting"), MekanismTools.rl(baseNuggetFrom + "smelting"),
                  Criterion.has(helmet), Criterion.has(chestplate), Criterion.has(leggings), Criterion.has(boots), Criterion.has(sword), hasAxe, hasPickaxe, hasShovel,
                  Criterion.has(hoe), Criterion.has(paxel));
        }
    }

    private void registerVanillaPaxels(Consumer<IFinishedRecipe> consumer) {
        registerVanillaPaxel(consumer, ToolsItems.WOOD_PAXEL, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, null);
        registerVanillaPaxel(consumer, ToolsItems.STONE_PAXEL, Items.STONE_AXE, Items.STONE_PICKAXE, Items.STONE_SHOVEL, null);
        registerVanillaPaxel(consumer, ToolsItems.IRON_PAXEL, Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_NUGGET);
        registerVanillaPaxel(consumer, ToolsItems.GOLD_PAXEL, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLD_NUGGET);
        registerVanillaPaxel(consumer, ToolsItems.DIAMOND_PAXEL, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, null);
    }

    private void registerVanillaPaxel(Consumer<IFinishedRecipe> consumer, IItemProvider paxel, Item axe, Item pickaxe, Item shovel, @Nullable Item nugget) {
        ExtendedShapedRecipeBuilder.shapedRecipe(paxel).pattern(PAXEL).key(AXE_CHAR, axe).key(PICKAXE_CHAR, pickaxe).key(SHOVEL_CHAR, shovel)
              .key(ROD_CHAR, Tags.Items.RODS_WOODEN).addCriterion(Criterion.has(axe)).addCriterion(Criterion.has(pickaxe)).addCriterion(Criterion.has(shovel)).build(consumer);
        //If we have a nugget that means we also want to add recipes for smelting tools/armor into the nugget
        if (nugget != null) {
            String baseNuggetFrom = nugget.getRegistryName().getPath() + "_from_";
            addSmeltingBlastingRecipes(consumer, Ingredient.fromItems(paxel), nugget, 0.1F, 200,
                  MekanismTools.rl(baseNuggetFrom + "blasting"), MekanismTools.rl(baseNuggetFrom + "smelting"), Criterion.has(paxel));
        }
    }

    private ExtendedShapedRecipeBuilder armor(RecipePattern pattern, IItemProvider armor, Tag<Item> ingot, RecipeCriterion criterion) {
        return ExtendedShapedRecipeBuilder.shapedRecipe(armor).pattern(pattern).key(Pattern.INGOT, ingot).addCriterion(criterion);
    }

    private ExtendedShapedRecipeBuilder tool(RecipePattern pattern, IItemProvider tool, Tag<Item> ingot, Tag<Item> rod, RecipeCriterion criterion) {
        return ExtendedShapedRecipeBuilder.shapedRecipe(tool).pattern(pattern).key(Pattern.INGOT, ingot).key(ROD_CHAR, rod).addCriterion(criterion);
    }
}