package mekanism.client.recipe_viewer.emi.recipe;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToInfuseTypeEmiRecipe extends ItemStackToChemicalEmiRecipe<Chemical, ChemicalStack, ItemStackToInfuseTypeRecipe> {

    public ItemStackToInfuseTypeEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToInfuseTypeRecipe> recipeHolder) {
        super(category, recipeHolder, 0);
    }

    @Override
    protected GuiChemicalGauge getGauge(GaugeType type, int x, int y) {
        return GuiChemicalGauge.getDummy(type, this, x, y);
    }
}