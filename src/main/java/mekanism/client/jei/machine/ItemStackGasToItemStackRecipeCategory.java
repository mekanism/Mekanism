package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class ItemStackGasToItemStackRecipeCategory extends BaseRecipeCategory<ItemStackGasToItemStackRecipe> {

    public ItemStackGasToItemStackRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock, 28, 16, 144, 54);
    }

    @Override
    public Class<? extends ItemStackGasToItemStackRecipe> getRecipeClass() {
        return ItemStackGasToItemStackRecipe.class;
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 63, 16));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 38, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 63, 52));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 116, 35));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, 164, 15));
        guiElements.add(new GuiEmptyBar(this, 68, 36, 6, 12));
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.BAR, this, 86, 38));
    }

    @Override
    public void setIngredients(ItemStackGasToItemStackRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        List<@NonNull GasStack> gasInputs = recipe.getChemicalInput().getRepresentations();
        long scale = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, scale)).collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackGasToItemStackRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 35, 0);
        itemStacks.init(1, false, 88, 19);
        itemStacks.init(2, false, 35, 36);
        itemStacks.set(0, recipe.getItemInput().getRepresentations());
        itemStacks.set(1, recipe.getOutputDefinition());
        GasStackIngredient gasInput = recipe.getChemicalInput();
        List<ItemStack> gasItemProviders = new ArrayList<>();
        List<@NonNull GasStack> gasInputs = gasInput.getRepresentations();
        List<GasStack> scaledGases = new ArrayList<>();
        long scale = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
        for (GasStack gas : gasInputs) {
            gasItemProviders.addAll(MekanismJEI.GAS_STACK_HELPER.getStacksFor(gas.getType(), true));
            //While we are already looping the gases ensure we scale it to get the average amount that will get used over all
            scaledGases.add(new GasStack(gas, scale));
        }
        itemStacks.set(2, gasItemProviders);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, 41, 21, 6, 12, scaledGases, false);
    }
}