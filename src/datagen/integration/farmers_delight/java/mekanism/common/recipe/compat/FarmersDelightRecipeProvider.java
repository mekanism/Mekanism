package mekanism.common.recipe.compat;

import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import vectorwing.farmersdelight.common.registry.ModItems;

public class FarmersDelightRecipeProvider extends CompatRecipeProvider {

    public FarmersDelightRecipeProvider(HolderLookup.Provider registries, String modid) {
        super(registries, modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
    }

    private void addPrecisionSawmillRecipes(RecipeOutput consumer, String basePath) {
        Holder<Item> bone = items.getOrThrow(ItemIds.BONE);
        Holder<Item> boneMeal = items.getOrThrow(ItemIds.BONE_MEAL);
        //Beef
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.BEEF),
                    new ItemStackTemplate(ModItems.MINCED_BEEF.get(), 2)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "minced_beef"));
        //Pork
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.PORKCHOP),
                    new ItemStackTemplate(ModItems.BACON.get(), 2)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "raw_bacon"));
        //Mutton
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.MUTTON),
                    new ItemStackTemplate(ModItems.MUTTON_CHOPS.get(), 2)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "raw_mutton_chops"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_MUTTON),
                    new ItemStackTemplate(ModItems.COOKED_MUTTON_CHOPS.get(), 2)
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "mutton_chops"));
        //Ham
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ModItems.HAM.get()),
                    new ItemStackTemplate(items.getOrThrow(ItemIds.PORKCHOP), 2),
                    new ItemStackTemplate(bone),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "ham_processing"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ModItems.SMOKED_HAM.get()),
                    new ItemStackTemplate(items.getOrThrow(ItemIds.COOKED_PORKCHOP), 2),
                    new ItemStackTemplate(bone),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "smoked_ham_processing"));
        //Chicken
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.CHICKEN),
                    new ItemStackTemplate(ModItems.CHICKEN_CUTS.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "raw_chicken_cuts"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_CHICKEN),
                    new ItemStackTemplate(ModItems.COOKED_CHICKEN_CUTS.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "chicken_cuts"));
        //Salmon
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.SALMON),
                    new ItemStackTemplate(ModItems.SALMON_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "raw_salmon_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_SALMON),
                    new ItemStackTemplate(ModItems.COOKED_SALMON_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "salmon_slice"));
        //Cod
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COD),
                    new ItemStackTemplate(ModItems.COD_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "raw_cod_slice"));
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_COD),
                    new ItemStackTemplate(ModItems.COOKED_COD_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).addCondition(modLoaded)
              .save(consumer, Mekanism.rl(basePath + "cod_slice"));
    }
}