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
import mekanism.common.recipe.condition.ModVersionLoadedCondition;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.potionstudios.biomeswevegone.world.level.block.BWGBlocks;
import net.potionstudios.biomeswevegone.world.level.block.sand.BWGSandSet;
import net.potionstudios.biomeswevegone.world.level.block.set.BWGBlockSet;
import net.potionstudios.biomeswevegone.world.level.block.wood.BWGWoodSet;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class BWGRecipeProvider extends CompatRecipeProvider {

    private static final FieldReflectionHelper<BWGWoodSet, String> WOOD_SET_NAME = new FieldReflectionHelper<>(BWGWoodSet.class, "name", () -> null);
    private final ICondition villageUpdate;

    public BWGRecipeProvider(String modid) {
        super(modid);
        //TODO - 1.21.8: Replace this with just the mod loaded condition
        villageUpdate = new ModVersionLoadedCondition(modid, "2.4.0");
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
                Item dye = DyeItem.byColor(dyeColor);
                dye(consumer, basePath, dye, false, color);
                dye(consumer, basePath, dye, true, color);
            }
        }
    }

    private void dye(RecipeOutput consumer, String basePath, ItemLike output, boolean large, EnumColor color) {
        String name = color.getRegistryPrefix();
        String makeTarget = name;
        if (large) {
            makeTarget = "2_" + makeTarget;
            name = "large_" + name;
        }
        TagKey<Item> makesDyeTag = tag("dye/makes_" + makeTarget + "_dye");
        ICondition tagNotEmpty = new NotCondition(new TagEmptyCondition(makesDyeTag));
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(makesDyeTag);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, large ? 4 : 2)
              ).addCondition(modLoaded)
              .addCondition(tagNotEmpty)
              .build(consumer, Mekanism.rl(basePath + "dye/" + name));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color).asStack(large ? 2 * flowerRate : flowerRate)
              ).addCondition(modLoaded)
              .addCondition(tagNotEmpty)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/" + name));
    }

    private void addCrushingRecipes(RecipeOutput consumer, String basePath) {
        addCrusherDaciteRecipes(consumer, basePath + "dacite/");
        addCrusherWhiteDaciteRecipes(consumer, basePath + "white_dacite/");
        addCrusherRedRockRecipes(consumer, basePath + "red_rock/");
    }

    private void addCrusherDaciteRecipes(RecipeOutput consumer, String basePath) {
        //Dacite -> Dacite Cobblestone
        crushing(consumer, basePath, BWGBlocks.DACITE_SET, BWGBlocks.DACITE_COBBLESTONE_SET);
        //Dacite Cobblestone -> Dacite Tile
        crushing(consumer, basePath, BWGBlocks.DACITE_COBBLESTONE_SET, BWGBlocks.DACITE_TILES_SET, villageUpdate);
        //Dacite Tile -> Chiseled Dacite Bricks
        crushing(consumer, basePath, BWGBlocks.DACITE_TILES_SET, BWGBlocks.CHISELED_DACITE_BRICKS_SET, villageUpdate);
        //Chiseled Dacite Bricks -> Dacite Bricks
        crushing(consumer, basePath, BWGBlocks.CHISELED_DACITE_BRICKS_SET, BWGBlocks.DACITE_BRICKS_SET, villageUpdate);
        //Dacite Bricks -> Cracked Dacite Bricks
        crushing(consumer, basePath, BWGBlocks.DACITE_BRICKS_SET, BWGBlocks.CRACKED_DACITE_BRICKS_SET, villageUpdate);
        //Dacite Bricks -> Dacite
        crushing(consumer, basePath, BWGBlocks.CRACKED_DACITE_BRICKS_SET, BWGBlocks.DACITE_SET, villageUpdate);
        //Dacite Pillar -> Dacite
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(BWGBlocks.DACITE_PILLAR.get()),
                    new ItemStack(BWGBlocks.DACITE_SET.getBase(), 2)
              ).addCondition(villageUpdate)
              .build(consumer, Mekanism.rl(basePath + "from_dacite_pillar"));
    }

    private void addCrusherWhiteDaciteRecipes(RecipeOutput consumer, String basePath) {
        //White Dacite -> White Dacite Cobblestone
        crushing(consumer, basePath, BWGBlocks.WHITE_DACITE_SET, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, villageUpdate);
        //White Dacite Cobblestone -> White Dacite Tile
        crushing(consumer, basePath, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, BWGBlocks.WHITE_DACITE_TILES_SET, villageUpdate);
        //White Dacite Tile -> Chiseled White Dacite Bricks
        crushing(consumer, basePath, BWGBlocks.WHITE_DACITE_TILES_SET, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, villageUpdate);
        //Chiseled White Dacite Bricks -> White Dacite Bricks
        crushing(consumer, basePath, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_BRICKS_SET, villageUpdate);
        //White Dacite Bricks -> Cracked White Dacite Bricks
        crushing(consumer, basePath, BWGBlocks.WHITE_DACITE_BRICKS_SET, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, villageUpdate);
        //White Dacite Bricks -> White Dacite
        crushing(consumer, basePath, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_SET, villageUpdate);
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(BWGBlocks.WHITE_DACITE_PILLAR.get()),
                    new ItemStack(BWGBlocks.WHITE_DACITE_SET.getBase(), 2)
              ).addCondition(villageUpdate)
              .build(consumer, Mekanism.rl(basePath + "from_white_dacite_pillar"));
    }

    private void addCrusherRedRockRecipes(RecipeOutput consumer, String basePath) {
        //Red Rock Tile -> Chiseled Red Rock Bricks
        crushing(consumer, basePath, BWGBlocks.RED_ROCK_TILES_SET, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, villageUpdate);
        //Chiseled Red Rock -> Red Rock Bricks
        crushing(consumer, basePath, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET);
        //Red Rock Bricks -> Cracked Red Rock Bricks
        crushing(consumer, basePath, BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET);
        //Cracked Red Rock Bricks -> Polished Red Rock
        crushing(consumer, basePath, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, BWGBlocks.POLISHED_RED_ROCK_SET, villageUpdate);
        //Polished Red Rock -> Red Rock
        crushing(consumer, basePath, BWGBlocks.POLISHED_RED_ROCK_SET, BWGBlocks.RED_ROCK_SET, villageUpdate);
        //Red Rock -> Red Rock Tiles
        crushing(consumer, basePath, BWGBlocks.RED_ROCK_SET, BWGBlocks.RED_ROCK_TILES_SET);
    }

    private void crushing(RecipeOutput consumer, String basePath, BWGBlockSet from, BWGBlockSet to) {
        crushing(consumer, basePath, from, to, null);
    }

    private void crushing(RecipeOutput consumer, String basePath, BWGBlockSet from, BWGBlockSet to, @Nullable ICondition condition) {
        String name = RegistryUtils.getPath(from.getBase());
        crushing(consumer, from.getBase(), to.getBase(), basePath + "conversion_" + name, condition);
        crushing(consumer, from.getSlab(), to.getSlab(), basePath + "slabs_conversion_" + name, condition);
        crushing(consumer, from.getStairs(), to.getStairs(), basePath + "stairs_conversion_" + name, condition);
        crushing(consumer, from.getWall(), to.getWall(), basePath + "walls_conversion_" + name, condition);
    }

    private void crushing(RecipeOutput consumer, ItemLike input, ItemLike output, String path, @Nullable ICondition condition) {
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(input),
                    new ItemStack(output)
              ).addCondition(condition == null ? modLoaded : condition)
              .build(consumer, Mekanism.rl(path));
    }

    private void addEnrichingRecipes(RecipeOutput consumer, String basePath) {
        addMossyStoneEnrichingRecipes(consumer, basePath + "mossy_stone/");
        addDaciteEnrichingRecipes(consumer, basePath + "dacite/");
        addWhiteDaciteEnrichingRecipes(consumer, basePath + "white_dacite/");
        addRedRockEnrichingRecipes(consumer, basePath + "red_rock/");
    }

    private void addMossyStoneEnrichingRecipes(RecipeOutput consumer, String basePath) {
        BWGBlockSet from = BWGBlocks.MOSSY_STONE_SET;
        String name = RegistryUtils.getPath(from.getBase());
        enriching(consumer, from.getBase(), Items.STONE, basePath + "conversion_" + name);
        enriching(consumer, from.getSlab(), Items.STONE_SLAB, basePath + "slabs_conversion_" + name);
        enriching(consumer, from.getStairs(), Items.STONE_STAIRS, basePath + "stairs_conversion_" + name);
        //enriching(consumer, from.getWall(), Items.STONE_WALL, basePath + "walls_conversion_" + name);
    }

    private void addDaciteEnrichingRecipes(RecipeOutput consumer, String basePath) {
        //Dacite Cobble -> Dacite
        enriching(consumer, basePath, BWGBlocks.DACITE_COBBLESTONE_SET, BWGBlocks.DACITE_SET);
        //Dacite -> Cracked Dacite Bricks
        enriching(consumer, basePath, BWGBlocks.DACITE_SET, BWGBlocks.CRACKED_DACITE_BRICKS_SET, villageUpdate);
        //Cracked Dacite Bricks -> Dacite Bricks
        enriching(consumer, basePath, BWGBlocks.CRACKED_DACITE_BRICKS_SET, BWGBlocks.DACITE_BRICKS_SET, villageUpdate);
        //Dacite Bricks -> Chiseled Dacite Bricks
        enriching(consumer, basePath, BWGBlocks.DACITE_BRICKS_SET, BWGBlocks.CHISELED_DACITE_BRICKS_SET, villageUpdate);
        //Chiseled Dacite Bricks -> Dacite Tile
        enriching(consumer, basePath, BWGBlocks.CHISELED_DACITE_BRICKS_SET, BWGBlocks.DACITE_TILES_SET, villageUpdate);
        //Dacite Tile -> Dacite Cobble
        enriching(consumer, basePath, BWGBlocks.DACITE_TILES_SET, BWGBlocks.DACITE_COBBLESTONE_SET, villageUpdate);
    }

    private void addWhiteDaciteEnrichingRecipes(RecipeOutput consumer, String basePath) {
        //White Dacite Cobble -> White Dacite
        enriching(consumer, basePath, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, BWGBlocks.WHITE_DACITE_SET, villageUpdate);
        //White Dacite -> White Cracked Dacite Bricks
        enriching(consumer, basePath, BWGBlocks.WHITE_DACITE_SET, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, villageUpdate);
        //Cracked White Dacite Bricks -> White Dacite Bricks
        enriching(consumer, basePath, BWGBlocks.CRACKED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_BRICKS_SET, villageUpdate);
        //White Dacite Bricks -> Chiseled White Dacite Bricks
        enriching(consumer, basePath, BWGBlocks.WHITE_DACITE_BRICKS_SET, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, villageUpdate);
        //Chiseled White Dacite Bricks -> White Dacite Tile
        enriching(consumer, basePath, BWGBlocks.CHISELED_WHITE_DACITE_BRICKS_SET, BWGBlocks.WHITE_DACITE_TILES_SET, villageUpdate);
        //White Dacite Tile -> White Dacite Cobble
        enriching(consumer, basePath, BWGBlocks.WHITE_DACITE_TILES_SET, BWGBlocks.WHITE_DACITE_COBBLESTONE_SET, villageUpdate);
    }

    private void addRedRockEnrichingRecipes(RecipeOutput consumer, String basePath) {
        //Red Rock -> Polished Red Rock
        enriching(consumer, basePath, BWGBlocks.RED_ROCK_SET, BWGBlocks.POLISHED_RED_ROCK_SET, villageUpdate);
        //Polished Red Rock -> Cracked Red Rock
        enriching(consumer, basePath, BWGBlocks.POLISHED_RED_ROCK_SET, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, villageUpdate);
        //Cracked Red Rock Bricks -> Red Rock Bricks
        enriching(consumer, basePath, BWGBlocks.CRACKED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET);
        //Red Rock Bricks -> Chiseled Red Rock
        enriching(consumer, basePath, BWGBlocks.RED_ROCK_BRICKS_SET, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET);
        //Chiseled Red Rock -> Red Rock Tiles
        enriching(consumer, basePath, BWGBlocks.CHISELED_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_TILES_SET, villageUpdate);
        //Red Rock Tiles -> Red Rock
        enriching(consumer, basePath, BWGBlocks.RED_ROCK_TILES_SET, BWGBlocks.RED_ROCK_SET, villageUpdate);

        //Mossy Red Rock Bricks -> Red Rock Bricks
        enriching(consumer, basePath, BWGBlocks.MOSSY_RED_ROCK_BRICKS_SET, BWGBlocks.RED_ROCK_BRICKS_SET);
    }

    private void enriching(RecipeOutput consumer, String basePath, BWGBlockSet from, BWGBlockSet to) {
        enriching(consumer, basePath, from, to, null);
    }

    private void enriching(RecipeOutput consumer, String basePath, BWGBlockSet from, BWGBlockSet to, @Nullable ICondition condition) {
        String name = RegistryUtils.getPath(from.getBase());
        enriching(consumer, from.getBase(), to.getBase(), basePath + "conversion_" + name, condition);
        enriching(consumer, from.getSlab(), to.getSlab(), basePath + "slabs_conversion_" + name, condition);
        enriching(consumer, from.getStairs(), to.getStairs(), basePath + "stairs_conversion_" + name, condition);
        enriching(consumer, from.getWall(), to.getWall(), basePath + "walls_conversion_" + name, condition);
    }

    private void enriching(RecipeOutput consumer, ItemLike input, ItemLike output, String path) {
        enriching(consumer, input, output, path, null);
    }

    private void enriching(RecipeOutput consumer, ItemLike input, ItemLike output, String path, @Nullable ICondition condition) {
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(input),
                    new ItemStack(output)
              ).addCondition(condition == null ? modLoaded : condition)
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
        String name = RegistryUtils.getPath(from.getBase());
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
