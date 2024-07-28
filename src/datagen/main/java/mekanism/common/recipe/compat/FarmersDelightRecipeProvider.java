package mekanism.common.recipe.compat;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vectorwing.farmersdelight.common.registry.ModItems;

@NothingNullByDefault
public class FarmersDelightRecipeProvider extends CompatRecipeProvider {

    public FarmersDelightRecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
    }

    private void addPrecisionSawmillRecipes(RecipeOutput consumer, String basePath) {
        //Beef
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.BEEF),
                    new ItemStack(ModItems.MINCED_BEEF.get(), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "minced_beef"));
        //Pork
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.PORKCHOP),
                    new ItemStack(ModItems.BACON.get(), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_bacon"));
        //Mutton
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.MUTTON),
                    new ItemStack(ModItems.MUTTON_CHOPS.get(), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_mutton_chops"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_MUTTON),
                    new ItemStack(ModItems.COOKED_MUTTON_CHOPS.get(), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "mutton_chops"));
        //Ham
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ModItems.HAM.get()),
                    new ItemStack(Items.PORKCHOP, 2),
                    new ItemStack(Items.BONE),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "ham_processing"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ModItems.SMOKED_HAM.get()),
                    new ItemStack(Items.COOKED_PORKCHOP, 2),
                    new ItemStack(Items.BONE),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smoked_ham_processing"));
        //Chicken
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.CHICKEN),
                    new ItemStack(ModItems.CHICKEN_CUTS.get(), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_chicken_cuts"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_CHICKEN),
                    new ItemStack(ModItems.COOKED_CHICKEN_CUTS.get(), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chicken_cuts"));
        //Salmon
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.SALMON),
                    new ItemStack(ModItems.SALMON_SLICE.get(), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_salmon_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_SALMON),
                    new ItemStack(ModItems.COOKED_SALMON_SLICE.get(), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "salmon_slice"));
        //Cod
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COD),
                    new ItemStack(ModItems.COD_SLICE.get(), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_cod_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_COD),
                    new ItemStack(ModItems.COOKED_COD_SLICE.get(), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "cod_slice"));
    }
}