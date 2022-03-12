package mekanism.client.jei.machine;

import javax.annotation.Nonnull;
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
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

public abstract class ItemStackToChemicalRecipeCategory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends BaseRecipeCategory<RECIPE> {

    protected static final String CHEMICAL_INPUT = "chemicalInput";

    private final IIngredientType<STACK> ingredientType;
    protected final GuiProgress progressBar;
    private final GuiGauge<?> output;
    private final GuiSlot input;

    protected ItemStackToChemicalRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, IItemProvider provider, IIngredientType<STACK> ingredientType,
          boolean isConversion) {
        this(helper, recipeType, provider.getTextComponent(), createIcon(helper, provider), ingredientType, isConversion);
    }

    protected ItemStackToChemicalRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, Component component, IDrawable icon,
          IIngredientType<STACK> ingredientType, boolean isConversion) {
        super(helper, recipeType, component, icon, 20, 12, 132, 62);
        this.ingredientType = ingredientType;
        output = addElement(getGauge(GaugeType.STANDARD.with(DataType.OUTPUT), 131, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        progressBar = addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    protected abstract GuiChemicalGauge<CHEMICAL, STACK, ?> getGauge(GaugeType type, int x, int y);

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, RECIPE recipe, @Nonnull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initChemical(builder, ingredientType, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition())
              .setSlotName(CHEMICAL_INPUT);
    }
}