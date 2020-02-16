package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.MekanismUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemStackGasToItemStackRecipeCategory extends BaseRecipeCategory<ItemStackGasToItemStackRecipe> {

    public ItemStackGasToItemStackRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock, ProgressBar progress) {
        super(helper, "mekanism:gui/advanced_machine.png", mekanismBlock, progress, 28, 16, 144, 54);
    }

    @Override
    public Class<? extends ItemStackGasToItemStackRecipe> getRecipeClass() {
        return ItemStackGasToItemStackRecipe.class;
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 55, 16));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 30, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 55, 52));
        guiElements.add(new GuiSlot(SlotType.OUTPUT_LARGE, this, 111, 30));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, progressBar, this, 77, 37));
    }

    @Override
    public void setIngredients(ItemStackGasToItemStackRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        @NonNull List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        int scale = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, scale)).collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackGasToItemStackRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 27, 0);
        itemStacks.init(1, false, 87, 18);
        itemStacks.init(2, false, 27, 36);
        itemStacks.set(0, recipe.getItemInput().getRepresentations());
        itemStacks.set(1, recipe.getOutputDefinition());
        GasStackIngredient gasInput = recipe.getGasInput();
        List<ItemStack> gasItemProviders = new ArrayList<>();
        @NonNull List<@NonNull GasStack> gasInputs = gasInput.getRepresentations();
        List<GasStack> scaledGases = new ArrayList<>();
        int scale = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
        for (GasStack gas : gasInputs) {
            gasItemProviders.addAll(getStacksForGas(gas.getType()));
            //While we are already looping the gases ensure we scale it to get the average amount that will get used over all
            scaledGases.add(new GasStack(gas, scale));
        }
        itemStacks.set(2, gasItemProviders);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 33, 21, 6, 12, scaledGases, false);
    }

    /**
     * Helper method for JEI to get the stacks to display for a specific type of gas
     */
    private static List<ItemStack> getStacksForGas(@Nonnull Gas type) {
        if (type.isEmptyType()) {
            return Collections.emptyList();
        }
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the gas tank of the type
        stacks.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, type));
        //See if there are any gas to item mappings
        List<ItemStackToGasRecipe> recipes = MekanismRecipeType.GAS_CONVERSION.getRecipes(world);
        for (ItemStackToGasRecipe recipe : recipes) {
            if (recipe.getOutputDefinition().isTypeEqual(type)) {
                stacks.addAll(recipe.getInput().getRepresentations());
            }
        }
        return stacks;
    }
}