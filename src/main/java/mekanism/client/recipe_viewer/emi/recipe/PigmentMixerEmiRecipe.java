package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.recipe_viewer.color.PigmentMixerColorDetails;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PigmentMixerEmiRecipe extends ChemicalChemicalToChemicalEmiRecipe<PigmentMixingRecipe> {

    private final Supplier<ChemicalStack> leftInput;
    private final Supplier<ChemicalStack> rightInput;
    private final Supplier<ChemicalStack> output;

    public PigmentMixerEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<PigmentMixingRecipe> recipeHolder) {
        super(category, recipeHolder);
        leftInput = getSupplier(recipe.getLeftInput().getRepresentations(), ChemicalStack.EMPTY);
        rightInput = getSupplier(recipe.getRightInput().getRepresentations(), ChemicalStack.EMPTY);
        output = getSupplier(recipe.getOutputDefinition(), ChemicalStack.EMPTY);
    }

    @Override
    protected GuiChemicalGauge getGauge(GaugeType type, int x, int y) {
        return GuiChemicalGauge.getDummy(type, this, x, y);
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