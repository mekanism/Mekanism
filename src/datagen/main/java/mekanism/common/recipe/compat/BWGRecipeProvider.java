package mekanism.common.recipe.compat;

import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.lib.FieldReflectionHelper;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.potionstudios.biomeswevegone.world.level.block.BWGBlocks;
import net.potionstudios.biomeswevegone.world.level.block.sand.BWGSandSet;
import net.potionstudios.biomeswevegone.world.level.block.set.BWGBlockSet;
import net.potionstudios.biomeswevegone.world.level.block.wood.BWGWoodSet;

@ParametersAreNotNullByDefault
public class BWGRecipeProvider extends CompatRecipeProvider {

    private static final FieldReflectionHelper<BWGWoodSet, String> WOOD_SET_NAME = new FieldReflectionHelper<>(BWGWoodSet.class, "name", () -> null);

    public BWGRecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addDyeRecipes(consumer, basePath);
        addCrushingRecipes(consumer, basePath + "crushing/");
        addEnrichingRecipes(consumer, basePath + "enriching/");
        addMetallurgicInfusingRecipes(consumer, basePath + "metallurgic_infusing/");
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
        //TODO: Bio-fuel recipes?
    }

    private void addPrecisionSawmillRecipes(RecipeOutput consumer, String basePath) {
        for (BWGWoodSet woodType : BWGWoodSet.woodsets()) {
            RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, woodType.planks(), woodType.boatItem().get(),
                  woodType.chestBoatItem().get(), woodType.door(), woodType.fenceGate(), woodType.logItemTag(), woodType.pressurePlate(),
                  woodType.trapdoor(), woodType.hangingSignItem(), WOOD_SET_NAME.getValue(woodType), modLoaded);
        }
    }

    private void addSandRecipes(RecipeOutput consumer, String basePath) {
        for (BWGSandSet sandSet : BWGSandSet.getSandSets()) {
            RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + sandSet.getName(), modLoaded, sandSet.getSand(), sandSet.getSandstoneBlocksItemTag());
        }
    }

    private void addDyeRecipes(RecipeOutput consumer, String basePath) {
        for (EnumColor color : EnumUtils.COLORS) {
            DyeColor dyeColor = color.getDyeColor();
            if (dyeColor != null) {
                dye(consumer, basePath, DyeItem.byColor(dyeColor), false, color);
            }
        }
    }

    private void dye(RecipeOutput consumer, String basePath, ItemLike output, boolean large, EnumColor color) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(Ingredient.of(tag("dye/makes_" + color.getRegistryPrefix() + "_dye")));
        String name = large ? "large_" + color.getRegistryPrefix() : color.getRegistryPrefix();
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, large ? 4 : 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "dye/" + name));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color).asStack(large ? 2 * flowerRate : flowerRate)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/" + name));
    }

    private void addCrushingRecipes(RecipeOutput consumer, String basePath) {
        addCrusherDaciteRecipes(consumer, basePath + "dacite/");
        addCrusherRedRockRecipes(consumer, basePath + "red_rock/");
    }

    private void addCrusherDaciteRecipes(RecipeOutput consumer, String basePath) {
        //Dacite -> Dacite Cobblestone
        crushing(consumer, basePath, BWGBlocks.DACITE_SET, BWGBlocks.DACITE_COBBLESTONE_SET);
        //Dacite Cobblestone -> Dacite Tile
        crushing(consumer, basePath, BWGBlocks.DACITE_COBBLESTONE_SET, BWGBlocks.DACITE_TILE_SET);
        //Dacite Tile -> Dacite Bricks
        crushing(consumer, basePath, BWGBlocks.DACITE_TILE_SET, BWGBlocks.DACITE_BRICKS_SET);
        //Dacite Bricks -> Dacite
        crushing(consumer, basePath, BWGBlocks.DACITE_BRICKS_SET, BWGBlocks.DACITE_SET);
        //Dacite Pillar -> Dacite
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(BWGBlocks.DACITE_PILLAR.get()),
                    new ItemStack(BWGBlocks.DACITE_SET.getBase(), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherRedRockRecipes(RecipeOutput consumer, String basePath) {
        //Chiseled Red Rock -> Red Rock Bricks
        crushing(consumer, basePath, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET);
        //Red Rock Bricks -> Cracked Red Rock Bricks
        crushing(consumer, basePath, BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET);
        //Cracked Red Rock Bricks -> Red Rock
        crushing(consumer, basePath, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_SET);
        //Red Rock -> Chiseled Red Rock Bricks
        crushing(consumer, basePath, BWGBlocks.RED_ROCK_SET, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET);
    }

    private void crushing(RecipeOutput consumer, String basePath, BWGBlockSet from, BWGBlockSet to) {
        String name = BuiltInRegistries.BLOCK.getKey(from.getBase()).getPath();
        crushing(consumer, from.getBase(), to.getBase(), basePath + "conversion_" + name);
        crushing(consumer, from.getSlab(), to.getSlab(), basePath + "slabs_conversion_" + name);
        crushing(consumer, from.getStairs(), to.getStairs(), basePath + "stairs_conversion_" + name);
        crushing(consumer, from.getWall(), to.getWall(), basePath + "walls_conversion_" + name);
    }

    private void crushing(RecipeOutput consumer, ItemLike input, ItemLike output, String path) {
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(input),
                    new ItemStack(output)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(path));
    }

    private void addEnrichingRecipes(RecipeOutput consumer, String basePath) {
        addMossyStoneEnrichingRecipes(consumer, basePath + "mossy_stone/");
        addDaciteEnrichingRecipes(consumer, basePath + "dacite/");
        addRedRockEnrichingRecipes(consumer, basePath + "red_rock/");
    }

    private void addMossyStoneEnrichingRecipes(RecipeOutput consumer, String basePath) {
        BWGBlockSet from = BWGBlocks.MOSSY_STONE_SET;
        String name = BuiltInRegistries.BLOCK.getKey(from.getBase()).getPath();
        enriching(consumer, from.getBase(), Items.STONE, basePath + "conversion_" + name);
        enriching(consumer, from.getSlab(), Items.STONE_SLAB, basePath + "slabs_conversion_" + name);
        enriching(consumer, from.getStairs(), Items.STONE_STAIRS, basePath + "stairs_conversion_" + name);
        //enriching(consumer, from.getWall(), Items.STONE_WALL, basePath + "walls_conversion_" + name);
    }

    private void addDaciteEnrichingRecipes(RecipeOutput consumer, String basePath) {
        //Dacite Cobble -> Dacite
        enriching(consumer, basePath, BWGBlocks.DACITE_COBBLESTONE_SET, BWGBlocks.DACITE_SET);
        //Dacite -> Dacite Bricks
        enriching(consumer, basePath, BWGBlocks.DACITE_SET, BWGBlocks.DACITE_BRICKS_SET);
        //Dacite Bricks -> Dacite Tile
        enriching(consumer, basePath, BWGBlocks.DACITE_BRICKS_SET, BWGBlocks.DACITE_TILE_SET);
        //Dacite Tile -> Dacite Cobble
        enriching(consumer, basePath, BWGBlocks.DACITE_TILE_SET, BWGBlocks.DACITE_COBBLESTONE_SET);
    }

    private void addRedRockEnrichingRecipes(RecipeOutput consumer, String basePath) {
        //Red Rock -> Cracked Red Rock Bricks
        enriching(consumer, basePath, BWGBlocks.RED_ROCK_SET, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET);
        //Cracked Red Rock Bricks -> Red Rock Bricks
        enriching(consumer, basePath, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET);
        //Red Rock Bricks -> Chiseled Red Rock
        enriching(consumer, basePath, BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET);
        //Chiseled Red Rock -> Red Rock
        enriching(consumer, basePath, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_SET);
        //Mossy Red Rock Bricks -> Red Rock Bricks
        enriching(consumer, basePath, BWGBlocks.MOSSY_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET);
    }

    private void enriching(RecipeOutput consumer, String basePath, BWGBlockSet from, BWGBlockSet to) {
        String name = BuiltInRegistries.BLOCK.getKey(from.getBase()).getPath();
        enriching(consumer, from.getBase(), to.getBase(), basePath + "conversion_" + name);
        enriching(consumer, from.getSlab(), to.getSlab(), basePath + "slabs_conversion_" + name);
        enriching(consumer, from.getStairs(), to.getStairs(), basePath + "stairs_conversion_" + name);
        enriching(consumer, from.getWall(), to.getWall(), basePath + "walls_conversion_" + name);
    }

    private void enriching(RecipeOutput consumer, ItemLike input, ItemLike output, String path) {
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(input),
                    new ItemStack(output)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(path));
    }

    private void addMetallurgicInfusingRecipes(RecipeOutput consumer, String basePath) {
        addMossyStoneInfusingRecipes(consumer, basePath + "mossy_stone/");
        infuseMoss(consumer, basePath + "red_rock/", BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.MOSSY_RED_ROCK_BRICKS_SET);
    }

    private void addMossyStoneInfusingRecipes(RecipeOutput consumer, String basePath) {
        String name = "stone";
        BWGBlockSet to = BWGBlocks.MOSSY_STONE_SET;
        infuseMoss(consumer, Items.STONE, to.getBase(), basePath + "conversion_" + name);
        infuseMoss(consumer, Items.STONE_SLAB, to.getSlab(), basePath + "slabs_conversion_" + name);
        infuseMoss(consumer, Items.STONE_STAIRS, to.getStairs(), basePath + "stairs_conversion_" + name);
        //infuseMoss(consumer, Items.STONE_WALL, to.getWall(), basePath + "walls_conversion_" + name);
    }

    private void infuseMoss(RecipeOutput consumer, String basePath, BWGBlockSet from, BWGBlockSet to) {
        String name = BuiltInRegistries.BLOCK.getKey(from.getBase()).getPath();
        infuseMoss(consumer, from.getBase(), to.getBase(), basePath + "conversion_" + name);
        infuseMoss(consumer, from.getSlab(), to.getSlab(), basePath + "slabs_conversion_" + name);
        infuseMoss(consumer, from.getStairs(), to.getStairs(), basePath + "stairs_conversion_" + name);
        infuseMoss(consumer, from.getWall(), to.getWall(), basePath + "walls_conversion_" + name);
    }

    private void infuseMoss(RecipeOutput consumer, ItemLike input, ItemLike output, String path) {
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
                    IngredientCreatorAccess.item().from(input),
                    IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
                    new ItemStack(output),
                    false
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(path));
    }
}