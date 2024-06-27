package mekanism.client.recipe_viewer.jei.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackToFluidOptionalItemRecipeCategory extends BaseRecipeCategory<ItemStackToFluidOptionalItemRecipe> {

    private static final String OUTPUT_ITEM = "outputItem";

    private final GuiProgress progressBar;
    private final GuiGauge<?> outputTank;
    private final GuiSlot outputItem;
    private final GuiSlot input;

    public ItemStackToFluidOptionalItemRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToFluidOptionalItemRecipe> recipeType, boolean isConversion) {
        super(helper, recipeType);
        input = addSlot(SlotType.INPUT, 26, 36);
        outputItem = addSlot(SlotType.OUTPUT, 110, 36);
        outputTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        progressBar = addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 54, 40));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ItemStackToFluidOptionalItemRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        List<FluidOptionalItemOutput> outputDefinition = recipe.getOutputDefinition();
        List<FluidStack> fluidOutputs = new ArrayList<>(outputDefinition.size());
        List<ItemStack> itemOutputs = new ArrayList<>();
        for (FluidOptionalItemOutput output : outputDefinition) {
            fluidOutputs.add(output.fluid());
            itemOutputs.add(output.optionalItem());
        }
        initFluid(builder, RecipeIngredientRole.OUTPUT, outputTank, fluidOutputs);
        if (!itemOutputs.stream().allMatch(ConstantPredicates.ITEM_EMPTY)) {
            initItem(builder, RecipeIngredientRole.OUTPUT, outputItem, itemOutputs)
                  .setSlotName(OUTPUT_ITEM);
        }
    }
}