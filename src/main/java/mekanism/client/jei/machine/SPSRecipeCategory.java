package mekanism.client.jei.machine;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.text.ITextComponent;

public class SPSRecipeCategory extends BaseRecipeCategory<GasToGasRecipe> {

    private final IDrawable icon;

    public SPSRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.SPS_CASING.getRegistryName(), MekanismLang.SPS.translate(), 3, 12, 168, 74);
        icon = helper.createDrawableIngredient(MekanismItems.ANTIMATTER_PELLET.getItemStack());
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiInnerScreen(this, 26, 13, 122, 60));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 6, 13));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 150, 13));
        guiElements.add(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(timer.getValue() / 20D));
            }

            @Override
            public double getLevel() {
                return timer.getValue() / 20D;
            }
        }, 6, 75, getWidth() - 8, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Nonnull
    @Override
    public Class<? extends GasToGasRecipe> getRecipeClass() {
        return GasToGasRecipe.class;
    }

    @Override
    public void setIngredients(GasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputRepresentation());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToGasRecipe recipe, @Nonnull IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, 7 - xOffset, 14 - yOffset, 16, 58, recipe.getInput().getRepresentations(), true);
        initChemical(gasStacks, 2, false, 151 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getOutputRepresentation()), true);
    }
}