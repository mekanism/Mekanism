package mekanism.additions.common.recipe;

import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.block.plastic.BlockPlastic;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.impl.BaseSubRecipeProvider;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.BlockItem;
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
    public void addRecipes(RecipeOutput output) {
        String basePath = "plastic/";
        registerPlasticBlocks(output, basePath + "block/");
        registerPlasticGlow(output, basePath + "glow/");
        registerReinforcedPlastic(output, basePath + "reinforced/");
        registerPlasticRoads(output, basePath + "road/");
        registerSlickPlastic(output, basePath + "slick/");
        registerPlasticTransparent(output, basePath + "transparent/");
    }

    private void registerPlasticBlocks(RecipeOutput output, String basePath) {
        HolderSet.Named<Item> plasticTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_PLASTIC.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapedRecipeBuilder.shaped(result, 4)
                      .pattern(PLASTIC)
                      .define(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
                      .define(Pattern.DYE, this.items, dye.getTag())
                      .category(RecipeCategory.BUILDING_BLOCKS)
                      .group(MekanismAdditions.rl("plastic"))
                      .unlockedBy("has_plastic", has(MekanismItems.HDPE_PELLET))
                      .unlockedBy("has_plastic_sheet", has(MekanismItems.HDPE_SHEET))
                      .save(output, basePath + color.getRegistryPrefix());
            }
            registerRecolor(output, this.items, this.chemicals, result, plasticTag, color, basePath, "plastic_recolor");
        });
    }

    private void registerPlasticTransparent(RecipeOutput output, String basePath) {
        HolderSet.Named<Item> transparentTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_TRANSPARENT.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapedRecipeBuilder.shaped(result, 8)
                      .pattern(PLASTIC_TRANSPARENT)
                      .define(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
                      .define(Pattern.DYE, this.items, dye.getTag())
                      .category(RecipeCategory.BUILDING_BLOCKS)
                      .group(MekanismAdditions.rl("transparent_plastic"))
                      .unlockedBy("has_plastic", has(MekanismItems.HDPE_PELLET))
                      .unlockedBy("has_plastic_sheet", has(MekanismItems.HDPE_SHEET))
                      .save(output, basePath + color.getRegistryPrefix());
            }
            registerTransparentRecolor(output, this.items, this.chemicals, result, transparentTag, color, basePath, "transparent_plastic_recolor");
        });
    }

    private void registerPlasticGlow(RecipeOutput output, String basePath) {
        HolderSet.Named<Item> glowTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_GLOW.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            ExtendedShapelessRecipeBuilder.shapeless(result, 3)
                  .requires(AdditionsBlocks.PLASTIC_BLOCKS.pick(color), 3)
                  .requires(this.items, Tags.Items.DUSTS_GLOWSTONE)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl("glow_plastic"))
                  .unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
                  .save(output, basePath + color.getRegistryPrefix());
            registerRecolor(output, this.items, this.chemicals, result, glowTag, color, basePath, "glow_plastic_recolor");
        });
    }

    private void registerReinforcedPlastic(RecipeOutput output, String basePath) {
        HolderSet.Named<Item> reinforcedTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_REINFORCED.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            BlockRegistryObject<BlockPlastic, BlockItem> plastic = AdditionsBlocks.PLASTIC_BLOCKS.pick(color);
            ExtendedShapedRecipeBuilder.shaped(result, 4)
                  .pattern(REINFORCED_PLASTIC)
                  .define(Pattern.OSMIUM, this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.OSMIUM))
                  .define(Pattern.CONSTANT, plastic)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl("reinforced_plastic"))
                  .unlockedBy("has_plastic", has(plastic))
                  .save(output, basePath + color.getRegistryPrefix());
            registerRecolor(output, this.items, this.chemicals, result, reinforcedTag, color, basePath, "reinforced_plastic_recolor");
        });
    }

    private void registerPlasticRoads(RecipeOutput output, String basePath) {
        HolderSet.Named<Item> roadTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_ROAD.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.PLASTIC_ROADS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            Holder<Item> slickPlastic = AdditionsBlocks.SLICK_PLASTIC_BLOCKS.pick(color).getItemHolder();
            ExtendedShapedRecipeBuilder.shaped(result, 3)
                  .pattern(PLASTIC_ROAD)
                  .define(AdditionsRecipeProvider.SAND_CHAR, this.items, Tags.BlockItems.SANDS)
                  .define(Pattern.CONSTANT, slickPlastic)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl("plastic_road"))
                  .unlockedBy("has_plastic", has(slickPlastic))
                  .save(output, basePath + color.getRegistryPrefix());
            registerRecolor(output, this.items, this.chemicals, result, roadTag, color, basePath, "plastic_road_recolor");
        });
    }

    private void registerSlickPlastic(RecipeOutput output, String basePath) {
        HolderSet.Named<Item> slickTag = this.items.getOrThrow(AdditionsTags.BlockItems.PLASTIC_BLOCKS_SLICK.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.SLICK_PLASTIC_BLOCKS, (color, block) -> {
            Holder<Item> result = block.getItemHolder();
            Holder<Item> plastic = AdditionsBlocks.PLASTIC_BLOCKS.pick(color).getItemHolder();
            ExtendedShapedRecipeBuilder.shaped(result, 4)
                  .pattern(SLICK_PLASTIC)
                  .define(Pattern.CONSTANT, plastic)
                  .define(AdditionsRecipeProvider.SLIME_CHAR, this.items, Tags.Items.SLIME_BALLS)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl("slick_plastic"))
                  .unlockedBy("has_plastic", has(plastic))
                  .save(output, basePath + color.getRegistryPrefix());
            //Enriching recipes
            ItemStackToItemStackRecipeBuilder.enriching(
                  IngredientCreatorAccess.item().fromHolder(plastic),
                  new ItemStackTemplate(result)
            ).category(EnrichingBookCategory.BUILDING)
                  .group(MekanismAdditions.rl("slick_plastic_enriching"))
                  .unlockedBy("has_plastic", has(plastic))
                  .save(output, basePath + "enriching/" + color.getRegistryPrefix());
            //Recolor recipes
            registerRecolor(output, this.items, this.chemicals, result, slickTag, color, basePath, "slick_plastic_recolor");
        });
    }

    public static void registerRecolor(RecipeOutput output, HolderGetter<Item> items, HolderGetter<Chemical> chemicals, Holder<Item> result, HolderSet.Named<Item> blockType,
          EnumColor color, String basePath, String groupName) {
        Ingredient recolorInput = BaseRecipeProvider.difference(blockType, result);
        String colorString = color.getRegistryPrefix();
        DyeColor dye = color.getDyeColor();
        if (dye != null) {
            ExtendedShapedRecipeBuilder.shaped(result, 4)
                  .pattern(PLASTIC)
                  .define(Pattern.CONSTANT, recolorInput)
                  .define(Pattern.DYE, items, dye.getTag())
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl(groupName))
                  .unlockedBy("has_needed_dye", RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(items, dye.getTag())))
                  .save(output, basePath + "recolor/" + colorString);
        }
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(recolorInput),
              IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(color), PigmentExtractingRecipeProvider.DYE_RATE / 4),
              false,
              new ItemStackTemplate(result.value())
        ).group(MekanismAdditions.rl(groupName.replace("recolor", "painting")))
              .unlockedBy("has_needed_dye", RecipeProviderUtil.has(items, dye == null ? blockType.key() : dye.getTag()))
              .save(output, basePath + "recolor/painting/" + colorString);
    }

    public static void registerTransparentRecolor(RecipeOutput output, HolderGetter<Item> items, HolderGetter<Chemical> chemicals, Holder<Item> result,
          HolderSet.Named<Item> blockType, EnumColor color, String basePath, String groupName) {
        Ingredient recolorInput = BaseRecipeProvider.difference(blockType, result);
        String colorString = color.getRegistryPrefix();
        DyeColor dye = color.getDyeColor();
        if (dye != null) {
            ExtendedShapedRecipeBuilder.shaped(result, 8)
                  .pattern(PLASTIC_TRANSPARENT)
                  .define(Pattern.CONSTANT, recolorInput)
                  .define(Pattern.DYE, items, dye.getTag())
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .group(MekanismAdditions.rl(groupName))
                  .unlockedBy("has_needed_dye", RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(items, dye.getTag())))
                  .save(output, basePath + "recolor/" + colorString);
        }
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              IngredientCreatorAccess.item().from(recolorInput),
              IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(color), PigmentExtractingRecipeProvider.DYE_RATE / 8),
              false,
              new ItemStackTemplate(result.value())
        ).group(MekanismAdditions.rl(groupName.replace("recolor", "painting")))
              .unlockedBy("has_needed_dye", RecipeProviderUtil.has(items, dye == null ? blockType.key() : dye.getTag()))
              .save(output, basePath + "recolor/painting/" + colorString);
    }
}