package mekanism.common.recipe;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.builder.ExtendedCookingRecipeBuilder;
import mekanism.common.registries.MekanismItems;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Class for helpers that are also used by some of our recipe compat providers for convenience
 */
@ParametersAreNonnullByDefault
public class RecipeProviderUtil {

    private RecipeProviderUtil() {
    }

    public static void addSmeltingBlastingRecipes(Consumer<IFinishedRecipe> consumer, Ingredient smeltingInput, IItemProvider output, float experience, int smeltingTime,
          ResourceLocation blastingLocation, ResourceLocation smeltingLocation, RecipeCriterion... criteria) {
        ExtendedCookingRecipeBuilder blastingRecipe = ExtendedCookingRecipeBuilder.blasting(output, smeltingInput, smeltingTime / 2).experience(experience);
        ExtendedCookingRecipeBuilder smeltingRecipe = ExtendedCookingRecipeBuilder.smelting(output, smeltingInput, smeltingTime).experience(experience);
        //If there are any criteria add them
        for (RecipeCriterion criterion : criteria) {
            blastingRecipe.addCriterion(criterion);
            smeltingRecipe.addCriterion(criterion);
        }
        blastingRecipe.build(consumer, blastingLocation);
        smeltingRecipe.build(consumer, smeltingLocation);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, @Nullable IItemProvider boat,
          IItemProvider door, IItemProvider fenceGate, ITag<Item> log, IItemProvider pressurePlate, IItemProvider trapdoor, String name) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, door, fenceGate, log, pressurePlate, trapdoor, name, null);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, @Nullable IItemProvider boat,
          IItemProvider door, IItemProvider fenceGate, ITag<Item> log, IItemProvider pressurePlate, IItemProvider trapdoor, String name, @Nullable ICondition condition) {
        if (boat != null) {
            //Boat
            build(consumer, SawmillRecipeBuilder.sawing(
                  ItemStackIngredient.from(boat.asItem()),
                  new ItemStack(planks, 5)
            ), basePath + "boat/" + name, condition);
        }
        //Door
        build(consumer, SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(door.asItem()),
              new ItemStack(planks, 2)
        ), basePath + "door/" + name, condition);
        //Fence Gate
        build(consumer, SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(fenceGate.asItem()),
              new ItemStack(planks, 2),
              new ItemStack(Items.STICK, 4),
              1
        ), basePath + "fence_gate/" + name, condition);
        //Log
        build(consumer, SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(log),
              new ItemStack(planks, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ), basePath + "log/" + name, condition);
        //Pressure plate
        build(consumer, SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(pressurePlate.asItem()),
              new ItemStack(planks, 2)
        ), basePath + "pressure_plate/" + name, condition);
        //Trapdoor
        build(consumer, SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(trapdoor.asItem()),
              new ItemStack(planks, 3)
        ), basePath + "trapdoor/" + name, condition);
    }

    private static void build(Consumer<IFinishedRecipe> consumer, MekanismRecipeBuilder<?> builder, String path, @Nullable ICondition condition) {
        if (condition != null) {
            //If there is a condition, add it to the recipe builder
            builder.addCondition(condition);
        }
        builder.build(consumer, Mekanism.rl(path));
    }

    public static void addPrecisionSawmillBedRecipe(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider bed, DyeColor color) {
        addPrecisionSawmillBedRecipe(consumer, basePath, bed, Blocks.OAK_PLANKS, color, null);
    }

    public static void addPrecisionSawmillBedRecipe(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider bed, IItemProvider planks, DyeColor color,
          @Nullable ICondition condition) {
        SawmillRecipeBuilder bedRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(bed),
              new ItemStack(planks, 3),
              new ItemStack(getWool(color), 3),
              1
        );
        if (condition != null) {
            bedRecipeBuilder.addCondition(condition);
        }
        bedRecipeBuilder.build(consumer, Mekanism.rl(basePath + color));
    }

    private static IItemProvider getWool(DyeColor color) {
        switch (color) {
            default:
            case WHITE:
                return Items.WHITE_WOOL;
            case ORANGE:
                return Items.ORANGE_WOOL;
            case MAGENTA:
                return Items.MAGENTA_WOOL;
            case LIGHT_BLUE:
                return Items.LIGHT_BLUE_WOOL;
            case YELLOW:
                return Items.YELLOW_WOOL;
            case LIME:
                return Items.LIME_WOOL;
            case PINK:
                return Items.PINK_WOOL;
            case GRAY:
                return Items.GRAY_WOOL;
            case LIGHT_GRAY:
                return Items.LIGHT_GRAY_WOOL;
            case CYAN:
                return Items.CYAN_WOOL;
            case PURPLE:
                return Items.PURPLE_WOOL;
            case BLUE:
                return Items.BLUE_WOOL;
            case BROWN:
                return Items.BROWN_WOOL;
            case GREEN:
                return Items.GREEN_WOOL;
            case RED:
                return Items.RED_WOOL;
            case BLACK:
                return Items.BLACK_WOOL;
        }
    }
}