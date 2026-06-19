package mekanism.common.recipe;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
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
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
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

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, HolderGetter<Item> items, String basePath, Item planks, @Nullable Item boat,
          @Nullable Item chestBoat, Item door, Item fenceGate, @Nullable TagKey<Item> log, Item pressurePlate, Item trapdoor,
          @Nullable Item hangingSign, String name) {
        addPrecisionSawmillWoodTypeRecipes(consumer, items, basePath, planks, boat, chestBoat, door, fenceGate, log, pressurePlate, trapdoor, hangingSign, name, null);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, HolderGetter<Item> items, String basePath, Item planks, @Nullable Item boat,
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
                      template(items, BlockItemIds.CHEST),
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
              new ItemStackTemplate(items.getOrThrow(ItemIds.STICK), 4),
              1
        ), basePath + "fence_gate/" + name, condition);
        if (log != null) {
            //Log
            save(consumer, SawmillRecipeBuilder.sawing(
                  IngredientCreatorAccess.item().from(items, log),
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

    public static void addSandStoneToSandRecipe(RecipeOutput consumer, HolderGetter<Item> items, String path, @Nullable ICondition condition, Item sand,
          TagKey<Item> sandstoneTag) {
        save(consumer, ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(items, sandstoneTag),
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

    public static ItemStackTemplate template(HolderGetter<Item> items, ResourceKey<Item> item) {
        return template(items, item, 1);
    }

    public static ItemStackTemplate template(HolderGetter<Item> items, ResourceKey<Item> item, int amount) {
        return new ItemStackTemplate(items.getOrThrow(item), amount);
    }

    public static ItemStackTemplate template(HolderGetter<Item> items, BlockItemId item) {
        return template(items, item, 1);
    }

    public static ItemStackTemplate template(HolderGetter<Item> items, BlockItemId item, int amount) {
        return template(items, item.item(), amount);
    }

    public static ChemicalStackTemplate chemicalTemplate(HolderGetter<Chemical> chemicals, ResourceKey<Chemical> chemical, int amount) {
        return new ChemicalStackTemplate(chemicals.getOrThrow(chemical), amount);
    }

    public static FluidStackTemplate fluidTemplate(HolderGetter<Fluid> fluids, ResourceKey<Fluid> fluid, int amount) {
        return new FluidStackTemplate(fluids.getOrThrow(fluid), amount);
    }
}