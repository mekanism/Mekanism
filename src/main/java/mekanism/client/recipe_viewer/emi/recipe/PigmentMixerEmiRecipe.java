package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.recipe_viewer.color.PigmentMixerColorDetails;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public class PigmentMixerEmiRecipe extends ChemicalChemicalToChemicalEmiRecipe {

    private final Supplier<ChemicalStack> leftInput;
    private final Supplier<ChemicalStack> rightInput;
    private final Supplier<@Nullable ChemicalStackTemplate> output;

    public PigmentMixerEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalChemicalToChemicalRecipe> recipeHolder) {
        super(category, recipeHolder);
        //TODO - Emi: ContextMap
        ContextMap contextMap = ContextMap.EMPTY;
        leftInput = getSupplier(recipe.getLeftInput().getRepresentations(contextMap), ChemicalStack.EMPTY);
        rightInput = getSupplier(recipe.getRightInput().getRepresentations(contextMap), ChemicalStack.EMPTY);
        output = getSupplier(recipe.getOutputDefinition(contextMap), null);
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