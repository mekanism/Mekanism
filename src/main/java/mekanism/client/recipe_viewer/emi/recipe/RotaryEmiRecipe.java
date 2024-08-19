package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class RotaryEmiRecipe extends MekanismEmiRecipe<RotaryRecipe> {

    private final RecipeHolder<RotaryRecipe> recipeHolder;
    private final boolean condensentrating;

    public RotaryEmiRecipe(MekanismEmiRecipeCategory category, ResourceLocation id, RecipeHolder<RotaryRecipe> recipeHolder, boolean condensentrating) {
        super(category, id, recipeHolder.value());
        this.recipeHolder = recipeHolder;
        this.condensentrating = condensentrating;
        if (condensentrating) {
            if (recipe.hasChemicalToFluid()) {
                addInputDefinition(recipe.getChemicalInput());
                addFluidOutputDefinition(recipe.getFluidOutputDefinition());
            } else {
                throw new IllegalArgumentException("Condensentrating recipes require a chemical to fluid component");
            }
        } else if (recipe.hasFluidToChemical()) {
            addInputDefinition(recipe.getFluidInput());
            addChemicalOutputDefinition(recipe.getChemicalOutputDefinition());
        } else {
            throw new IllegalArgumentException("Decondensentrating recipes require a fluid to chemical component");
        }
    }

    @Nullable
    @Override
    public RecipeHolder<RotaryRecipe> getBackingRecipe() {
        return recipeHolder;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiDownArrow(this, 159, 44));
        SlotWidget leftWidget = initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 25, 13), condensentrating ? input(0) : output(0));
        SlotWidget rightWidget = initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 133, 13), condensentrating ? output(0) : input(0));
        if (condensentrating) {
            rightWidget.recipeContext(this);
        } else {
            leftWidget.recipeContext(this);
        }
        addSlot(widgetHolder, SlotType.INPUT, 5, 25).with(SlotOverlay.PLUS);
        addSlot(widgetHolder, SlotType.OUTPUT, 5, 56).with(SlotOverlay.MINUS);
        addSlot(widgetHolder, SlotType.INPUT, 155, 25);
        addSlot(widgetHolder, SlotType.OUTPUT, 155, 56);
        addConstantProgress(widgetHolder, this.condensentrating ? ProgressType.LARGE_RIGHT : ProgressType.LARGE_LEFT, 64, 39);
    }
}