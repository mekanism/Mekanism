package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SawmillEmiRecipe extends MekanismEmiHolderRecipe<SawmillRecipe> {

    public SawmillEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<SawmillRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getInput());
        addItemOutputDefinition(recipe.getMainOutputDefinition());
        //TODO - 1.20.4: Improve how we display the chance for this
        addOutputDefinition(recipe.getSecondaryOutputDefinition().stream().map(EmiStack::of).map(stack -> stack.setChance((float) recipe.getSecondaryChance())).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiUpArrow(this, 60, 38));
        addSlot(widgetHolder, SlotType.INPUT, 56, 17, input(0));
        addSlot(widgetHolder, SlotType.POWER, 56, 53).with(SlotOverlay.POWER);
        GuiSlot output = addSlot(widgetHolder, SlotType.OUTPUT_WIDE, 112, 31);
        initItem(widgetHolder, output.getX() + 4, output.getY() + 4, output(0));
        initItem(widgetHolder, output.getX() + 20, output.getY() + 4, output(1));
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        addSimpleProgress(widgetHolder, ProgressType.BAR, 78, 38, TileEntityPrecisionSawmill.BASE_TICKS_REQUIRED);

        /*double secondaryChance = recipe.getSecondaryChance();
        if (secondaryChance > 0) {
            guiGraphics.drawString(getFont(), TextUtils.getPercent(secondaryChance), 104, 41, SpecialColors.TEXT_TITLE.argb(), false);
        }*/
    }
}