package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.item.BOPItems;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

@NothingNullByDefault
public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addDyeRecipes(consumer, basePath);
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
    }

    private void addPrecisionSawmillRecipes(RecipeOutput consumer, String basePath) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.DEAD_PLANKS, BOPItems.DEAD_BOAT, BOPItems.DEAD_CHEST_BOAT, BOPBlocks.DEAD_DOOR,
              BOPBlocks.DEAD_FENCE_GATE, BOPBlocks.DEAD_PRESSURE_PLATE, BOPBlocks.DEAD_TRAPDOOR, BOPBlocks.DEAD_HANGING_SIGN, "dead");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.EMPYREAL_PLANKS, BOPItems.EMPYREAL_BOAT, BOPItems.EMPYREAL_CHEST_BOAT, BOPBlocks.EMPYREAL_DOOR,
              BOPBlocks.EMPYREAL_FENCE_GATE, BOPBlocks.EMPYREAL_PRESSURE_PLATE, BOPBlocks.EMPYREAL_TRAPDOOR, BOPBlocks.EMPYREAL_HANGING_SIGN, "empyreal");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.FIR_PLANKS, BOPItems.FIR_BOAT, BOPItems.FIR_CHEST_BOAT, BOPBlocks.FIR_DOOR,
              BOPBlocks.FIR_FENCE_GATE, BOPBlocks.FIR_PRESSURE_PLATE, BOPBlocks.FIR_TRAPDOOR, BOPBlocks.FIR_HANGING_SIGN, "fir");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.PINE_PLANKS, BOPItems.PINE_BOAT, BOPItems.PINE_CHEST_BOAT, BOPBlocks.PINE_DOOR,
              BOPBlocks.PINE_FENCE_GATE, BOPBlocks.PINE_PRESSURE_PLATE, BOPBlocks.PINE_TRAPDOOR, BOPBlocks.PINE_HANGING_SIGN, "pine");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.MAPLE_PLANKS, BOPItems.MAPLE_BOAT, BOPItems.MAPLE_CHEST_BOAT, BOPBlocks.MAPLE_DOOR,
              BOPBlocks.MAPLE_FENCE_GATE, BOPBlocks.MAPLE_PRESSURE_PLATE, BOPBlocks.MAPLE_TRAPDOOR, BOPBlocks.MAPLE_HANGING_SIGN, "maple");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.HELLBARK_PLANKS, BOPItems.HELLBARK_BOAT, BOPItems.HELLBARK_CHEST_BOAT, BOPBlocks.HELLBARK_DOOR,
              BOPBlocks.HELLBARK_FENCE_GATE, BOPBlocks.HELLBARK_PRESSURE_PLATE, BOPBlocks.HELLBARK_TRAPDOOR, BOPBlocks.HELLBARK_HANGING_SIGN, "hellbark");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.JACARANDA_PLANKS, BOPItems.JACARANDA_BOAT, BOPItems.JACARANDA_CHEST_BOAT, BOPBlocks.JACARANDA_DOOR,
              BOPBlocks.JACARANDA_FENCE_GATE, BOPBlocks.JACARANDA_PRESSURE_PLATE, BOPBlocks.JACARANDA_TRAPDOOR, BOPBlocks.JACARANDA_HANGING_SIGN, "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.MAGIC_PLANKS, BOPItems.MAGIC_BOAT, BOPItems.MAGIC_CHEST_BOAT, BOPBlocks.MAGIC_DOOR,
              BOPBlocks.MAGIC_FENCE_GATE, BOPBlocks.MAGIC_PRESSURE_PLATE, BOPBlocks.MAGIC_TRAPDOOR, BOPBlocks.MAGIC_HANGING_SIGN, "magic");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.MAHOGANY_PLANKS, BOPItems.MAHOGANY_BOAT, BOPItems.MAHOGANY_CHEST_BOAT, BOPBlocks.MAHOGANY_DOOR,
              BOPBlocks.MAHOGANY_FENCE_GATE, BOPBlocks.MAHOGANY_PRESSURE_PLATE, BOPBlocks.MAHOGANY_TRAPDOOR, BOPBlocks.MAHOGANY_HANGING_SIGN, "mahogany");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.PALM_PLANKS, BOPItems.PALM_BOAT, BOPItems.PALM_CHEST_BOAT, BOPBlocks.PALM_DOOR,
              BOPBlocks.PALM_FENCE_GATE, BOPBlocks.PALM_PRESSURE_PLATE, BOPBlocks.PALM_TRAPDOOR, BOPBlocks.PALM_HANGING_SIGN, "palm");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.REDWOOD_PLANKS, BOPItems.REDWOOD_BOAT, BOPItems.REDWOOD_CHEST_BOAT, BOPBlocks.REDWOOD_DOOR,
              BOPBlocks.REDWOOD_FENCE_GATE, BOPBlocks.REDWOOD_PRESSURE_PLATE, BOPBlocks.REDWOOD_TRAPDOOR, BOPBlocks.REDWOOD_HANGING_SIGN, "redwood");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.UMBRAN_PLANKS, BOPItems.UMBRAN_BOAT, BOPItems.UMBRAN_CHEST_BOAT, BOPBlocks.UMBRAN_DOOR,
              BOPBlocks.UMBRAN_FENCE_GATE, BOPBlocks.UMBRAN_PRESSURE_PLATE, BOPBlocks.UMBRAN_TRAPDOOR, BOPBlocks.UMBRAN_HANGING_SIGN, "umbran");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.WILLOW_PLANKS, BOPItems.WILLOW_BOAT, BOPItems.WILLOW_CHEST_BOAT, BOPBlocks.WILLOW_DOOR,
              BOPBlocks.WILLOW_FENCE_GATE, BOPBlocks.WILLOW_PRESSURE_PLATE, BOPBlocks.WILLOW_TRAPDOOR, BOPBlocks.WILLOW_HANGING_SIGN, "willow");
    }

    private void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, String basePath, Block planks, Item boat, Item chestBoat, Block door, Block fenceGate,
          Block pressurePlate, Block trapdoor, Block hangingSign, String name) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, chestBoat, door, fenceGate,
              tag(name + "_logs"), pressurePlate, trapdoor, hangingSign, name, modLoaded);
    }

    private void addSandRecipes(RecipeOutput consumer, String basePath) {
        //Black Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "black", BOPBlocks.BLACK_SAND, BOPBlocks.BLACK_SANDSTONE, BOPBlocks.CHISELED_BLACK_SANDSTONE,
              BOPBlocks.CUT_BLACK_SANDSTONE, BOPBlocks.SMOOTH_BLACK_SANDSTONE);
        //Orange Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "orange", BOPBlocks.ORANGE_SAND, BOPBlocks.ORANGE_SANDSTONE, BOPBlocks.CHISELED_ORANGE_SANDSTONE,
              BOPBlocks.CUT_ORANGE_SANDSTONE, BOPBlocks.SMOOTH_ORANGE_SANDSTONE);
        //White Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "white", BOPBlocks.WHITE_SAND, BOPBlocks.WHITE_SANDSTONE, BOPBlocks.CHISELED_WHITE_SANDSTONE,
              BOPBlocks.CUT_WHITE_SANDSTONE, BOPBlocks.SMOOTH_WHITE_SANDSTONE);
    }

    private void addSandStoneToSandRecipe(RecipeOutput consumer, String path, Block sand, Block... sandstones) {
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, path, modLoaded, sand, sandstones);
    }

    private void addDyeRecipes(RecipeOutput consumer, String basePath) {
        //Brown
        largeDye(consumer, basePath, Items.BROWN_DYE, EnumColor.BROWN, BOPBlocks.CATTAIL);
        //Red
        dye(consumer, basePath, Items.RED_DYE, EnumColor.RED, BOPBlocks.ROSE, BOPBlocks.WATERLILY);
        //Green
        dye(consumer, basePath, Items.GREEN_DYE, EnumColor.DARK_GREEN, BOPBlocks.TINY_CACTUS);
        //Purple
        dye(consumer, basePath, Items.PURPLE_DYE, EnumColor.PURPLE, BOPBlocks.VIOLET, BOPBlocks.LAVENDER);
        largeDye(consumer, basePath, Items.PURPLE_DYE, EnumColor.PURPLE, BOPBlocks.TALL_LAVENDER);
        //Magenta
        dye(consumer, basePath, Items.MAGENTA_DYE, EnumColor.PINK, BOPBlocks.WILDFLOWER);
        //Orange
        dye(consumer, basePath, Items.ORANGE_DYE, EnumColor.ORANGE, BOPBlocks.ORANGE_COSMOS, BOPBlocks.BURNING_BLOSSOM);
        //Pink
        dye(consumer, basePath, Items.PINK_DYE, EnumColor.BRIGHT_PINK, BOPBlocks.PINK_DAFFODIL, BOPBlocks.PINK_HIBISCUS);
        //Cyan
        dye(consumer, basePath, Items.CYAN_DYE, EnumColor.DARK_AQUA, BOPBlocks.GLOWFLOWER);
        //Gray
        dye(consumer, basePath, Items.GRAY_DYE, EnumColor.DARK_GRAY, BOPBlocks.WILTED_LILY);
        //Light Blue
        dye(consumer, basePath, Items.LIGHT_BLUE_DYE, EnumColor.INDIGO, BOPBlocks.BLUE_HYDRANGEA);
        largeDye(consumer, basePath, Items.LIGHT_BLUE_DYE, EnumColor.INDIGO, BOPBlocks.ICY_IRIS);
        //Light Gray
        dye(consumer, basePath, Items.LIGHT_GRAY_DYE, EnumColor.GRAY, BOPBlocks.ENDBLOOM);
        //White
        dye(consumer, basePath, Items.WHITE_DYE, EnumColor.WHITE, BOPBlocks.WHITE_LAVENDER, BOPBlocks.WHITE_PETALS);
        largeDye(consumer, basePath, Items.WHITE_DYE, EnumColor.WHITE, BOPBlocks.TALL_WHITE_LAVENDER);
        //Yellow
        dye(consumer, basePath, Items.YELLOW_DYE, EnumColor.YELLOW, BOPBlocks.GOLDENROD);
    }

    private void dye(RecipeOutput consumer, String basePath, ItemLike output, EnumColor color, Block... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(inputs);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color).asStack(flowerRate)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/" + color.getRegistryPrefix()));
    }

    private void largeDye(RecipeOutput consumer, String basePath, ItemLike output, EnumColor color, Block... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(inputs);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, 4)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "dye/large_" + color.getRegistryPrefix()));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long largeFlowerRate = 6 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color).asStack(largeFlowerRate)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/large_" + color.getRegistryPrefix()));
    }
}