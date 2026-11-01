package mekanism.common.recipe.compat;

import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.util.RegistryUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.potionstudios.biomeswevegone.tags.BWGItemTags;
import net.potionstudios.biomeswevegone.world.level.block.BWGBlocks;
import net.potionstudios.biomeswevegone.world.level.block.sand.BWGSandSet;
import net.potionstudios.biomeswevegone.world.level.block.set.BWGBlockSet;
import net.potionstudios.biomeswevegone.world.level.block.wood.BWGWoodSet;

public class BWGRecipeProvider extends CompatRecipeProvider {

    public BWGRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput, String modid) {
        super(recipeOutput, advancementOutput, modid);
    }

    @Override
    protected void addRecipes() {
        String basePath = getBasePath();
        addDyeRecipes(basePath);
        addCrushingRecipes(basePath + "crushing/");
        addEnrichingRecipes(basePath + "enriching/");
        addMetallurgicInfusingRecipes(basePath + "metallurgic_infusing/");
        addPrecisionSawmillRecipes(basePath + "sawing/");
        addSandRecipes(basePath + "sandstone_to_sand/");
        //TODO: Bio-fuel recipes?
    }

    private void addPrecisionSawmillRecipes(String basePath) {
        for (BWGWoodSet woodType : BWGWoodSet.woodsets()) {
            RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(this.output, this.items, basePath, woodType.planks().asItem(), woodType.boatItem().get(),
                  woodType.chestBoatItem().get(), woodType.door().asItem(), woodType.fenceGate().asItem(), woodType.logItemTag(), woodType.pressurePlate().asItem(),
                  woodType.trapdoor().asItem(), woodType.hangingSignItem(), woodType.strippedLogStem().asItem(), woodType.shelf().asItem(), woodType.name());
        }
    }

    private void addSandRecipes(String basePath) {
        for (BWGSandSet sandSet : BWGSandSet.getSandSets()) {
            ItemStackToItemStackRecipeBuilder.crushing(
                  IngredientCreatorAccess.item().from(items, sandSet.getSandstoneBlocksItemTag()),
                  new ItemStackTemplate(sandSet.getSand().asItem(), 2)
            ).category(CrushingBookCategory.BUILDING)
                  .unlockedBy("has_sandstone", has(sandSet.getSandstoneBlocksItemTag()))
                  .save(this.output, basePath + sandSet.getName());
        }
    }

    private void addDyeRecipes(String basePath) {
        for (EnumColor color : EnumColor.VALUES) {
            DyeColor dyeColor = color.getDyeColor();
            if (dyeColor != null) {
                ResourceKey<Item> dye = ItemIds.DYE.pick(dyeColor);
                dye(basePath, dye, false, color);
                dye(basePath, dye, true, color);
            }
        }
    }

    private void dye(String basePath, ResourceKey<Item> output, boolean large, EnumColor color) {
        String name = color.getRegistryPrefix();
        String makeTarget = name;
        if (large) {
            makeTarget = "2_" + makeTarget;
            name = "large_" + name;
        }
        //TODO: BWGItemTags?
        TagKey<Item> makesDyeTag = tag("dye/makes_" + makeTarget + "_dye");
        ICondition tagNotEmpty = new NotCondition(new TagEmptyCondition<>(makesDyeTag));
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(this.items, makesDyeTag);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    template(output, large ? 4 : 2)
              ).category(EnrichingBookCategory.DECORATIVE)
              .group(output)
              .unlockedBy("has_flowers", has(makesDyeTag))
              .addCondition(tagNotEmpty)
              .save(this.output, basePath + "dye/" + name);
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        int flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    chemicalTemplate(ChemicalIds.SIMPLE_PIGMENTS.pick(color), large ? 2 * flowerRate : flowerRate)
              ).group(output)
              .unlockedBy("has_flowers", has(makesDyeTag))
              .addCondition(tagNotEmpty)
              .save(this.output, basePath + "pigment_extracting/" + name);
    }

    private void addCrushingRecipes(String basePath) {
        addCrusherDaciteRecipes(basePath + "dacite/");
        addCrusherWhiteDaciteRecipes(basePath + "white_dacite/");
        addCrusherRedRockRecipes(basePath + "red_rock/");
    }

    private void addCrusherDaciteRecipes(String basePath) {
        //Dacite -> Dacite Cobblestone
        crushing(basePath, BWGBlocks.DACITE_SET, BWGBlocks.DACITE_COBBLESTONE_SET, "dacite_conversion");
        //Dacite Cobblestone -> Dacite Tile
        crushing(basePath, BWGBlocks.DACITE_COBBLESTONE_SET, BWGBlocks.DACITE_TILES_SET, "dacite_cobblestone_conversion");
        //Dacite Tile -> Chiseled Dacite Bricks
        crushing(basePath, BWGBlocks.DACITE_TILES_SET, BWGBlocks.CHISELED_DACITE_BRICKS_SET, "dacite_tile_conversion");
        //Chiseled Dacite Bricks -> Dacite Bricks
        crushing(basePath, BWGBlocks.CHISELED_DACITE_BRICKS_SET, BWGBlocks.DACITE_BRICKS_SET, "chiseled_dacite_brick_conversion");
        //Dacite Bricks -> Cracked Dacite Bricks
        crushing(basePath, BWGBlocks.DACITE_BRICKS_SET, BWGBlocks.CRACKED_DACITE_BRICKS_SET, "dacite_brick_conversion");
        //Dacite Bricks -> Dacite
        crushing(basePath, BWGBlocks.CRACKED_DACITE_BRICKS_SET, BWGBlocks.DACITE_SET, "cracked_dacite_conversion");
        //Dacite Pillar -> Dacite
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(BWGBlocks.DACITE_PILLAR.get()),
                    new ItemStackTemplate(BWGBlocks.DACITE_SET.getBase().asItem(), 2)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BWGBlocks.DACITE_PILLAR.get().asItem()))
              .save(this.output, basePath + "from_dacite_pillar");
    }

    private void addCrusherWhiteDaciteRecipes(String basePath) {
        //White Dacite -> White Dacite Cobblestone
        crushing(basePath, BWGBlocks.WHITE_DACITE_SET, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, "white_dacite_conversion");
        //White Dacite Cobblestone -> White Dacite Tile
        crushing(basePath, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, BWGBlocks.WHITE_DACITE_TILES_SET, "white_dacite_cobblestone_conversion");
        //White Dacite Tile -> Chiseled White Dacite Bricks
        crushing(basePath, BWGBlocks.WHITE_DACITE_TILES_SET, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, "white_dacite_tile_conversion");
        //Chiseled White Dacite Bricks -> White Dacite Bricks
        crushing(basePath, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_BRICKS_SET, "white_chiseled_dacite_brick_conversion");
        //White Dacite Bricks -> Cracked White Dacite Bricks
        crushing(basePath, BWGBlocks.WHITE_DACITE_BRICKS_SET, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, "white_dacite_brick_conversion");
        //White Dacite Bricks -> White Dacite
        crushing(basePath, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_SET, "white_cracked_dacite_conversion");
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(BWGBlocks.WHITE_DACITE_PILLAR.get()),
                    new ItemStackTemplate(BWGBlocks.WHITE_DACITE_SET.getBase().asItem(), 2)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BWGBlocks.WHITE_DACITE_PILLAR.get().asItem()))
              .save(this.output, basePath + "from_white_dacite_pillar");
    }

    private void addCrusherRedRockRecipes(String basePath) {
        //Red Rock Tile -> Chiseled Red Rock Bricks
        crushing(basePath, BWGBlocks.RED_ROCK_TILES_SET, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, "red_rock_tile_conversion");
        //Chiseled Red Rock -> Red Rock Bricks
        crushing(basePath, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET, "chiseled_red_rock_brick_conversion");
        //Red Rock Bricks -> Cracked Red Rock Bricks
        crushing(basePath, BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, "red_rock_brick_conversion");
        //Cracked Red Rock Bricks -> Polished Red Rock
        crushing(basePath, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, BWGBlocks.POLISHED_RED_ROCK_SET, "cracked_red_rock_conversion");
        //Polished Red Rock -> Red Rock
        crushing(basePath, BWGBlocks.POLISHED_RED_ROCK_SET, BWGBlocks.RED_ROCK_SET, "polished_red_rock_conversion");
        //Red Rock -> Red Rock Tiles
        crushing(basePath, BWGBlocks.RED_ROCK_SET, BWGBlocks.RED_ROCK_TILES_SET, "red_rock_conversion");
    }

    private void crushing(String basePath, BWGBlockSet from, BWGBlockSet to, String group) {
        String name = RegistryUtils.getPath(from.getBase());
        crushing(from.getBase(), to.getBase(), group, basePath + "conversion_" + name);
        crushing(from.getSlab(), to.getSlab(), group, basePath + "slabs_conversion_" + name);
        crushing(from.getStairs(), to.getStairs(), group, basePath + "stairs_conversion_" + name);
        crushing(from.getWall(), to.getWall(), group, basePath + "walls_conversion_" + name);
    }

    private void crushing(ItemLike input, ItemLike output, String group, String path) {
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(input),
                    new ItemStackTemplate(output.asItem())
              ).category(CrushingBookCategory.BUILDING)
              .group(Mekanism.rl(group))
              .unlockedBy(hasCriterion(input.asItem()))
              .save(this.output, path);
    }

    private void addEnrichingRecipes(String basePath) {
        addMossyStoneEnrichingRecipes(basePath + "mossy_stone/");
        addDaciteEnrichingRecipes(basePath + "dacite/");
        addWhiteDaciteEnrichingRecipes(basePath + "white_dacite/");
        addRedRockEnrichingRecipes(basePath + "red_rock/");
    }

    private void addMossyStoneEnrichingRecipes(String basePath) {
        BWGBlockSet from = BWGBlocks.MOSSY_STONE_SET;
        String name = RegistryUtils.getPath(from.getBase());
        enriching(from.getBase(), Items.STONE, "mossy_stone_removal", basePath + "conversion_" + name);
        enriching(from.getSlab(), Items.STONE_SLAB, "mossy_stone_removal", basePath + "slabs_conversion_" + name);
        enriching(from.getStairs(), Items.STONE_STAIRS, "mossy_stone_removal", basePath + "stairs_conversion_" + name);
        //enriching(consumer, from.getWall(), Items.STONE_WALL, "mossy_stone_removal", basePath + "walls_conversion_" + name);
    }

    private void addDaciteEnrichingRecipes(String basePath) {
        //Dacite Cobble -> Dacite
        enriching(basePath, BWGBlocks.DACITE_COBBLESTONE_SET, BWGBlocks.DACITE_SET, "dacite_cobblestone_conversion");
        //Dacite -> Cracked Dacite Bricks
        enriching(basePath, BWGBlocks.DACITE_SET, BWGBlocks.CRACKED_DACITE_BRICKS_SET, "dacite_conversion");
        //Cracked Dacite Bricks -> Dacite Bricks
        enriching(basePath, BWGBlocks.CRACKED_DACITE_BRICKS_SET, BWGBlocks.DACITE_BRICKS_SET, "cracked_dacite_conversion");
        //Dacite Bricks -> Chiseled Dacite Bricks
        enriching(basePath, BWGBlocks.DACITE_BRICKS_SET, BWGBlocks.CHISELED_DACITE_BRICKS_SET, "dacite_brick_conversion");
        //Chiseled Dacite Bricks -> Dacite Tile
        enriching(basePath, BWGBlocks.CHISELED_DACITE_BRICKS_SET, BWGBlocks.DACITE_TILES_SET, "chiseled_dacite_brick_conversion");
        //Dacite Tile -> Dacite Cobble
        enriching(basePath, BWGBlocks.DACITE_TILES_SET, BWGBlocks.DACITE_COBBLESTONE_SET, "dacite_tile_conversion");
    }

    private void addWhiteDaciteEnrichingRecipes(String basePath) {
        //White Dacite Cobble -> White Dacite
        enriching(basePath, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, BWGBlocks.WHITE_DACITE_SET, "white_dacite_cobblestone_conversion");
        //White Dacite -> White Cracked Dacite Bricks
        enriching(basePath, BWGBlocks.WHITE_DACITE_SET, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, "white_dacite_conversion");
        //Cracked White Dacite Bricks -> White Dacite Bricks
        enriching(basePath, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_BRICKS_SET, "white_cracked_dacite_conversion");
        //White Dacite Bricks -> Chiseled White Dacite Bricks
        enriching(basePath, BWGBlocks.WHITE_DACITE_BRICKS_SET, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, "white_dacite_brick_conversion");
        //Chiseled White Dacite Bricks -> White Dacite Tile
        enriching(basePath, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_TILES_SET, "white_chiseled_dacite_brick_conversion");
        //White Dacite Tile -> White Dacite Cobble
        enriching(basePath, BWGBlocks.WHITE_DACITE_TILES_SET, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, "white_dacite_tile_conversion");
    }

    private void addRedRockEnrichingRecipes(String basePath) {
        //Red Rock -> Polished Red Rock
        enriching(basePath, BWGBlocks.RED_ROCK_SET, BWGBlocks.POLISHED_RED_ROCK_SET, "red_rock_conversion");
        //Polished Red Rock -> Cracked Red Rock
        enriching(basePath, BWGBlocks.POLISHED_RED_ROCK_SET, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, "polished_red_rock_conversion");
        //Cracked Red Rock Bricks -> Red Rock Bricks
        enriching(basePath, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET, "cracked_red_rock_conversion");
        //Red Rock Bricks -> Chiseled Red Rock
        enriching(basePath, BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, "red_rock_brick_conversion");
        //Chiseled Red Rock -> Red Rock Tiles
        enriching(basePath, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_TILES_SET, "chiseled_red_rock_brick_conversion");
        //Red Rock Tiles -> Red Rock
        enriching(basePath, BWGBlocks.RED_ROCK_TILES_SET, BWGBlocks.RED_ROCK_SET, "red_rock_tile_conversion");

        //Mossy Red Rock Bricks -> Red Rock Bricks
        enriching(basePath, BWGBlocks.MOSSY_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET, "mossy_red_rock_removal");
    }

    private void enriching(String basePath, BWGBlockSet from, BWGBlockSet to, String group) {
        String name = RegistryUtils.getPath(from.getBase());
        enriching(from.getBase(), to.getBase(), group, basePath + "conversion_" + name);
        enriching(from.getSlab(), to.getSlab(), group, basePath + "slabs_conversion_" + name);
        enriching(from.getStairs(), to.getStairs(), group, basePath + "stairs_conversion_" + name);
        enriching(from.getWall(), to.getWall(), group, basePath + "walls_conversion_" + name);
    }

    private void enriching(ItemLike input, ItemLike output, String group, String path) {
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(input),
                    new ItemStackTemplate(output.asItem())
              ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl(group))
              .unlockedBy(hasCriterion(input.asItem()))
              .save(this.output, path);
    }

    private void addMetallurgicInfusingRecipes(String basePath) {
        addMossyStoneInfusingRecipes(basePath + "mossy_stone/");
        infuseMoss(basePath + "red_rock/", BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.MOSSY_RED_ROCK_BRICKS_SET, "mossy_red_rock_addition");
    }

    private void addMossyStoneInfusingRecipes(String basePath) {
        String name = "stone";
        BWGBlockSet to = BWGBlocks.MOSSY_STONE_SET;
        infuseMoss(Items.STONE, to.getBase(), "mossy_stone_addition", basePath + "conversion_" + name);
        infuseMoss(Items.STONE_SLAB, to.getSlab(), "mossy_stone_addition", basePath + "slabs_conversion_" + name);
        infuseMoss(Items.STONE_STAIRS, to.getStairs(), "mossy_stone_addition", basePath + "stairs_conversion_" + name);
        //infuseMoss(Items.STONE_WALL, to.getWall(), "mossy_stone_addition", basePath + "walls_conversion_" + name);
    }

    private void infuseMoss(String basePath, BWGBlockSet from, BWGBlockSet to, String group) {
        String name = RegistryUtils.getPath(from.getBase());
        infuseMoss(from.getBase(), to.getBase(), group, basePath + "conversion_" + name);
        infuseMoss(from.getSlab(), to.getSlab(), group, basePath + "slabs_conversion_" + name);
        infuseMoss(from.getStairs(), to.getStairs(), group, basePath + "stairs_conversion_" + name);
        infuseMoss(from.getWall(), to.getWall(), group, basePath + "walls_conversion_" + name);
    }

    private void infuseMoss(ItemLike input, ItemLike output, String group, String path) {
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
                    IngredientCreatorAccess.item().from(input),
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
                    false,
                    new ItemStackTemplate(output.asItem())
              ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl(group))
              .unlockedBy(hasCriterion(input.asItem()))
              .save(this.output, path);
    }
}
