package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.item.BOPItems;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.condition.ModVersionLoadedCondition;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;

@ParametersAreNonnullByDefault
public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider() {
        super("biomesoplenty");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addDyeRecipes(consumer, basePath);
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
        //Mud brick -> mud ball
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
                    ItemStackIngredient.from(BOPItems.mud_brick),
                    GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
                    new ItemStack(BOPItems.mud_ball)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "mud_brick_to_mud_ball"));

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
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, door, fenceGate, tag(name + "_logs"), pressurePlate, trapdoor, name,
              modLoaded);
    }

    private void addSandRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Black Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "black", modLoaded, BOPBlocks.black_sand, BOPBlocks.black_sandstone,
              BOPBlocks.chiseled_black_sandstone, BOPBlocks.cut_black_sandstone, BOPBlocks.smooth_black_sandstone);
        //Orange Sandstone -> Sand
        //TODO - 1.18: Change this to just modLoaded
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "orange", new ModVersionLoadedCondition(modid, "1.16.3-12.0.0.404"),
              BOPBlocks.orange_sand, BOPBlocks.orange_sandstone, BOPBlocks.chiseled_orange_sandstone, BOPBlocks.cut_orange_sandstone, BOPBlocks.smooth_orange_sandstone);
        //White Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "white", modLoaded, BOPBlocks.white_sand, BOPBlocks.white_sandstone,
              BOPBlocks.chiseled_white_sandstone, BOPBlocks.cut_white_sandstone, BOPBlocks.smooth_white_sandstone);
    }

    private void addDyeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Red
        dye(consumer, basePath, Items.RED_DYE, EnumColor.RED, BOPBlocks.rose);
        //Purple
        dye(consumer, basePath, Items.PURPLE_DYE, EnumColor.PURPLE, BOPBlocks.violet, BOPBlocks.lavender);
        //Magenta
        dye(consumer, basePath, Items.MAGENTA_DYE, EnumColor.PINK, BOPBlocks.wildflower);
        //Orange
        dye(consumer, basePath, Items.ORANGE_DYE, EnumColor.ORANGE, BOPBlocks.orange_cosmos, BOPBlocks.burning_blossom);
        //Pink
        dye(consumer, basePath, Items.PINK_DYE, EnumColor.BRIGHT_PINK, BOPBlocks.pink_daffodil, BOPBlocks.pink_hibiscus);
        //Cyan
        dye(consumer, basePath, Items.CYAN_DYE, EnumColor.DARK_AQUA, BOPBlocks.glowflower);
        //Gray
        dye(consumer, basePath, Items.GRAY_DYE, EnumColor.DARK_GRAY, BOPBlocks.wilted_lily);
        //Light Blue
        dye(consumer, basePath, Items.LIGHT_BLUE_DYE, EnumColor.INDIGO, BOPBlocks.blue_hydrangea);
        //Yellow
        dye(consumer, basePath, Items.YELLOW_DYE, EnumColor.YELLOW, BOPBlocks.goldenrod);
    }

    private void dye(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider output, EnumColor color, IItemProvider... inputs) {
        ItemStackIngredient inputIngredient = ItemStackIngredient.from(Ingredient.of(inputs));
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color).getStack(flowerRate)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/" + color.getRegistryPrefix()));
    }
}