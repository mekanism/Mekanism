package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.color.PaintingColorDetails;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PaintingEmiRecipe extends MekanismEmiHolderRecipe<ItemStackChemicalToItemStackRecipe> {

    private final Supplier<ChemicalStack> chemicalInput;

    public PaintingEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackChemicalToItemStackRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getItemInput());
        addInputDefinition(recipe.getChemicalInput(), recipe.perTickUsage() ? TileEntityMetallurgicInfuser.BASE_TICKS_REQUIRED : 1);
        addItemOutputDefinition(recipe.getOutputDefinition());
        chemicalInput = getSupplier(recipe.getChemicalInput().getRepresentations(), ChemicalStack.EMPTY);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addSlot(widgetHolder, SlotType.INPUT, 45, 35, input(0));
        addSlot(widgetHolder, SlotType.POWER, 144, 35).with(SlotOverlay.POWER);
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 35, output(0)).recipeContext(this);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13), input(1));
        PaintingColorDetails paintingColorDetails = new PaintingColorDetails();
        paintingColorDetails.setIngredient(chemicalInput);
        addSimpleProgress(widgetHolder, ProgressType.LARGE_RIGHT, 64, 39, TileEntityPaintingMachine.BASE_TICKS_REQUIRED).colored(paintingColorDetails);
    }
}