package mekanism.client.jei.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;

public class ItemStackToPigmentRecipeCategory extends ItemStackToChemicalRecipeCategory<Pigment, PigmentStack, ItemStackToPigmentRecipe> {

    private final PigmentColorDetails currentDetails;

    public ItemStackToPigmentRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToPigmentRecipe> recipeType, IItemProvider mekanismBlock) {
        super(helper, recipeType, mekanismBlock, MekanismJEI.TYPE_PIGMENT, false);
        progressBar.colored(currentDetails = new PigmentColorDetails());
    }

    @Override
    protected GuiPigmentGauge getGauge(GaugeType type, int x, int y) {
        return GuiPigmentGauge.getDummy(type, this, x, y);
    }

    @Override
    public void draw(ItemStackToPigmentRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        currentDetails.ingredient = getDisplayedStack(recipeSlotsView, CHEMICAL_INPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
        super.draw(recipe, recipeSlotsView, matrixStack, mouseX, mouseY);
        currentDetails.reset();
    }

    private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {

        private PigmentColorDetails() {
            super(PigmentStack.EMPTY);
        }

        @Override
        public int getColorFrom() {
            return 0xFFFFFFFF;
        }

        @Override
        public int getColorTo() {
            return getColor(ingredient);
        }
    }
}