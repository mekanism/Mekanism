package mekanism.common.recipe.compat;

import biomesoplenty.api.item.BOPItems;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider(HolderLookup.Provider registries, String modid) {
        super(registries, modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addDyeRecipes(consumer, basePath);
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
    }

    private void addPrecisionSawmillRecipes(RecipeOutput consumer, String basePath) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.DEAD_PLANKS, BOPItems.DEAD_BOAT, BOPItems.DEAD_CHEST_BOAT, BOPItems.DEAD_DOOR,
              BOPItems.DEAD_FENCE_GATE, BOPItems.DEAD_PRESSURE_PLATE, BOPItems.DEAD_TRAPDOOR, BOPItems.DEAD_HANGING_SIGN, "dead");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.EMPYREAL_PLANKS, BOPItems.EMPYREAL_BOAT, BOPItems.EMPYREAL_CHEST_BOAT, BOPItems.EMPYREAL_DOOR,
              BOPItems.EMPYREAL_FENCE_GATE, BOPItems.EMPYREAL_PRESSURE_PLATE, BOPItems.EMPYREAL_TRAPDOOR, BOPItems.EMPYREAL_HANGING_SIGN, "empyreal");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.FIR_PLANKS, BOPItems.FIR_BOAT, BOPItems.FIR_CHEST_BOAT, BOPItems.FIR_DOOR,
              BOPItems.FIR_FENCE_GATE, BOPItems.FIR_PRESSURE_PLATE, BOPItems.FIR_TRAPDOOR, BOPItems.FIR_HANGING_SIGN, "fir");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.PINE_PLANKS, BOPItems.PINE_BOAT, BOPItems.PINE_CHEST_BOAT, BOPItems.PINE_DOOR,
              BOPItems.PINE_FENCE_GATE, BOPItems.PINE_PRESSURE_PLATE, BOPItems.PINE_TRAPDOOR, BOPItems.PINE_HANGING_SIGN, "pine");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.MAPLE_PLANKS, BOPItems.MAPLE_BOAT, BOPItems.MAPLE_CHEST_BOAT, BOPItems.MAPLE_DOOR,
              BOPItems.MAPLE_FENCE_GATE, BOPItems.MAPLE_PRESSURE_PLATE, BOPItems.MAPLE_TRAPDOOR, BOPItems.MAPLE_HANGING_SIGN, "maple");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.HELLBARK_PLANKS, BOPItems.HELLBARK_BOAT, BOPItems.HELLBARK_CHEST_BOAT, BOPItems.HELLBARK_DOOR,
              BOPItems.HELLBARK_FENCE_GATE, BOPItems.HELLBARK_PRESSURE_PLATE, BOPItems.HELLBARK_TRAPDOOR, BOPItems.HELLBARK_HANGING_SIGN, "hellbark");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.JACARANDA_PLANKS, BOPItems.JACARANDA_BOAT, BOPItems.JACARANDA_CHEST_BOAT, BOPItems.JACARANDA_DOOR,
              BOPItems.JACARANDA_FENCE_GATE, BOPItems.JACARANDA_PRESSURE_PLATE, BOPItems.JACARANDA_TRAPDOOR, BOPItems.JACARANDA_HANGING_SIGN, "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.MAGIC_PLANKS, BOPItems.MAGIC_BOAT, BOPItems.MAGIC_CHEST_BOAT, BOPItems.MAGIC_DOOR,
              BOPItems.MAGIC_FENCE_GATE, BOPItems.MAGIC_PRESSURE_PLATE, BOPItems.MAGIC_TRAPDOOR, BOPItems.MAGIC_HANGING_SIGN, "magic");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.MAHOGANY_PLANKS, BOPItems.MAHOGANY_BOAT, BOPItems.MAHOGANY_CHEST_BOAT, BOPItems.MAHOGANY_DOOR,
              BOPItems.MAHOGANY_FENCE_GATE, BOPItems.MAHOGANY_PRESSURE_PLATE, BOPItems.MAHOGANY_TRAPDOOR, BOPItems.MAHOGANY_HANGING_SIGN, "mahogany");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.PALM_PLANKS, BOPItems.PALM_BOAT, BOPItems.PALM_CHEST_BOAT, BOPItems.PALM_DOOR,
              BOPItems.PALM_FENCE_GATE, BOPItems.PALM_PRESSURE_PLATE, BOPItems.PALM_TRAPDOOR, BOPItems.PALM_HANGING_SIGN, "palm");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.REDWOOD_PLANKS, BOPItems.REDWOOD_BOAT, BOPItems.REDWOOD_CHEST_BOAT, BOPItems.REDWOOD_DOOR,
              BOPItems.REDWOOD_FENCE_GATE, BOPItems.REDWOOD_PRESSURE_PLATE, BOPItems.REDWOOD_TRAPDOOR, BOPItems.REDWOOD_HANGING_SIGN, "redwood");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.UMBRAN_PLANKS, BOPItems.UMBRAN_BOAT, BOPItems.UMBRAN_CHEST_BOAT, BOPItems.UMBRAN_DOOR,
              BOPItems.UMBRAN_FENCE_GATE, BOPItems.UMBRAN_PRESSURE_PLATE, BOPItems.UMBRAN_TRAPDOOR, BOPItems.UMBRAN_HANGING_SIGN, "umbran");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPItems.WILLOW_PLANKS, BOPItems.WILLOW_BOAT, BOPItems.WILLOW_CHEST_BOAT, BOPItems.WILLOW_DOOR,
              BOPItems.WILLOW_FENCE_GATE, BOPItems.WILLOW_PRESSURE_PLATE, BOPItems.WILLOW_TRAPDOOR, BOPItems.WILLOW_HANGING_SIGN, "willow");
    }

    private void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, String basePath, Item planks, Item boat, Item chestBoat, Item door, Item fenceGate,
          Item pressurePlate, Item trapdoor, Item hangingSign, String name) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, planks, boat, chestBoat, door, fenceGate,
              tag(name + "_logs"), pressurePlate, trapdoor, hangingSign, name, modLoaded);
    }

    private void addSandRecipes(RecipeOutput consumer, String basePath) {
        //Black Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "black", BOPItems.BLACK_SAND, BOPItems.BLACK_SANDSTONE, BOPItems.CHISELED_BLACK_SANDSTONE,
              BOPItems.CUT_BLACK_SANDSTONE, BOPItems.SMOOTH_BLACK_SANDSTONE);
        //Orange Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "orange", BOPItems.ORANGE_SAND, BOPItems.ORANGE_SANDSTONE, BOPItems.CHISELED_ORANGE_SANDSTONE,
              BOPItems.CUT_ORANGE_SANDSTONE, BOPItems.SMOOTH_ORANGE_SANDSTONE);
        //White Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "white", BOPItems.WHITE_SAND, BOPItems.WHITE_SANDSTONE, BOPItems.CHISELED_WHITE_SANDSTONE,
              BOPItems.CUT_WHITE_SANDSTONE, BOPItems.SMOOTH_WHITE_SANDSTONE);
    }

    private void addSandStoneToSandRecipe(RecipeOutput consumer, String path, Item sand, Item... sandstones) {
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, path, modLoaded, sand, sandstones);
    }

    private void addDyeRecipes(RecipeOutput consumer, String basePath) {
        //Brown
        largeDye(consumer, basePath, ItemIds.DYE.brown(), EnumColor.BROWN, BOPItems.CATTAIL);
        //Red
        dye(consumer, basePath, ItemIds.DYE.red(), EnumColor.RED, BOPItems.ORIGIN_ROSE, BOPItems.WATERLILY);
        //Green
        dye(consumer, basePath, ItemIds.DYE.green(), EnumColor.DARK_GREEN, BOPItems.TINY_CACTUS);
        //Purple
        dye(consumer, basePath, ItemIds.DYE.purple(), EnumColor.PURPLE, BOPItems.VIOLET, BOPItems.LAVENDER);
        largeDye(consumer, basePath, ItemIds.DYE.purple(), EnumColor.PURPLE, BOPItems.TALL_LAVENDER);
        //Magenta
        dye(consumer, basePath, ItemIds.DYE.magenta(), EnumColor.PINK, BOPItems.PURPLE_WILDFLOWERS);
        //Orange
        dye(consumer, basePath, ItemIds.DYE.orange(), EnumColor.ORANGE, BOPItems.ORANGE_COSMOS, BOPItems.BURNING_BLOSSOM);
        //Pink
        dye(consumer, basePath, ItemIds.DYE.pink(), EnumColor.BRIGHT_PINK, BOPItems.PINK_DAFFODIL, BOPItems.PINK_HIBISCUS);
        //Cyan
        dye(consumer, basePath, ItemIds.DYE.cyan(), EnumColor.DARK_AQUA, BOPItems.GLOWFLOWER);
        //Gray
        dye(consumer, basePath, ItemIds.DYE.gray(), EnumColor.DARK_GRAY, BOPItems.WILTED_LILY);
        //Light Blue
        dye(consumer, basePath, ItemIds.DYE.lightBlue(), EnumColor.INDIGO, BOPItems.BLUE_HYDRANGEA);
        largeDye(consumer, basePath, ItemIds.DYE.lightBlue(), EnumColor.INDIGO, BOPItems.ICY_IRIS);
        //Light Gray
        dye(consumer, basePath, ItemIds.DYE.lightGray(), EnumColor.GRAY, BOPItems.ENDBLOOM);
        //White
        dye(consumer, basePath, ItemIds.DYE.white(), EnumColor.WHITE, BOPItems.WHITE_LAVENDER, BOPItems.WHITE_PETALS);
        largeDye(consumer, basePath, ItemIds.DYE.white(), EnumColor.WHITE, BOPItems.TALL_WHITE_LAVENDER);
        //Yellow
        dye(consumer, basePath, ItemIds.DYE.yellow(), EnumColor.YELLOW, BOPItems.GOLDENROD);
    }

    private void dye(RecipeOutput consumer, String basePath, ResourceKey<Item> output, EnumColor color, Item... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(inputs);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStackTemplate(this.items.getOrThrow(output), 2)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        int flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    chemicalTemplate(ChemicalIds.SIMPLE_PIGMENTS.pick(color), flowerRate)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "pigment_extracting/" + color.getRegistryPrefix()));
    }

    private void largeDye(RecipeOutput consumer, String basePath, ResourceKey<Item> output, EnumColor color, Item... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(inputs);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStackTemplate(this.items.getOrThrow(output), 4)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "dye/large_" + color.getRegistryPrefix()));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        int largeFlowerRate = 6 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    chemicalTemplate(ChemicalIds.SIMPLE_PIGMENTS.pick(color), largeFlowerRate)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "pigment_extracting/large_" + color.getRegistryPrefix()));
    }
}