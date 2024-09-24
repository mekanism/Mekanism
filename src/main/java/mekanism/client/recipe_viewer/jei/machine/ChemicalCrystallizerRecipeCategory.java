package mekanism.client.recipe_viewer.jei.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.gui.element.custom.GuiQIOCrystallizerScreen;
import mekanism.client.gui.element.custom.GuiQIOCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChemicalCrystallizerRecipeCategory extends HolderRecipeCategory<ChemicalCrystallizerRecipe> {

    private static final String CHEMICAL_INPUT = "chemicalInput";
    private static final String DISPLAYED_ITEM = "displayedItem";

    private final OreInfo oreInfo = new OreInfo();
    private final GuiGauge<?> gauge;
    private final GuiSlot output;
    private final GuiQIOCrystallizerScreen screen;

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalCrystallizerRecipe> recipeType) {
        super(helper, recipeType);
        GaugeType type = GaugeType.STANDARD.with(DataType.INPUT);
        gauge = addElement(GuiChemicalGauge.getDummy(type, this, 7, 4));
        addSlot(SlotType.INPUT, 8, 65).with(SlotOverlay.PLUS);
        output = addSlot(SlotType.OUTPUT, 129, 57);
        addSimpleProgress(ProgressType.LARGE_RIGHT, 53, 61);
        screen = addElement(new GuiQIOCrystallizerScreen(this, 31, 13, 115, 42, oreInfo));
    }

    @Override
    public void draw(RecipeHolder<ChemicalCrystallizerRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our ore info
        oreInfo.currentRecipe = recipeHolder.value();
        oreInfo.ingredient = (ChemicalStack) recipeSlotsView.findSlotByName(CHEMICAL_INPUT)
              .flatMap(IRecipeSlotView::getDisplayedIngredient)
              .map(ITypedIngredient::getIngredient)
              .filter(ingredient -> ingredient instanceof ChemicalStack)
              .orElse(null);
        oreInfo.itemIngredient = getDisplayedStack(recipeSlotsView, DISPLAYED_ITEM, VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
        super.draw(recipeHolder, recipeSlotsView, guiGraphics, mouseX, mouseY);
        oreInfo.currentRecipe = null;
        oreInfo.ingredient = null;
        oreInfo.itemIngredient = ItemStack.EMPTY;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ChemicalCrystallizerRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        ChemicalCrystallizerRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
        ChemicalStackIngredient input = recipe.getInput();
        initChemical(builder, RecipeIngredientRole.INPUT, gauge, input.getRepresentations())
              .setSlotName(CHEMICAL_INPUT);
        List<ItemStack> displayItems = RecipeViewerUtils.getDisplayItems(input);
        if (!displayItems.isEmpty()) {
            initItem(builder, RecipeIngredientRole.RENDER_ONLY, screen.getSlotX(), screen.getSlotY(), displayItems).setSlotName(DISPLAYED_ITEM);
        }
    }

    private static class OreInfo implements IOreInfo {

        @Nullable
        private ChemicalCrystallizerRecipe currentRecipe;
        @Nullable
        private ChemicalStack ingredient;
        private ItemStack itemIngredient = ItemStack.EMPTY;

        @NotNull
        @Override
        public ChemicalStack getInputChemical() {
            if (ingredient == null || ingredient.isEmpty()) {
                return ChemicalStack.EMPTY;
            }
            return ingredient;
        }

        @Nullable
        @Override
        public ChemicalCrystallizerRecipe getRecipe() {
            return currentRecipe;
        }

        @NotNull
        @Override
        public ItemStack getRenderStack() {
            return itemIngredient;
        }

        @Override
        public boolean usesSequencedDisplay() {
            return false;
        }
    }
}