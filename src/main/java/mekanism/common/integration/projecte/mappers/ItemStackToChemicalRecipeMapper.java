package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;

@RecipeTypeMapper
public class ItemStackToChemicalRecipeMapper extends TypedMekanismRecipeMapper<ItemStackToChemicalRecipe> {

    public ItemStackToChemicalRecipeMapper() {
        super(ItemStackToChemicalRecipe.class, MekanismRecipeType.CHEMICAL_CONVERSION, MekanismRecipeType.OXIDIZING, MekanismRecipeType.PIGMENT_EXTRACTING);
    }

    @Override
    public String getName() {
        return "MekItemStackToChemical";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism item stack to chemical recipes. (Chemical conversion, Oxidizing, Pigment Extracting)";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackToChemicalRecipe recipe) {
        boolean handled = false;
        for (ItemStack representation : recipe.getInput().getRepresentations()) {
            ChemicalStack output = recipe.getOutput(representation);
            if (!output.isEmpty()) {
                IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                ingredientHelper.put(representation);
                if (ingredientHelper.addAsConversion(output)) {
                    handled = true;
                }
            }
        }
        return handled;
    }
}