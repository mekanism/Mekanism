package mekanism.additions.common.recipe;

import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.impl.BaseSubRecipeProvider;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class PlasticBlockRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.DYE, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));
    private static final RecipePattern PLASTIC_TRANSPARENT = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.DYE, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    private static final RecipePattern REINFORCED_PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, Pattern.OSMIUM, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));
    private static final RecipePattern PLASTIC_ROAD = RecipePattern.createPattern(
          TripleLine.of(AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR, AdditionsRecipeProvider.SAND_CHAR));
    private static final RecipePattern SLICK_PLASTIC = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
          TripleLine.of(Pattern.CONSTANT, AdditionsRecipeProvider.SLIME_CHAR, Pattern.CONSTANT),
          TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY));

    PlasticBlockRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "plastic/";
        registerPlasticBlocks(consumer, basePath + "block/");
        registerPlasticGlow(consumer, basePath + "glow/");
        registerReinforcedPlastic(consumer, basePath + "reinforced/");
        registerPlasticRoads(consumer, basePath + "road/");
        registerSlickPlastic(consumer, basePath + "slick/");
        registerPlasticTransparent(consumer, basePath + "transparent/");
    }

    private void registerPlasticBlocks(RecipeOutput consumer, String basePath) {
        HolderSet<Item> plasticTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_PLASTIC.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
                      .pattern(PLASTIC)
                      .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
                      .key(Pattern.DYE, this.items, dye.getTag())
                      .category(RecipeCategory.BUILDING_BLOCKS)
                      .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            }
            registerRecolor(consumer, this.items, this.chemicals, result, plasticTag, color, basePath);
        });
    }

    private void registerPlasticTransparent(RecipeOutput consumer, String basePath) {
        HolderSet<Item> transparentTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_TRANSPARENT.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapedRecipeBuilder.shapedRecipe(result, 8)
                      .pattern(PLASTIC_TRANSPARENT)
                      .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
                      .key(Pattern.DYE, this.items, dye.getTag())
                      .category(RecipeCategory.BUILDING_BLOCKS)
                      .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            }
            registerTransparentRecolor(consumer, this.items, this.chemicals, result, transparentTag, color, basePath);
        });
    }

    private void registerPlasticGlow(RecipeOutput consumer, String basePath) {
        HolderSet<Item> glowTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_GLOW.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            ExtendedShapelessRecipeBuilder.shapelessRecipe(result, 3)
                  .addIngredient(AdditionsBlocks.PLASTIC_BLOCKS.pick(color), 3)
                  .addIngredient(this.items, Tags.Items.DUSTS_GLOWSTONE)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            registerRecolor(consumer, this.items, this.chemicals, result, glowTag, color, basePath);
        });
    }

    private void registerReinforcedPlastic(RecipeOutput consumer, String basePath) {
        HolderSet<Item> reinforcedTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_REINFORCED.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
                  .pattern(REINFORCED_PLASTIC)
                  .key(Pattern.OSMIUM, this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.OSMIUM))
                  .key(Pattern.CONSTANT, AdditionsBlocks.PLASTIC_BLOCKS.pick(color))
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            registerRecolor(consumer, this.items, this.chemicals, result, reinforcedTag, color, basePath);
        });
    }

    private void registerPlasticRoads(RecipeOutput consumer, String basePath) {
        HolderSet<Item> roadTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_ROAD.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.PLASTIC_ROADS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            Holder<Item> slickPlastic = AdditionsBlocks.SLICK_PLASTIC_BLOCKS.pick(color).getItemHolder();
            ExtendedShapedRecipeBuilder.shapedRecipe(result, 3)
                  .pattern(PLASTIC_ROAD)
                  .key(AdditionsRecipeProvider.SAND_CHAR, this.items, Tags.Items.SANDS)
                  .key(Pattern.CONSTANT, slickPlastic)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            registerRecolor(consumer, this.items, this.chemicals, result, roadTag, color, basePath);
        });
    }

    private void registerSlickPlastic(RecipeOutput consumer, String basePath) {
        HolderSet<Item> slickTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_SLICK.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.SLICK_PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            Holder<Item> plastic = AdditionsBlocks.PLASTIC_BLOCKS.pick(color).getItemHolder();
            ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
                  .pattern(SLICK_PLASTIC)
                  .key(Pattern.CONSTANT, plastic)
                  .key(AdditionsRecipeProvider.SLIME_CHAR, this.items, Tags.Items.SLIME_BALLS)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            //Enriching recipes
            ItemStackToItemStackRecipeBuilder.enriching(
                  IngredientCreatorAccess.item().fromHolder(plastic),
                  new ItemStackTemplate(result)
            ).save(consumer, MekanismAdditions.rl(basePath + "enriching/" + color.getRegistryPrefix()));
            //Recolor recipes
            registerRecolor(consumer, this.items, this.chemicals, result, slickTag, color, basePath);
        });
    }

    public static void registerRecolor(RecipeOutput consumer, HolderGetter<Item> items, HolderGetter<Chemical> chemicals, Holder<Item> result, HolderSet<Item> blockType,
          EnumColor color, String basePath) {
        Ingredient recolorInput = BaseRecipeProvider.difference(blockType, result);
        String colorString = color.getRegistryPrefix();
        DyeColor dye = color.getDyeColor();
        if (dye != null) {
            ExtendedShapedRecipeBuilder.shapedRecipe(result, 4)
                  .pattern(PLASTIC)
                  .key(Pattern.CONSTANT, recolorInput)
                  .key(Pattern.DYE, items, dye.getTag())
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + "recolor/" + colorString));
        }
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(recolorInput),
              IngredientCreatorAccess.chemicalStack().from(chemicals, MekanismChemicals.SIMPLE_PIGMENTS.pick(color), PigmentExtractingRecipeProvider.DYE_RATE / 4),
              new ItemStackTemplate(result.value()),
              false
        ).save(consumer, MekanismAdditions.rl(basePath + "recolor/painting/" + colorString));
    }

    public static void registerTransparentRecolor(RecipeOutput consumer, HolderGetter<Item> items, HolderGetter<Chemical> chemicals, Holder<Item> result,
          HolderSet<Item> blockType, EnumColor color, String basePath) {
        Ingredient recolorInput = BaseRecipeProvider.difference(blockType, result);
        String colorString = color.getRegistryPrefix();
        DyeColor dye = color.getDyeColor();
        if (dye != null) {
            ExtendedShapedRecipeBuilder.shapedRecipe(result, 8)
                  .pattern(PLASTIC_TRANSPARENT)
                  .key(Pattern.CONSTANT, recolorInput)
                  .key(Pattern.DYE, items, dye.getTag())
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .save(consumer, MekanismAdditions.rl(basePath + "recolor/" + colorString));
        }
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(recolorInput),
              IngredientCreatorAccess.chemicalStack().from(chemicals, MekanismChemicals.SIMPLE_PIGMENTS.pick(color), PigmentExtractingRecipeProvider.DYE_RATE / 8),
              new ItemStackTemplate(result.value()),
              false
        ).save(consumer, MekanismAdditions.rl(basePath + "recolor/painting/" + colorString));
    }
}