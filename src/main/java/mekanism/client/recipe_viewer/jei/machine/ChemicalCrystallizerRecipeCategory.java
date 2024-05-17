package mekanism.client.recipe_viewer.jei.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.gui.machine.GuiChemicalCrystallizer.IOreInfo;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
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
    private final GuiSlot slurryOreSlot;

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalCrystallizerRecipe> recipeType) {
        super(helper, recipeType);
        gauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 4));
        addSlot(SlotType.INPUT, 8, 65).with(SlotOverlay.PLUS);
        output = addSlot(SlotType.OUTPUT, 129, 57);
        addSimpleProgress(ProgressType.LARGE_RIGHT, 53, 61);
        addElement(new GuiInnerScreen(this, 31, 13, 115, 42, () -> GuiChemicalCrystallizer.getScreenRenderStrings(this.oreInfo)));
        slurryOreSlot = addElement(new GuiSlot(SlotType.ORE, this, 128, 13).setRenderAboveSlots());
    }

    @Override
    public void draw(RecipeHolder<ChemicalCrystallizerRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our ore info
        oreInfo.currentRecipe = recipeHolder.value();
        oreInfo.ingredient = (ChemicalStack<?>) recipeSlotsView.findSlotByName(CHEMICAL_INPUT)
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
        ChemicalStackIngredient<?, ?, ?> input = recipe.getInput();
        switch (input) {
            case GasStackIngredient ingredient -> initChemical(builder, MekanismJEI.TYPE_GAS, ingredient);
            case InfusionStackIngredient ingredient -> initChemical(builder, MekanismJEI.TYPE_INFUSION, ingredient);
            case PigmentStackIngredient ingredient -> initChemical(builder, MekanismJEI.TYPE_PIGMENT, ingredient);
            case SlurryStackIngredient ingredient -> {
                initChemical(builder, MekanismJEI.TYPE_SLURRY, ingredient);
                List<ItemStack> displayItems = RecipeViewerUtils.getDisplayItems(ingredient);
                if (!displayItems.isEmpty()) {
                    initItem(builder, RecipeIngredientRole.RENDER_ONLY, slurryOreSlot, displayItems).setSlotName(DISPLAYED_ITEM);
                }
            }
        }
    }

    private <STACK extends ChemicalStack<?>> void initChemical(IRecipeLayoutBuilder builder, IIngredientType<STACK> type, ChemicalStackIngredient<?, STACK, ?> ingredient) {
        initChemical(builder, type, RecipeIngredientRole.INPUT, gauge, ingredient.getRepresentations())
              .setSlotName(CHEMICAL_INPUT);
    }

    private static class OreInfo implements IOreInfo {

        @Nullable
        private ChemicalCrystallizerRecipe currentRecipe;
        @Nullable
        private ChemicalStack<?> ingredient;
        private ItemStack itemIngredient = ItemStack.EMPTY;

        @NotNull
        @Override
        public BoxedChemicalStack getInputChemical() {
            if (ingredient == null || ingredient.isEmpty()) {
                return BoxedChemicalStack.EMPTY;
            }
            return BoxedChemicalStack.box(ingredient);
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
    }
}