package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.function.Supplier;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.recipe_viewer.color.PigmentExtractorColorDetails;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToPigmentEmiRecipe extends ItemStackToChemicalEmiRecipe<Pigment, PigmentStack, ItemStackToPigmentRecipe> {

    private final Supplier<PigmentStack> output;

    public ItemStackToPigmentEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToPigmentRecipe> recipeHolder) {
        super(category, recipeHolder, TileEntityPigmentExtractor.BASE_TICKS_REQUIRED);
        output = getSupplier(recipe.getOutputDefinition(), PigmentStack.EMPTY);
    }

    @Override
    protected GuiPigmentGauge getGauge(GaugeType type, int x, int y) {
        return GuiPigmentGauge.getDummy(type, this, x, y);
    }

    @Override
    protected GuiProgress addProgressBar(WidgetHolder widgetHolder, ProgressType type, int x, int y) {
        PigmentExtractorColorDetails colorDetails = new PigmentExtractorColorDetails();
        colorDetails.setIngredient(output);
        return super.addProgressBar(widgetHolder, type, x, y).colored(colorDetails);
    }
}