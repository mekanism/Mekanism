package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public class PressurizedReactionRecipeCategory extends BaseRecipeCategory<PressurizedReactionRecipe> {

    private final GuiGauge<?> inputGas;
    private final GuiGauge<?> inputFluid;
    private final GuiSlot inputItem;
    private final GuiSlot outputItem;
    private final GuiGauge<?> outputGas;

    public PressurizedReactionRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, 3, 10, 170, 60);
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
    public Class<? extends PressurizedReactionRecipe> getRecipeClass() {
        return PressurizedReactionRecipe.class;
    }

    @Override
    public void setIngredients(PressurizedReactionRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInputSolid().getRepresentations()));
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInputFluid().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInputGas().getRepresentations()));
        Pair<List<@NonNull ItemStack>, @NonNull GasStack> outputDefinition = recipe.getOutputDefinition();
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(outputDefinition.getLeft()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, outputDefinition.getRight());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PressurizedReactionRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        initItem(itemStacks, 0, true, inputItem, recipe.getInputSolid().getRepresentations());
        Pair<List<@NonNull ItemStack>, @NonNull GasStack> outputDefinition = recipe.getOutputDefinition();
        initItem(itemStacks, 1, false, outputItem, outputDefinition.getLeft());
        initFluid(recipeLayout.getFluidStacks(), 0, true, inputFluid, recipe.getInputFluid().getRepresentations());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, inputGas, recipe.getInputGas().getRepresentations());
        initChemical(gasStacks, 1, false, outputGas, Collections.singletonList(outputDefinition.getRight()));
    }
}