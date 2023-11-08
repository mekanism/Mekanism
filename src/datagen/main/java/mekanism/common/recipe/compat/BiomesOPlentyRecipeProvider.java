package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.item.BOPItems;
import java.util.Arrays;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismPigments;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath) {
        addDyeRecipes(consumer, basePath);
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
        //TODO: Bio-fuel recipes?
    }

    //todo 1.20.2 replace with real fields if there's a Neo version released
    @SuppressWarnings("unchecked")
    public static RegistryObject<Block> getBOPBlock(String fieldName) {
        try {
            return (RegistryObject<Block>) BOPBlocks.class.getDeclaredField(fieldName).get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    private static RegistryObject<Item> getBOPItem(String fieldName) {
        try {
            return (RegistryObject<Item>) BOPItems.class.getDeclaredField(fieldName).get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPrecisionSawmillRecipes(RecipeOutput consumer, String basePath) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("DEAD_PLANKS"), getBOPItem("DEAD_BOAT"), getBOPItem("DEAD_CHEST_BOAT"), getBOPBlock("DEAD_DOOR"),
              getBOPBlock("DEAD_FENCE_GATE"), getBOPBlock("DEAD_PRESSURE_PLATE"), getBOPBlock("DEAD_TRAPDOOR"), getBOPBlock("DEAD_HANGING_SIGN"), "dead");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("FIR_PLANKS"), getBOPItem("FIR_BOAT"), getBOPItem("FIR_CHEST_BOAT"), getBOPBlock("FIR_DOOR"),
              getBOPBlock("FIR_FENCE_GATE"), getBOPBlock("FIR_PRESSURE_PLATE"), getBOPBlock("FIR_TRAPDOOR"), getBOPBlock("FIR_HANGING_SIGN"), "fir");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("HELLBARK_PLANKS"), getBOPItem("HELLBARK_BOAT"), getBOPItem("HELLBARK_CHEST_BOAT"), getBOPBlock("HELLBARK_DOOR"),
              getBOPBlock("HELLBARK_FENCE_GATE"), getBOPBlock("HELLBARK_PRESSURE_PLATE"), getBOPBlock("HELLBARK_TRAPDOOR"), getBOPBlock("HELLBARK_HANGING_SIGN"), "hellbark");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("JACARANDA_PLANKS"), getBOPItem("JACARANDA_BOAT"), getBOPItem("JACARANDA_CHEST_BOAT"), getBOPBlock("JACARANDA_DOOR"),
              getBOPBlock("JACARANDA_FENCE_GATE"), getBOPBlock("JACARANDA_PRESSURE_PLATE"), getBOPBlock("JACARANDA_TRAPDOOR"), getBOPBlock("JACARANDA_HANGING_SIGN"), "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("MAGIC_PLANKS"), getBOPItem("MAGIC_BOAT"), getBOPItem("MAGIC_CHEST_BOAT"), getBOPBlock("MAGIC_DOOR"),
              getBOPBlock("MAGIC_FENCE_GATE"), getBOPBlock("MAGIC_PRESSURE_PLATE"), getBOPBlock("MAGIC_TRAPDOOR"), getBOPBlock("MAGIC_HANGING_SIGN"), "magic");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("MAHOGANY_PLANKS"), getBOPItem("MAHOGANY_BOAT"), getBOPItem("MAHOGANY_CHEST_BOAT"), getBOPBlock("MAHOGANY_DOOR"),
              getBOPBlock("MAHOGANY_FENCE_GATE"), getBOPBlock("MAHOGANY_PRESSURE_PLATE"), getBOPBlock("MAHOGANY_TRAPDOOR"), getBOPBlock("MAHOGANY_HANGING_SIGN"), "mahogany");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("PALM_PLANKS"), getBOPItem("PALM_BOAT"), getBOPItem("PALM_CHEST_BOAT"), getBOPBlock("PALM_DOOR"),
              getBOPBlock("PALM_FENCE_GATE"), getBOPBlock("PALM_PRESSURE_PLATE"), getBOPBlock("PALM_TRAPDOOR"), getBOPBlock("PALM_HANGING_SIGN"), "palm");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("REDWOOD_PLANKS"), getBOPItem("REDWOOD_BOAT"), getBOPItem("REDWOOD_CHEST_BOAT"), getBOPBlock("REDWOOD_DOOR"),
              getBOPBlock("REDWOOD_FENCE_GATE"), getBOPBlock("REDWOOD_PRESSURE_PLATE"), getBOPBlock("REDWOOD_TRAPDOOR"), getBOPBlock("REDWOOD_HANGING_SIGN"), "redwood");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("UMBRAN_PLANKS"), getBOPItem("UMBRAN_BOAT"), getBOPItem("UMBRAN_CHEST_BOAT"), getBOPBlock("UMBRAN_DOOR"),
              getBOPBlock("UMBRAN_FENCE_GATE"), getBOPBlock("UMBRAN_PRESSURE_PLATE"), getBOPBlock("UMBRAN_TRAPDOOR"), getBOPBlock("UMBRAN_HANGING_SIGN"), "umbran");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, getBOPBlock("WILLOW_PLANKS"), getBOPItem("WILLOW_BOAT"), getBOPItem("WILLOW_CHEST_BOAT"), getBOPBlock("WILLOW_DOOR"),
              getBOPBlock("WILLOW_FENCE_GATE"), getBOPBlock("WILLOW_PRESSURE_PLATE"), getBOPBlock("WILLOW_TRAPDOOR"), getBOPBlock("WILLOW_HANGING_SIGN"), "willow");
    }

    private void addPrecisionSawmillWoodTypeRecipes(RecipeOutput consumer, String basePath, RegistryObject<Block> planks, RegistryObject<Item> boat,
          RegistryObject<Item> chestBoat, RegistryObject<Block> door, RegistryObject<Block> fenceGate, RegistryObject<Block> pressurePlate, RegistryObject<Block> trapdoor,
          RegistryObject<Block> hangingSign, String name) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks.get(), boat.get(), chestBoat.get(), door.get(), fenceGate.get(),
              tag(name + "_logs"), pressurePlate.get(), trapdoor.get(), hangingSign.get(), name, modLoaded);
    }

    private void addSandRecipes(RecipeOutput consumer, String basePath) {
        //Black Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "black", getBOPBlock("BLACK_SAND"), getBOPBlock("BLACK_SANDSTONE"), getBOPBlock("CHISELED_BLACK_SANDSTONE"),
              getBOPBlock("CUT_BLACK_SANDSTONE"), getBOPBlock("SMOOTH_BLACK_SANDSTONE"));
        //Orange Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "orange", getBOPBlock("ORANGE_SAND"), getBOPBlock("ORANGE_SANDSTONE"), getBOPBlock("CHISELED_ORANGE_SANDSTONE"),
              getBOPBlock("CUT_ORANGE_SANDSTONE"), getBOPBlock("SMOOTH_ORANGE_SANDSTONE"));
        //White Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "white", getBOPBlock("WHITE_SAND"), getBOPBlock("WHITE_SANDSTONE"), getBOPBlock("CHISELED_WHITE_SANDSTONE"),
              getBOPBlock("CUT_WHITE_SANDSTONE"), getBOPBlock("SMOOTH_WHITE_SANDSTONE"));
    }

    @SafeVarargs
    private void addSandStoneToSandRecipe(RecipeOutput consumer, String path, RegistryObject<Block> sand, RegistryObject<Block>... sandstones) {
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, path, modLoaded, sand.get(), toItemLike(sandstones));
    }

    private void addDyeRecipes(RecipeOutput consumer, String basePath) {
        //Red
        dye(consumer, basePath, Items.RED_DYE, EnumColor.RED, getBOPBlock("ROSE"));
        //Purple
        dye(consumer, basePath, Items.PURPLE_DYE, EnumColor.PURPLE, getBOPBlock("VIOLET"), getBOPBlock("LAVENDER"));
        //Magenta
        dye(consumer, basePath, Items.MAGENTA_DYE, EnumColor.PINK, getBOPBlock("WILDFLOWER"));
        //Orange
        dye(consumer, basePath, Items.ORANGE_DYE, EnumColor.ORANGE, getBOPBlock("ORANGE_COSMOS"), getBOPBlock("BURNING_BLOSSOM"));
        //Pink
        dye(consumer, basePath, Items.PINK_DYE, EnumColor.BRIGHT_PINK, getBOPBlock("PINK_DAFFODIL"), getBOPBlock("PINK_HIBISCUS"));
        //Cyan
        dye(consumer, basePath, Items.CYAN_DYE, EnumColor.DARK_AQUA, getBOPBlock("GLOWFLOWER"));
        //Gray
        dye(consumer, basePath, Items.GRAY_DYE, EnumColor.DARK_GRAY, getBOPBlock("WILTED_LILY"));
        //Light Blue
        dye(consumer, basePath, Items.LIGHT_BLUE_DYE, EnumColor.INDIGO, getBOPBlock("BLUE_HYDRANGEA"));
        //Yellow
        dye(consumer, basePath, Items.YELLOW_DYE, EnumColor.YELLOW, getBOPBlock("GOLDENROD"));
    }

    @SafeVarargs
    private void dye(RecipeOutput consumer, String basePath, ItemLike output, EnumColor color, RegistryObject<Block>... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(Ingredient.of(toItemLike(inputs)));
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

    @SafeVarargs
    private static ItemLike[] toItemLike(RegistryObject<Block>... ros) {
        return Arrays.stream(ros).map(RegistryObject::get).toArray(ItemLike[]::new);
    }
}