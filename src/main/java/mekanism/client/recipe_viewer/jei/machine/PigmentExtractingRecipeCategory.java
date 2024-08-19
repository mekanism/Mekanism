package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.client.recipe_viewer.color.PigmentExtractorColorDetails;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PigmentExtractingRecipeCategory extends ItemStackToChemicalRecipeCategory<ItemStackToChemicalRecipe> {

    private final PigmentExtractorColorDetails currentDetails;

    public PigmentExtractingRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToChemicalRecipe> recipeType) {
        super(helper, recipeType, false);
        progressBar.colored(currentDetails = new PigmentExtractorColorDetails());
    }

    @Override
    public void draw(RecipeHolder<ItemStackToChemicalRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        currentDetails.setIngredient(getDisplayedStack(recipeSlotsView, CHEMICAL_INPUT, MekanismJEI.TYPE_CHEMICAL, ChemicalStack.EMPTY));
        super.draw(recipeHolder, recipeSlotsView, guiGraphics, mouseX, mouseY);
        currentDetails.reset();
    }
}