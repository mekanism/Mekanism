package mekanism.common.recipe;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Class for helpers that are also used by some of our recipe compat providers for convenience
 */
@ParametersAreNonnullByDefault
public class RecipeProviderUtil {

    public static void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, IItemProvider boat, IItemProvider door,
          IItemProvider fenceGate, Tag<Item> log, IItemProvider pressurePlate, IItemProvider trapdoor, String name) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, door, fenceGate, log, pressurePlate, trapdoor, name, null);
    }

    public static void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, IItemProvider boat, IItemProvider door,
          IItemProvider fenceGate, Tag<Item> log, IItemProvider pressurePlate, IItemProvider trapdoor, String name, @Nullable ICondition condition) {
        //Boat
        SawmillRecipeBuilder boatRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(boat.asItem()),
              new ItemStack(planks, 5)
        );
        //Door
        SawmillRecipeBuilder doorRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(door.asItem()),
              new ItemStack(planks, 2)
        );
        //Fence Gate
        SawmillRecipeBuilder fenceGateRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(fenceGate.asItem()),
              new ItemStack(planks, 2),
              new ItemStack(Items.STICK, 4),
              1
        );
        //Log
        SawmillRecipeBuilder logRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(log),
              new ItemStack(planks, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        );
        //Pressure plate
        SawmillRecipeBuilder pressurePlateRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(pressurePlate.asItem()),
              new ItemStack(planks, 2)
        );
        //Trapdoor
        SawmillRecipeBuilder trapdoorRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(trapdoor.asItem()),
              new ItemStack(planks, 3)
        );
        if (condition != null) {
            //If there is a condition, add it to the various recipe builders
            boatRecipeBuilder.addCondition(condition);
            doorRecipeBuilder.addCondition(condition);
            fenceGateRecipeBuilder.addCondition(condition);
            logRecipeBuilder.addCondition(condition);
            pressurePlateRecipeBuilder.addCondition(condition);
            trapdoorRecipeBuilder.addCondition(condition);
        }
        //build the recipes
        boatRecipeBuilder.build(consumer, Mekanism.rl(basePath + "boat/" + name));
        doorRecipeBuilder.build(consumer, Mekanism.rl(basePath + "door/" + name));
        fenceGateRecipeBuilder.build(consumer, Mekanism.rl(basePath + "fence_gate/" + name));
        logRecipeBuilder.build(consumer, Mekanism.rl(basePath + "log/" + name));
        pressurePlateRecipeBuilder.build(consumer, Mekanism.rl(basePath + "pressure_plate/" + name));
        trapdoorRecipeBuilder.build(consumer, Mekanism.rl(basePath + "trapdoor/" + name));
    }

    public static void addPrecisionSawmillBedRecipe(Consumer<IFinishedRecipe> consumer, String basePath, Item bed, Item wool, String name) {
        addPrecisionSawmillBedRecipe(consumer, basePath, bed, Items.OAK_PLANKS, wool, name, null);
    }

    public static void addPrecisionSawmillBedRecipe(Consumer<IFinishedRecipe> consumer, String basePath, Item bed, Item planks, Item wool, String name,
          @Nullable ICondition condition) {
        SawmillRecipeBuilder bedRecipeBuilder = SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(bed),
              new ItemStack(planks, 3),
              new ItemStack(wool, 3),
              1
        );
        if (condition != null) {
            bedRecipeBuilder.addCondition(condition);
        }
        bedRecipeBuilder.build(consumer, Mekanism.rl(basePath + name));
    }
}