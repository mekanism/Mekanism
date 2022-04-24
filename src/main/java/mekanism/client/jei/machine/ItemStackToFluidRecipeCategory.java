package mekanism.client.jei.machine;

import javax.annotation.Nonnull;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
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
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

public class ItemStackToFluidRecipeCategory extends BaseRecipeCategory<ItemStackToFluidRecipe> {

    protected final GuiProgress progressBar;
    private final GuiGauge<?> output;
    private final GuiSlot input;

    public ItemStackToFluidRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToFluidRecipe> recipeType, IItemProvider provider, boolean isConversion) {
        this(helper, recipeType, provider.getTextComponent(), createIcon(helper, provider), isConversion);
    }

    public ItemStackToFluidRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToFluidRecipe> recipeType, Component component, IDrawable icon,
          boolean isConversion) {
        super(helper, recipeType, component, icon, 20, 12, 132, 62);
        output = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        progressBar = addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, ItemStackToFluidRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initFluid(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}