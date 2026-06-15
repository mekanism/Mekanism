package mekanism.common.recipe;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.builder.ExtendedCookingRecipeBuilder;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

/// Class for helpers that are also used by some of our recipe compat providers for convenience
public class RecipeProviderUtil {

    private RecipeProviderUtil() {
    }

    public static void addSmeltingBlastingRecipes(RecipeOutput consumer, Ingredient smeltingInput, Holder<Item> output, float experience, int smeltingTime,
          Identifier blastingLocation, Identifier smeltingLocation, RecipeCriterion... criteria) {
        ExtendedCookingRecipeBuilder blastingRecipe = ExtendedCookingRecipeBuilder.blasting(output, smeltingInput, smeltingTime / 2).experience(experience);
        ExtendedCookingRecipeBuilder smeltingRecipe = ExtendedCookingRecipeBuilder.smelting(output, smeltingInput, smeltingTime).experience(experience);
        //If there are any criteria add them
        for (RecipeCriterion criterion : criteria) {
            blastingRecipe.unlockedBy(criterion);
            smeltingRecipe.unlockedBy(criterion);
        }
        blastingRecipe.save(consumer, blastingLocation);
        smeltingRecipe.save(consumer, smeltingLocation);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, HolderGetter<Item> lookup, String basePath, Item planks, @Nullable Item boat,
          @Nullable Item chestBoat, Item door, Item fenceGate, @Nullable TagKey<Item> log, Item pressurePlate, Item trapdoor,
          @Nullable Item hangingSign, String name) {
        addPrecisionSawmillWoodTypeRecipes(consumer, lookup, basePath, planks, boat, chestBoat, door, fenceGate, log, pressurePlate, trapdoor, hangingSign, name, null);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, HolderGetter<Item> lookup, String basePath, Item planks, @Nullable Item boat,
          @Nullable Item chestBoat, Item door, Item fenceGate, @Nullable TagKey<Item> log, Item pressurePlate, Item trapdoor,
          @Nullable Item hangingSign, String name, @Nullable ICondition condition) {
        if (boat != null) {
            //Boat
            save(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(boat),
                  new ItemStackTemplate(planks, 5)
            ), basePath + "boat/" + name, condition);
            if (chestBoat != null) {
                //Chest Boat
                save(consumer, SawmillRecipeBuilder.sawing(
                      IngredientCreatorAccess.item().from(chestBoat),
                      new ItemStackTemplate(boat),
                      new ItemStackTemplate(Items.CHEST),
                      1
                ), basePath + "chest_boat/" + name, condition);
            }
        }
        //Door
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(door),
              new ItemStackTemplate(planks, 2)
        ), basePath + "door/" + name, condition);
        //Fence Gate
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(fenceGate),
              new ItemStackTemplate(planks, 2),
              new ItemStackTemplate(Items.STICK, 4),
              1
        ), basePath + "fence_gate/" + name, condition);
        if (log != null) {
            //Log
            save(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(lookup, log),
                  new ItemStackTemplate(planks, 6),
                  MekanismItems.SAWDUST.asTemplate(),
                  0.25
            ), basePath + "log/" + name, condition);
        }
        if (hangingSign != null) {
            //Hanging sign
            save(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(hangingSign),
                  new ItemStackTemplate(planks, 2),
                  MekanismItems.SAWDUST.asTemplate(),
                  0.5
            ), basePath + "hanging_sign/" + name, condition);
        }
        //Pressure plate
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(pressurePlate),
              new ItemStackTemplate(planks),
              MekanismItems.SAWDUST.asTemplate(2),
              0.25
        ), basePath + "pressure_plate/" + name, condition);
        //Trapdoor
        save(consumer, SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(trapdoor),
              new ItemStackTemplate(planks, 3)
        ), basePath + "trapdoor/" + name, condition);
    }

    public static void addSandStoneToSandRecipe(RecipeOutput consumer, HolderGetter<Item> lookup, String path, @Nullable ICondition condition, Item sand,
          TagKey<Item> sandstoneTag) {
        save(consumer, ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(lookup, sandstoneTag),
              new ItemStackTemplate(sand, 2)
        ), path, condition);
    }

    @Deprecated
    public static void addSandStoneToSandRecipe(RecipeOutput consumer, String path, @Nullable ICondition condition, Item sand, Item... sandstones) {
        save(consumer, ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(sandstones),
              new ItemStackTemplate(sand, 2)
        ), path, condition);
    }

    private static void save(RecipeOutput consumer, MekanismRecipeBuilder<?> builder, String path, @Nullable ICondition condition) {
        if (condition != null) {
            //If there is a condition, add it to the recipe builder
            builder.addCondition(condition);
        }
        builder.save(consumer, Mekanism.rl(path));
    }
}