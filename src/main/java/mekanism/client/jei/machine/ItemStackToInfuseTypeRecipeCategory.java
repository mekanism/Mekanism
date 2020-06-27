package mekanism.client.jei.machine;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiInfusionGauge;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;

public class ItemStackToInfuseTypeRecipeCategory extends ItemStackToChemicalRecipeCategory<InfuseType, InfusionStack, ItemStackToInfuseTypeRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "infuse_types.png");

    public ItemStackToInfuseTypeRecipeCategory(IGuiHelper helper, ResourceLocation id) {
        super(helper, id, MekanismLang.CONVERSION_INFUSION.translate(), MekanismJEI.TYPE_INFUSION, true);
        icon = helper.drawableBuilder(iconRL, 0, 0, 18, 18)
              .setTextureSize(18, 18)
              .build();
    }

    @Override
    protected GuiInfusionGauge getGauge(GaugeType type, int x, int y) {
        return GuiInfusionGauge.getDummy(type, this, x, y);
    }

    @Override
    public Class<? extends ItemStackToInfuseTypeRecipe> getRecipeClass() {
        return ItemStackToInfuseTypeRecipe.class;
    }
}