package mekanism.client.recipe_viewer.jei.machine;

import java.util.function.Supplier;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.recipe_viewer.color.PigmentMixerColorDetails;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PigmentMixerRecipeCategory extends ChemicalChemicalToChemicalRecipeCategory<Pigment, PigmentStack, PigmentMixingRecipe> {

    private final PigmentMixerColorDetails leftColorDetails;
    private final PigmentMixerColorDetails rightColorDetails;

    public PigmentMixerRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<PigmentMixingRecipe> recipeType) {
        super(helper, recipeType, MekanismJEI.TYPE_PIGMENT);
        rightArrow.colored(leftColorDetails = new PigmentMixerColorDetails());
        leftArrow.colored(rightColorDetails = new PigmentMixerColorDetails());
    }

    @Override
    protected GuiChemicalGauge<Pigment, PigmentStack, ?> getGauge(GaugeType type, int x, int y) {
        return GuiPigmentGauge.getDummy(type, this, x, y);
    }

    @Override
    public void draw(RecipeHolder<PigmentMixingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        leftColorDetails.setIngredient(getDisplayedStack(recipeSlotsView, LEFT_INPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY));
        rightColorDetails.setIngredient(getDisplayedStack(recipeSlotsView, RIGHT_INPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY));
        PigmentStack outputStack = getDisplayedStack(recipeSlotsView, OUTPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
        Supplier<PigmentStack> outputSupplier = () -> outputStack;
        leftColorDetails.setOutputIngredient(outputSupplier);
        rightColorDetails.setOutputIngredient(outputSupplier);
        super.draw(recipeHolder, recipeSlotsView, guiGraphics, mouseX, mouseY);
        leftColorDetails.reset();
        rightColorDetails.reset();
    }
}