package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.color.PigmentExtractorColorDetails;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToPigmentRecipeCategory extends ItemStackToChemicalRecipeCategory<ItemStackToPigmentRecipe> {

    private final PigmentExtractorColorDetails currentDetails;

    public ItemStackToPigmentRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToPigmentRecipe> recipeType) {
        super(helper, recipeType, false);
        progressBar.colored(currentDetails = new PigmentExtractorColorDetails());
    }

    @Override
    public void draw(RecipeHolder<ItemStackToPigmentRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        currentDetails.setIngredient(getDisplayedStack(recipeSlotsView, CHEMICAL_INPUT, MekanismJEI.TYPE_PIGMENT, ChemicalStack.EMPTY));
        super.draw(recipeHolder, recipeSlotsView, guiGraphics, mouseX, mouseY);
        currentDetails.reset();
    }
}