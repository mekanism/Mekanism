package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ChemicalOxidizerRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalOxidizerEmiRecipe extends MekanismEmiHolderRecipe<ChemicalOxidizerRecipe> {

    public ChemicalOxidizerEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalOxidizerRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getInput());
        addOutputDefinition(recipe.getOutputDefinition().stream().<EmiStack>map(stack -> ChemicalEmiStack.create(stack.getChemicalStack())).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13), output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.INPUT, 28, 36, input(0));
        addElement(widgetHolder, new GuiHorizontalPowerBar(this, RecipeViewerUtils.FULL_BAR, 115, 75));
    }
}