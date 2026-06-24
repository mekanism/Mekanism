package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.util.RegistryUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RotaryCondensentratorRecipeCategory extends HolderRecipeCategory<RotaryRecipe> {

    private final boolean condensentrating;
    private final GuiGauge<?> chemicalGauge;
    private final GuiGauge<?> fluidGauge;

    public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
        super(helper, condensentrating ? RecipeViewerRecipeType.CONDENSENTRATING : RecipeViewerRecipeType.DECONDENSENTRATING);
        this.condensentrating = condensentrating;
        addElement(GuiTexturedElement.downArrow(this, 9, 44));
        addElement(GuiTexturedElement.downArrow(this, 159, 44));
        chemicalGauge = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
        fluidGauge = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        addSlot(SlotType.INPUT, 5, 25).with(SlotOverlay.PLUS);
        addSlot(SlotType.OUTPUT, 5, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.INPUT, 155, 25);
        addSlot(SlotType.OUTPUT, 155, 56);
        addConstantProgress(this.condensentrating ? ProgressType.LARGE_RIGHT : ProgressType.LARGE_LEFT, 64, 39);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<RotaryRecipe> recipeHolder, IFocusGroup focusGroup) {
        RotaryRecipe recipe = recipeHolder.value();
        ContextMap slotDisplayContext = getSlotDisplayContext();
        if (condensentrating) {
            if (recipe.hasChemicalToFluid()) {
                initChemical(builder, RecipeIngredientRole.INPUT, chemicalGauge, recipe.getChemicalInput().getRepresentations(slotDisplayContext));
                initFluid(builder, fluidGauge, recipe.getFluidOutputDefinition());
            }
        } else if (recipe.hasFluidToChemical()) {
            initFluid(builder, RecipeIngredientRole.INPUT, fluidGauge, recipe.getFluidInput().getRepresentations(slotDisplayContext));
            initChemical(builder, chemicalGauge, recipe.getChemicalOutputDefinition());
        }
    }

    @Override
    public Identifier getIdentifier(RecipeHolder<RotaryRecipe> recipe) {
        Identifier baseId = super.getIdentifier(recipe);
        if (condensentrating) {
            return RegistryUtils.synthetic(baseId, "condensentrating");
        }
        return RegistryUtils.synthetic(baseId, "decondensentrating");
    }
}