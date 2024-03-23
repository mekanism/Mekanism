package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.recipe_viewer.color.PigmentExtractorColorDetails;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToPigmentRecipeCategory extends ItemStackToChemicalRecipeCategory<Pigment, PigmentStack, ItemStackToPigmentRecipe> {

    private final PigmentExtractorColorDetails currentDetails;

    public ItemStackToPigmentRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToPigmentRecipe> recipeType) {
        super(helper, recipeType, MekanismJEI.TYPE_PIGMENT, false);
        progressBar.colored(currentDetails = new PigmentExtractorColorDetails());
    }

    @Override
    protected GuiPigmentGauge getGauge(GaugeType type, int x, int y) {
        return GuiPigmentGauge.getDummy(type, this, x, y);
    }

    @Override
    public void draw(RecipeHolder<ItemStackToPigmentRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        currentDetails.setIngredient(getDisplayedStack(recipeSlotsView, CHEMICAL_INPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY));
        super.draw(recipeHolder, recipeSlotsView, guiGraphics, mouseX, mouseY);
        currentDetails.reset();
    }
}