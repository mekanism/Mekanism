package mekanism.client.jei.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;

public class PressurizedReactionRecipeCategory extends BaseRecipeCategory<PressurizedReactionRecipe> {

    private static final String OUTPUT_GAS = "outputGas";

    private final GuiGauge<?> inputGas;
    private final GuiGauge<?> inputFluid;
    private final GuiSlot inputItem;
    private final GuiSlot outputItem;
    private final GuiGauge<?> outputGas;

    public PressurizedReactionRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<PressurizedReactionRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, 3, 10, 170, 60);
        //Note: This previously had a lang key for a shorter string. Though ideally especially due to translations
        // we will eventually instead just make the text scale
        inputItem = addSlot(SlotType.INPUT, 54, 35);
        outputItem = addSlot(SlotType.OUTPUT, 116, 35);
        addSlot(SlotType.POWER, 141, 17).with(SlotOverlay.POWER);
        inputFluid = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 10));
        inputGas = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 10));
        outputGas = addElement(GuiGasGauge.getDummy(GaugeType.SMALL.with(DataType.OUTPUT), this, 140, 40));
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        addSimpleProgress(ProgressType.RIGHT, 77, 38);
    }

    @Override
    public void draw(PressurizedReactionRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrix, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, matrix, mouseX, mouseY);
        if (recipeSlotsView.findSlotByName(OUTPUT_GAS).isEmpty()) {
            //If we don't have an output gas at all for this recipe, draw the bar overlay manually
            outputGas.drawBarOverlay(matrix);
        }
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, PressurizedReactionRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, inputItem, recipe.getInputSolid().getRepresentations());
        initFluid(builder, RecipeIngredientRole.INPUT, inputFluid, recipe.getInputFluid().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, inputGas, recipe.getInputGas().getRepresentations());
        List<ItemStack> itemOutputs = new ArrayList<>();
        List<GasStack> gasOutputs = new ArrayList<>();
        for (PressurizedReactionRecipeOutput output : recipe.getOutputDefinition()) {
            itemOutputs.add(output.item());
            gasOutputs.add(output.gas());
        }
        if (!itemOutputs.stream().allMatch(ItemStack::isEmpty)) {
            initItem(builder, RecipeIngredientRole.OUTPUT, outputItem, itemOutputs);
        }
        if (!gasOutputs.stream().allMatch(GasStack::isEmpty)) {
            initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, outputGas, gasOutputs)
                  .setSlotName(OUTPUT_GAS);
        }
    }
}