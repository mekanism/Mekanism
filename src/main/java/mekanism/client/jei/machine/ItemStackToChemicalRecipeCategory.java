package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class ItemStackToChemicalRecipeCategory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends BaseRecipeCategory<RECIPE> {

    private final IIngredientType<STACK> ingredientType;

    protected ItemStackToChemicalRecipeCategory(IGuiHelper helper, ResourceLocation id, ITextComponent component, IIngredientType<STACK> ingredientType,
          boolean isConversion) {
        super(helper, id, component, 20, 12, 132, 62);
        this.ingredientType = ingredientType;
        //Add the progress bar. addGuiElements gets called before isConversion is accessible
        guiElements.add(new GuiProgress(isConversion ? () -> 1 : () -> timer.getValue() / 20D, ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    protected abstract GuiChemicalGauge<CHEMICAL, STACK, ?> getGauge(GaugeType type, int x, int y);

    @Override
    protected void addGuiElements() {
        guiElements.add(getGauge(GaugeType.STANDARD, 133, 13));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 25, 35));
    }

    @Override
    public void setIngredients(RECIPE recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(ingredientType, recipe.getOutputDefinition());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RECIPE recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getInput().getRepresentations());
        IGuiIngredientGroup<STACK> chemicalStacks = recipeLayout.getIngredientsGroup(ingredientType);
        initChemical(chemicalStacks, 0, false, 134 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getOutputDefinition()), true);
    }
}