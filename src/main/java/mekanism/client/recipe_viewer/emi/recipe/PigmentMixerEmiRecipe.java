package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.function.Supplier;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.recipe_viewer.color.PigmentMixerColorDetails;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PigmentMixerEmiRecipe extends ChemicalChemicalToChemicalEmiRecipe<Pigment, PigmentStack, PigmentMixingRecipe> {

    private final Supplier<PigmentStack> leftInput;
    private final Supplier<PigmentStack> rightInput;
    private final Supplier<PigmentStack> output;

    public PigmentMixerEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<PigmentMixingRecipe> recipeHolder) {
        super(category, recipeHolder);
        leftInput = getSupplier(recipe.getLeftInput().getRepresentations(), PigmentStack.EMPTY);
        rightInput = getSupplier(recipe.getRightInput().getRepresentations(), PigmentStack.EMPTY);
        output = getSupplier(recipe.getOutputDefinition(), PigmentStack.EMPTY);
    }

    @Override
    protected GuiChemicalGauge<Pigment, PigmentStack, ?> getGauge(GaugeType type, int x, int y) {
        return GuiPigmentGauge.getDummy(type, this, x, y);
    }

    @Override
    protected GuiProgress addConstantProgress(WidgetHolder widgetHolder, ProgressType type, int x, int y, boolean left) {
        PigmentMixerColorDetails colorDetails = new PigmentMixerColorDetails();
        colorDetails.setOutputIngredient(output);
        if (left) {
            colorDetails.setIngredient(leftInput);
        } else {
            colorDetails.setIngredient(rightInput);
        }
        return super.addConstantProgress(widgetHolder, type, x, y, left).colored(colorDetails);
    }
}