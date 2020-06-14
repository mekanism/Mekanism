package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.item.BOPItems;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackGasToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;

@ParametersAreNonnullByDefault
public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider() {
        super("biomesoplenty");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        //Mud brick -> mud ball
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(BOPItems.mud_brick),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(BOPItems.mud_ball)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "mud_brick_to_mud_ball"));
        //White Sandstone -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(BOPBlocks.white_sandstone),
                    ItemStackIngredient.from(BOPBlocks.chiseled_white_sandstone),
                    ItemStackIngredient.from(BOPBlocks.cut_white_sandstone),
                    ItemStackIngredient.from(BOPBlocks.smooth_white_sandstone)
              ),
              new ItemStack(BOPBlocks.white_sand, 2)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "white_sandstone_to_sand"));
        //TODO: Bio-fuel recipes?
    }

    private void addPrecisionSawmillRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.cherry_planks, BOPItems.cherry_boat, BOPBlocks.cherry_door, BOPBlocks.cherry_fence_gate,
              BOPBlocks.cherry_pressure_plate, BOPBlocks.cherry_trapdoor, "cherry");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.dead_planks, BOPItems.dead_boat, BOPBlocks.dead_door, BOPBlocks.dead_fence_gate,
              BOPBlocks.dead_pressure_plate, BOPBlocks.dead_trapdoor, "dead");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.fir_planks, BOPItems.fir_boat, BOPBlocks.fir_door, BOPBlocks.fir_fence_gate,
              BOPBlocks.fir_pressure_plate, BOPBlocks.fir_trapdoor, "fir");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.hellbark_planks, BOPItems.hellbark_boat, BOPBlocks.hellbark_door, BOPBlocks.hellbark_fence_gate,
              BOPBlocks.hellbark_pressure_plate, BOPBlocks.hellbark_trapdoor, "hellbark");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.jacaranda_planks, BOPItems.jacaranda_boat, BOPBlocks.jacaranda_door, BOPBlocks.jacaranda_fence_gate,
              BOPBlocks.jacaranda_pressure_plate, BOPBlocks.jacaranda_trapdoor, "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.magic_planks, BOPItems.magic_boat, BOPBlocks.magic_door, BOPBlocks.magic_fence_gate,
              BOPBlocks.magic_pressure_plate, BOPBlocks.magic_trapdoor, "magic");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.mahogany_planks, BOPItems.mahogany_boat, BOPBlocks.mahogany_door, BOPBlocks.mahogany_fence_gate,
              BOPBlocks.mahogany_pressure_plate, BOPBlocks.mahogany_trapdoor, "mahogany");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.palm_planks, BOPItems.palm_boat, BOPBlocks.palm_door, BOPBlocks.palm_fence_gate,
              BOPBlocks.palm_pressure_plate, BOPBlocks.palm_trapdoor, "palm");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.redwood_planks, BOPItems.redwood_boat, BOPBlocks.redwood_door, BOPBlocks.redwood_fence_gate,
              BOPBlocks.redwood_pressure_plate, BOPBlocks.redwood_trapdoor, "redwood");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.umbran_planks, BOPItems.umbran_boat, BOPBlocks.umbran_door, BOPBlocks.umbran_fence_gate,
              BOPBlocks.umbran_pressure_plate, BOPBlocks.umbran_trapdoor, "umbran");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.willow_planks, BOPItems.willow_boat, BOPBlocks.willow_door, BOPBlocks.willow_fence_gate,
              BOPBlocks.willow_pressure_plate, BOPBlocks.willow_trapdoor, "willow");
    }

    private void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, IItemProvider boat, IItemProvider door,
          IItemProvider fenceGate, IItemProvider pressurePlate, IItemProvider trapdoor, String name) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, door, fenceGate, new ItemTags.Wrapper(rl(name + "_logs")),
              pressurePlate, trapdoor, name, modLoaded);
    }
}