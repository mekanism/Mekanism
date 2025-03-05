package mekanism.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.builder.ExtendedCookingRecipeBuilder;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

/**
 * Class for helpers that are also used by some of our recipe compat providers for convenience
 */
@NothingNullByDefault
public class RecipeProviderUtil {

    private RecipeProviderUtil() {
    }

    public static void addSmeltingBlastingRecipes(RecipeOutput consumer, Ingredient smeltingInput, Holder<Item> output, float experience, int smeltingTime,
          ResourceLocation blastingLocation, ResourceLocation smeltingLocation, RecipeCriterion... criteria) {
        ExtendedCookingRecipeBuilder blastingRecipe = ExtendedCookingRecipeBuilder.blasting(output, smeltingInput, smeltingTime / 2).experience(experience);
        ExtendedCookingRecipeBuilder smeltingRecipe = ExtendedCookingRecipeBuilder.smelting(output, smeltingInput, smeltingTime).experience(experience);
        //If there are any criteria add them
        for (RecipeCriterion criterion : criteria) {
            blastingRecipe.unlockedBy(criterion);
            smeltingRecipe.unlockedBy(criterion);
        }
        blastingRecipe.build(consumer, blastingLocation);
        smeltingRecipe.build(consumer, smeltingLocation);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, String basePath, ItemLike planks, @Nullable ItemLike boat,
          @Nullable ItemLike chestBoat, ItemLike door, ItemLike fenceGate, @Nullable TagKey<Item> log, ItemLike pressurePlate, ItemLike trapdoor,
          @Nullable ItemLike hangingSign, String name) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, chestBoat, door, fenceGate, log, pressurePlate, trapdoor, hangingSign, name, null);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, String basePath, ItemLike planks, @Nullable ItemLike boat,
          @Nullable ItemLike chestBoat, ItemLike door, ItemLike fenceGate, @Nullable TagKey<Item> log, ItemLike pressurePlate, ItemLike trapdoor,
          @Nullable ItemLike hangingSign, String name, @Nullable ICondition condition) {
        if (boat != null) {
            //Boat
            build(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(boat),
                  new ItemStack(planks, 5)
            ), basePath + "boat/" + name, condition);
            if (chestBoat != null) {
                //Chest Boat
                build(consumer, SawmillRecipeBuilder.sawing(
                      IngredientCreatorAccess.item().from(chestBoat),
                      new ItemStack(boat),
                      new ItemStack(Items.CHEST),
                      1
                ), basePath + "chest_boat/" + name, condition);
            }
        }
        //Door
        build(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(door),
              new ItemStack(planks, 2)
        ), basePath + "door/" + name, condition);
        //Fence Gate
        build(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(fenceGate),
              new ItemStack(planks, 2),
              new ItemStack(Items.STICK, 4),
              1
        ), basePath + "fence_gate/" + name, condition);
        if (log != null) {
            //Log
            build(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(log),
                  new ItemStack(planks, 6),
                  MekanismItems.SAWDUST.asStack(),
                  0.25
            ), basePath + "log/" + name, condition);
        }
        if (hangingSign != null) {
            //Hanging sign
            build(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(hangingSign),
                  new ItemStack(planks, 2),
                  MekanismItems.SAWDUST.asStack(),
                  0.5
            ), basePath + "hanging_sign/" + name, condition);
        }
        //Pressure plate
        build(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(pressurePlate),
              new ItemStack(planks, 1),
              MekanismItems.SAWDUST.asStack(2),
              0.25
        ), basePath + "pressure_plate/" + name, condition);
        //Trapdoor
        build(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(trapdoor),
              new ItemStack(planks, 3)
        ), basePath + "trapdoor/" + name, condition);
    }

    public static void addSandStoneToSandRecipe(RecipeOutput consumer, String path, @Nullable ICondition condition, ItemLike sand, TagKey<Item> sandstoneTag) {
        build(consumer, ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(sandstoneTag),
              new ItemStack(sand, 2)
        ), path, condition);
    }

    @Deprecated
    public static void addSandStoneToSandRecipe(RecipeOutput consumer, String path, @Nullable ICondition condition, ItemLike sand, ItemLike... sandstones) {
        build(consumer, ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(sandstones),
              new ItemStack(sand, 2)
        ), path, condition);
    }

    private static void build(RecipeOutput consumer, MekanismRecipeBuilder<?> builder, String path, @Nullable ICondition condition) {
        if (condition != null) {
            //If there is a condition, add it to the recipe builder
            builder.addCondition(condition);
        }
        builder.build(consumer, Mekanism.rl(path));
    }

    public static void addPrecisionSawmillBedRecipe(RecipeOutput consumer, String basePath, Item bed, DyeColor color) {
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(bed),
              new ItemStack(Items.OAK_PLANKS, 3),
              new ItemStack(getWool(color), 3),
              1
        ).build(consumer, Mekanism.rl(basePath + color));
    }

    private static Item getWool(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_WOOL;
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
        };
    }
}