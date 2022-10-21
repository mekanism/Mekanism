package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PigmentExtractingIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_PIGMENT)
public abstract class ItemStackToPigmentRecipeManager extends ItemStackToChemicalRecipeManager<Pigment, PigmentStack, ICrTPigmentStack, ItemStackToPigmentRecipe> {

    protected ItemStackToPigmentRecipeManager(IMekanismRecipeTypeProvider<ItemStackToPigmentRecipe, ?> recipeType) {
        super(recipeType);
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_PIGMENT_EXTRACTING)
    public static class PigmentExtractingRecipeManager extends ItemStackToPigmentRecipeManager {

        public static final PigmentExtractingRecipeManager INSTANCE = new PigmentExtractingRecipeManager();

        private PigmentExtractingRecipeManager() {
            super(MekanismRecipeType.PIGMENT_EXTRACTING);
        }

        @Override
        protected ItemStackToPigmentRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
            return new PigmentExtractingIRecipe(id, input, output);
        }
    }
}