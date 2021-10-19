package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class ItemStackToChemicalRecipeCategory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends BaseRecipeCategory<RECIPE> {

    private final IIngredientType<STACK> ingredientType;
    protected final GuiProgress progressBar;
    private final GuiGauge<?> output;
    private final GuiSlot input;

    protected ItemStackToChemicalRecipeCategory(IGuiHelper helper, IItemProvider provider, IIngredientType<STACK> ingredientType, boolean isConversion) {
        this(helper, provider.getRegistryName(), provider.getTextComponent(), createIcon(helper, provider), ingredientType, isConversion);
    }

    protected ItemStackToChemicalRecipeCategory(IGuiHelper helper, ResourceLocation id, ITextComponent component, IDrawable icon, IIngredientType<STACK> ingredientType,
          boolean isConversion) {
        super(helper, id, component, icon, 20, 12, 132, 62);
        this.ingredientType = ingredientType;
        output = addElement(getGauge(GaugeType.STANDARD.with(DataType.OUTPUT), 131, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        progressBar = addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    protected abstract GuiChemicalGauge<CHEMICAL, STACK, ?> getGauge(GaugeType type, int x, int y);

    @Override
    public void setIngredients(RECIPE recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(ingredientType, Collections.singletonList(recipe.getOutputDefinitionNew()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RECIPE recipe, IIngredients ingredients) {
        initItem(recipeLayout.getItemStacks(), 0, true, input, recipe.getInput().getRepresentations());
        initChemical(recipeLayout.getIngredientsGroup(ingredientType), 0, false, output, recipe.getOutputDefinitionNew());
    }
}