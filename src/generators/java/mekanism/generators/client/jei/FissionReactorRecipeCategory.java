package mekanism.generators.client.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class FissionReactorRecipeCategory extends BaseRecipeCategory<GasToGasRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "radioactive.png");

    public FissionReactorRecipeCategory(IGuiHelper helper) {
        super(helper, GeneratorsBlocks.FISSION_REACTOR_CASING.getRegistryName(), GeneratorsLang.FISSION_REACTOR.translate(), 3, 12, 189, 70);
        icon = helper.drawableBuilder(iconRL, 0, 0, 18, 18)
              .setTextureSize(18, 18)
              .build();
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiInnerScreen(this, 45, 17, 105, 56));
        guiElements.add(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13)
              .setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13)
              .setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 152, 13)
              .setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.GRAY)));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 171, 13)
              .setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
    }

    private List<FluidStack> getWaterInput() {
        List<FluidStack> representations = new ArrayList<>();
        for (Fluid fluid : FluidTags.WATER.getAllElements()) {
            representations.add(new FluidStack(fluid, 1_000));
        }
        return representations;
    }

    @Nonnull
    @Override
    public Class<? extends GasToGasRecipe> getRecipeClass() {
        return GasToGasRecipe.class;
    }

    @Override
    public void setIngredients(GasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(getWaterInput()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputs(MekanismJEI.TYPE_GAS, Arrays.asList(MekanismGases.STEAM.getStack(1_000), recipe.getOutputRepresentation()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToGasRecipe recipe, @Nonnull IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        fluidStacks.init(0, true, 7 - xOffset, 14 - yOffset, 16, 58, 1, false, fluidOverlayLarge);
        fluidStacks.set(0, getWaterInput());
        initChemical(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getInput().getRepresentations(), true);
        initChemical(gasStacks, 1, false, 153 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(MekanismGases.STEAM.getStack(1_000)), true);
        initChemical(gasStacks, 2, false, 172 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getOutputRepresentation()), true);
    }
}