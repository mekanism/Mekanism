package mekanism.client.recipe_viewer.emi.recipe;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiInfusionGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToInfuseTypeEmiRecipe extends ItemStackToChemicalEmiRecipe<InfuseType, InfusionStack, ItemStackToInfuseTypeRecipe> {

    public ItemStackToInfuseTypeEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToInfuseTypeRecipe> recipeHolder) {
        super(category, recipeHolder, 0);
    }

    @Override
    protected GuiInfusionGauge getGauge(GaugeType type, int x, int y) {
        return GuiInfusionGauge.getDummy(type, this, x, y);
    }
}