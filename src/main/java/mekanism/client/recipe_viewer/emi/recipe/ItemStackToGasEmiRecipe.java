package mekanism.client.recipe_viewer.emi.recipe;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToGasEmiRecipe extends ItemStackToChemicalEmiRecipe<Chemical, ChemicalStack, ItemStackToGasRecipe> {

    public ItemStackToGasEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToGasRecipe> recipeHolder, int processTime) {
        super(category, recipeHolder, processTime);
    }

    @Override
    protected GuiChemicalGauge getGauge(GaugeType type, int x, int y) {
        return GuiChemicalGauge.getDummy(type, this, x, y);
    }
}