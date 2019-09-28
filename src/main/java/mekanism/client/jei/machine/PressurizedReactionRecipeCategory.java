package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public class PressurizedReactionRecipeCategory extends BaseRecipeCategory<PressurizedReactionRecipe> {

    public PressurizedReactionRecipeCategory(IGuiHelper helper) {
        //TODO: Had a lang key for shorter name
        super(helper, "mekanism:gui/nei/pressurized_reaction_chamber.png", MekanismBlock.PRESSURIZED_REACTION_CHAMBER, ProgressBar.MEDIUM, 3, 11, 170, 68);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 53, 34));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 140, 18).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, guiLocation, 115, 34));
        guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD_YELLOW, this, guiLocation, 5, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD_RED, this, guiLocation, 28, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.SMALL_BLUE, this, guiLocation, 140, 40));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (float) timer.getValue() / 20F;
            }
        }, progressBar, this, guiLocation, 75, 37));
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
        @NonNull Pair<List<@NonNull ItemStack>, @NonNull GasStack> outputDefinition = recipe.getOutputDefinition();
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(outputDefinition.getLeft()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, outputDefinition.getRight());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PressurizedReactionRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 53 - xOffset, 34 - yOffset);
        itemStacks.init(1, false, 115 - xOffset, 34 - yOffset);
        itemStacks.set(0, recipe.getInputSolid().getRepresentations());
        @NonNull Pair<List<@NonNull ItemStack>, @NonNull GasStack> outputDefinition = recipe.getOutputDefinition();
        itemStacks.set(1, outputDefinition.getLeft());
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        @NonNull List<FluidStack> fluidInputs = recipe.getInputFluid().getRepresentations();
        int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 3, 0, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 29 - xOffset, 11 - yOffset, 16, 58, recipe.getInputGas().getRepresentations(), true);
        initGas(gasStacks, 1, false, 141 - xOffset, 41 - yOffset, 16, 28, outputDefinition.getRight(), true);
    }
}