package mekanism.common.recipe.compat;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vectorwing.farmersdelight.common.registry.ModItems;

//TODO - 1.21: Remove this if we want to release before Farmers delight so that our generated recipes go away as our syntax has changed
@NothingNullByDefault
public class FarmersDelightRecipeProvider extends CompatRecipeProvider {

    public FarmersDelightRecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath) {
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
    }

    //TODO - 1.20.2: replace with real fields if there's a Neo version released
    @SuppressWarnings("unchecked")
    private static Holder<Item> getFDItem(String fieldName) {
        try {
            return (Holder<Item>) ModItems.class.getDeclaredField(fieldName).get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPrecisionSawmillRecipes(RecipeOutput consumer, String basePath) {
        //Beef
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.BEEF),
                    new ItemStack(getFDItem("MINCED_BEEF"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "minced_beef"));
        //Pork
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.PORKCHOP),
                    new ItemStack(getFDItem("BACON"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_bacon"));
        //Mutton
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.MUTTON),
                    new ItemStack(getFDItem("MUTTON_CHOPS"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_mutton_chops"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_MUTTON),
                    new ItemStack(getFDItem("COOKED_MUTTON_CHOPS"), 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "mutton_chops"));
        //Ham
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().fromHolder(getFDItem("HAM")),
                    new ItemStack(Items.PORKCHOP, 2),
                    new ItemStack(Items.BONE),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "ham_processing"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().fromHolder(getFDItem("SMOKED_HAM")),
                    new ItemStack(Items.COOKED_PORKCHOP, 2),
                    new ItemStack(Items.BONE),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smoked_ham_processing"));
        //Chicken
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.CHICKEN),
                    new ItemStack(getFDItem("CHICKEN_CUTS"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_chicken_cuts"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_CHICKEN),
                    new ItemStack(getFDItem("COOKED_CHICKEN_CUTS"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chicken_cuts"));
        //Salmon
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.SALMON),
                    new ItemStack(getFDItem("SALMON_SLICE"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_salmon_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_SALMON),
                    new ItemStack(getFDItem("COOKED_SALMON_SLICE"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "salmon_slice"));
        //Cod
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COD),
                    new ItemStack(getFDItem("COD_SLICE"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "raw_cod_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(Items.COOKED_COD),
                    new ItemStack(getFDItem("COOKED_COD_SLICE"), 2),
                    new ItemStack(Items.BONE_MEAL),
                    1
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "cod_slice"));
    }
}