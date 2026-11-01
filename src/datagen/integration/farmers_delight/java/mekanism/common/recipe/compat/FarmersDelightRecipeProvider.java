package mekanism.common.recipe.compat;

import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.book.SawingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import vectorwing.farmersdelight.common.registry.ModItems;

public class FarmersDelightRecipeProvider extends CompatRecipeProvider {

    public FarmersDelightRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput, String modid) {
        super(recipeOutput, advancementOutput, modid);
    }

    @Override
    protected void addRecipes() {
        String basePath = getBasePath();
        addPrecisionSawmillRecipes(basePath + "sawing/");
    }

    private void addPrecisionSawmillRecipes(String basePath) {
        Holder<Item> bone = items.getOrThrow(ItemIds.BONE);
        Holder<Item> boneMeal = items.getOrThrow(ItemIds.BONE_MEAL);
        //Beef
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.BEEF),
                    new ItemStackTemplate(ModItems.MINCED_BEEF.get(), 2)
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_meat"))
              .unlockedBy(hasCriterion(ItemIds.BEEF))
              .save(output, basePath + "minced_beef");
        //Pork
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.PORKCHOP),
                    new ItemStackTemplate(ModItems.BACON.get(), 2)
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_cooked_meat"))
              .unlockedBy(hasCriterion(ItemIds.PORKCHOP))
              .save(output, basePath + "raw_bacon");
        //Mutton
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.MUTTON),
                    new ItemStackTemplate(ModItems.MUTTON_CHOPS.get(), 2)
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_meat"))
              .unlockedBy(hasCriterion(ItemIds.MUTTON))
              .save(output, basePath + "raw_mutton_chops");
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_MUTTON),
                    new ItemStackTemplate(ModItems.COOKED_MUTTON_CHOPS.get(), 2)
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_cooked_meat"))
              .unlockedBy(hasCriterion(ItemIds.COOKED_MUTTON))
              .save(output, basePath + "mutton_chops");
        //Ham
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ModItems.HAM.get()),
                    new ItemStackTemplate(items.getOrThrow(ItemIds.PORKCHOP), 2),
                    new ItemStackTemplate(bone),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_meat"))
              .unlockedBy(hasCriterion(ModItems.HAM.get()))
              .save(output, basePath + "ham_processing");
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ModItems.SMOKED_HAM.get()),
                    new ItemStackTemplate(items.getOrThrow(ItemIds.COOKED_PORKCHOP), 2),
                    new ItemStackTemplate(bone),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_cooked_meat"))
              .unlockedBy(hasCriterion(ModItems.SMOKED_HAM.get()))
              .save(output, basePath + "smoked_ham_processing");
        //Chicken
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.CHICKEN),
                    new ItemStackTemplate(ModItems.CHICKEN_CUTS.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_meat"))
              .unlockedBy(hasCriterion(ItemIds.CHICKEN))
              .save(output, basePath + "raw_chicken_cuts");
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_CHICKEN),
                    new ItemStackTemplate(ModItems.COOKED_CHICKEN_CUTS.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_cooked_meat"))
              .unlockedBy(hasCriterion(ItemIds.COOKED_CHICKEN))
              .save(output, basePath + "chicken_cuts");
        //Salmon
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.SALMON),
                    new ItemStackTemplate(ModItems.SALMON_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_fish"))
              .unlockedBy(hasCriterion(ItemIds.SALMON))
              .save(output, basePath + "raw_salmon_slice");
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_SALMON),
                    new ItemStackTemplate(ModItems.COOKED_SALMON_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_cooked_fish"))
              .unlockedBy(hasCriterion(ItemIds.COOKED_SALMON))
              .save(output, basePath + "salmon_slice");
        //Cod
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COD),
                    new ItemStackTemplate(ModItems.COD_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_fish"))
              .unlockedBy(hasCriterion(ItemIds.COD))
              .save(output, basePath + "raw_cod_slice");
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, ItemIds.COOKED_COD),
                    new ItemStackTemplate(ModItems.COOKED_COD_SLICE.get(), 2),
                    new ItemStackTemplate(boneMeal),
                    1
              ).category(SawingBookCategory.FOOD)
              .group(Mekanism.rl("fd_cooked_fish"))
              .unlockedBy(hasCriterion(ItemIds.COOKED_COD))
              .save(output, basePath + "cod_slice");
    }
}