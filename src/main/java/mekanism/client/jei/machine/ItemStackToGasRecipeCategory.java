package mekanism.client.jei.machine;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

public class ItemStackToGasRecipeCategory extends ItemStackToChemicalRecipeCategory<Gas, GasStack, ItemStackToGasRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "gases.png");

    public ItemStackToGasRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToGasRecipe> recipeType, IItemProvider mekanismBlock) {
        super(helper, recipeType, mekanismBlock, MekanismJEI.TYPE_GAS, false);
    }

    public ItemStackToGasRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToGasRecipe> recipeType) {
        super(helper, recipeType, MekanismLang.CONVERSION_GAS.translate(), createIcon(helper, iconRL), MekanismJEI.TYPE_GAS, true);
    }

    @Override
    protected GuiGasGauge getGauge(GaugeType type, int x, int y) {
        return GuiGasGauge.getDummy(type, this, x, y);
    }
}