package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.gui.element.custom.GuiQIOCrystallizerScreen;
import mekanism.client.gui.element.custom.GuiQIOCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalCrystallizerEmiRecipe extends MekanismEmiHolderRecipe<ChemicalCrystallizerRecipe> {

    private final List<ItemStack> displayItems;
    private final IOreInfo oreInfo;

    public ChemicalCrystallizerEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalCrystallizerRecipe> recipeHolder) {
        super(category, recipeHolder);
        //TODO - Emi: ContextMap
        ContextMap contextMap = ContextMap.EMPTY;
        addItemOutputDefinition(recipe.getOutputDefinition(contextMap));
        ChemicalStackIngredient input = recipe.getInput();
        addInputDefinition(input);
        List<ChemicalStack> inputRepresentations = input.getRepresentations(contextMap);
        displayItems = RecipeViewerUtils.getDisplayItems(input, contextMap);
        oreInfo = new IOreInfo() {
            @Override
            public ChemicalResource getInputChemical() {
                return inputRepresentations.isEmpty() ? ChemicalResource.EMPTY : ChemicalResource.of(RecipeViewerUtils.getCurrent(inputRepresentations));
            }

            @Override
            public ChemicalCrystallizerRecipe getRecipe() {
                return recipe;
            }

            @Override
            public ItemStack getRenderStack() {
                return displayItems.isEmpty() ? ItemStack.EMPTY : RecipeViewerUtils.getCurrent(displayItems);
            }

            @Override
            public boolean usesSequencedDisplay() {
                return false;
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
        GuiQIOCrystallizerScreen screen = addElement(widgetHolder, new GuiQIOCrystallizerScreen(this, 31, 13, 115, 42, oreInfo));
        initItem(widgetHolder, screen.getSlotX(), screen.getSlotY(), ingredient(displayItems));
    }
}