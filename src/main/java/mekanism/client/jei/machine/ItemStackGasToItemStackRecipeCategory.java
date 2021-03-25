package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
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

    private final GuiBar<?> gasInput;
    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;

    public ItemStackGasToItemStackRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock, 28, 16, 144, 54);
        input = addSlot(SlotType.INPUT, 64, 17);
        extra = addSlot(SlotType.EXTRA, 64, 53);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addSlot(SlotType.POWER, 39, 35).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        gasInput = addElement(new GuiEmptyBar(this, 68, 36, 6, 12));
        addSimpleProgress(ProgressType.BAR, 86, 38);
    }

    @Override
    public Class<? extends ItemStackGasToItemStackRecipe> getRecipeClass() {
        return ItemStackGasToItemStackRecipe.class;
    }

    @Override
    public void setIngredients(ItemStackGasToItemStackRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        List<@NonNull GasStack> gasInputs = recipe.getChemicalInput().getRepresentations();
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, gas.getAmount() * TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED))
              .collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackGasToItemStackRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        initItem(itemStacks, 0, true, input, recipe.getItemInput().getRepresentations());
        initItem(itemStacks, 1, false, output, recipe.getOutputDefinition());
        List<ItemStack> gasItemProviders = new ArrayList<>();
        List<GasStack> scaledGases = new ArrayList<>();
        for (GasStack gas : recipe.getChemicalInput().getRepresentations()) {
            gasItemProviders.addAll(MekanismJEI.GAS_STACK_HELPER.getStacksFor(gas.getType(), true));
            //While we are already looping the gases ensure we scale it to get the average amount that will get used over all
            scaledGases.add(new GasStack(gas, gas.getAmount() * TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED));
        }
        initItem(itemStacks, 2, true, extra, gasItemProviders);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, gasInput, scaledGases);
    }
}