package mekanism.client.jei.machine;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;

public class ItemStackToGasRecipeCategory extends ItemStackToChemicalRecipeCategory<Gas, GasStack, ItemStackToGasRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "gases.png");

    public ItemStackToGasRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock.getRegistryName(), mekanismBlock.getTextComponent(), MekanismJEI.TYPE_GAS, false);
    }

    public ItemStackToGasRecipeCategory(IGuiHelper helper, ResourceLocation id) {
        super(helper, id, MekanismLang.CONVERSION_GAS.translate(), MekanismJEI.TYPE_GAS, true);
        icon = helper.drawableBuilder(iconRL, 0, 0, 18, 18)
              .setTextureSize(18, 18)
              .build();
    }

    @Override
    protected GuiGasGauge getGauge(GaugeType type, int x, int y) {
        return GuiGasGauge.getDummy(type, this, x, y);
    }

    @Override
    public Class<? extends ItemStackToGasRecipe> getRecipeClass() {
        return ItemStackToGasRecipe.class;
    }
}