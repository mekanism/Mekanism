package mekanism.client.jei.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;

public class PigmentMixerRecipeCategory extends ChemicalChemicalToChemicalRecipeCategory<Pigment, PigmentStack, PigmentMixingRecipe> {

    private final PigmentColorDetails leftColorDetails;
    private final PigmentColorDetails rightColorDetails;

    public PigmentMixerRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<PigmentMixingRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.PIGMENT_MIXER, MekanismJEI.TYPE_PIGMENT, 3, 3, 170, 80);
        rightArrow.colored(leftColorDetails = new PigmentColorDetails());
        leftArrow.colored(rightColorDetails = new PigmentColorDetails());
    }

    @Override
    protected GuiChemicalGauge<Pigment, PigmentStack, ?> getGauge(GaugeType type, int x, int y) {
        return GuiPigmentGauge.getDummy(type, this, x, y);
    }

    @Override
    public void draw(PigmentMixingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        leftColorDetails.ingredient = getDisplayedStack(recipeSlotsView, LEFT_INPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
        rightColorDetails.ingredient = getDisplayedStack(recipeSlotsView, RIGHT_INPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
        leftColorDetails.outputIngredient = getDisplayedStack(recipeSlotsView, OUTPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
        rightColorDetails.outputIngredient = leftColorDetails.outputIngredient;
        super.draw(recipe, recipeSlotsView, matrixStack, mouseX, mouseY);
        leftColorDetails.reset();
        rightColorDetails.reset();
    }

    private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {

        private PigmentStack outputIngredient = PigmentStack.EMPTY;

        private PigmentColorDetails() {
            super(PigmentStack.EMPTY);
        }

        @Override
        public void reset() {
            super.reset();
            outputIngredient = empty;
        }

        @Override
        public int getColorFrom() {
            return getColor(ingredient);
        }

        @Override
        public int getColorTo() {
            return getColor(outputIngredient);
        }
    }
}