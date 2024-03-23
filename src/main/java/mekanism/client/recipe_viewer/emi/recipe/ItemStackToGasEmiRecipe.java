package mekanism.client.recipe_viewer.emi.recipe;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToGasEmiRecipe extends ItemStackToChemicalEmiRecipe<Gas, GasStack, ItemStackToGasRecipe> {

    public ItemStackToGasEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToGasRecipe> recipeHolder, int processTime) {
        super(category, recipeHolder, processTime);
    }

    @Override
    protected GuiGasGauge getGauge(GaugeType type, int x, int y) {
        return GuiGasGauge.getDummy(type, this, x, y);
    }
}