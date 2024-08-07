package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.recipe_viewer.color.PigmentExtractorColorDetails;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToPigmentEmiRecipe extends ItemStackToChemicalEmiRecipe<ItemStackToPigmentRecipe> {

    private final Supplier<ChemicalStack> output;

    public ItemStackToPigmentEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToPigmentRecipe> recipeHolder) {
        super(category, recipeHolder, TileEntityPigmentExtractor.BASE_TICKS_REQUIRED);
        output = getSupplier(recipe.getOutputDefinition(), ChemicalStack.EMPTY);
    }

    @Override
    protected GuiProgress addProgressBar(WidgetHolder widgetHolder, ProgressType type, int x, int y) {
        PigmentExtractorColorDetails colorDetails = new PigmentExtractorColorDetails();
        colorDetails.setIngredient(output);
        return super.addProgressBar(widgetHolder, type, x, y).colored(colorDetails);
    }
}