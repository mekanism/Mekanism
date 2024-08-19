package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.gui.machine.GuiChemicalCrystallizer.IOreInfo;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class ChemicalCrystallizerEmiRecipe extends MekanismEmiHolderRecipe<ChemicalCrystallizerRecipe> {

    private final List<ItemStack> displayItems;
    private final IOreInfo oreInfo;

    public ChemicalCrystallizerEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalCrystallizerRecipe> recipeHolder) {
        super(category, recipeHolder);
        addItemOutputDefinition(recipe.getOutputDefinition());
        ChemicalStackIngredient input = recipe.getInput();
        addInputDefinition(input);
        List<ChemicalStack> inputRepresentations = input.getRepresentations();
        displayItems = RecipeViewerUtils.getDisplayItems(input);
        oreInfo = new IOreInfo() {
            @Override
            public @NotNull ChemicalStack getInputChemical() {
                return inputRepresentations.isEmpty() ? ChemicalStack.EMPTY : RecipeViewerUtils.getCurrent(inputRepresentations);
            }

            @Override
            public ChemicalCrystallizerRecipe getRecipe() {
                return recipe;
            }

            @NotNull
            @Override
            public ItemStack getRenderStack() {
                return displayItems.isEmpty() ? ItemStack.EMPTY : RecipeViewerUtils.getCurrent(displayItems);
            }
        };
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        GaugeType type = GaugeType.STANDARD.with(DataType.INPUT);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type, this, 7, 4), input(0));
        addSlot(widgetHolder, SlotType.INPUT, 8, 65).with(SlotOverlay.PLUS);
        addSlot(widgetHolder, SlotType.OUTPUT, 129, 57, output(0)).recipeContext(this);
        addSimpleProgress(widgetHolder, ProgressType.LARGE_RIGHT, 53, 61, TileEntityChemicalCrystallizer.BASE_TICKS_REQUIRED);
        addElement(widgetHolder, new GuiInnerScreen(this, 31, 13, 115, 42, () -> GuiChemicalCrystallizer.getScreenRenderStrings(this.oreInfo)));
        GuiSlot slurryOreSlot = addElement(widgetHolder, new GuiSlot(SlotType.ORE, this, 128, 13).setRenderAboveSlots());
        initItem(widgetHolder, slurryOreSlot.getX(), slurryOreSlot.getY(), ingredient(displayItems));
    }
}