package mekanism.generators.client.jei;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.BooleanStateDisplay.ActiveDisabled;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.GeneratorsLang;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class FissionReactorRecipeCategory extends BaseRecipeCategory<GasToGasRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "radioactive.png");
    private final GuiGauge<?> coolantTank;
    private final GuiGauge<?> fuelTank;
    private final GuiGauge<?> heatedCoolantTank;
    private final GuiGauge<?> wasteTank;

    public FissionReactorRecipeCategory(IGuiHelper helper, ResourceLocation id) {
        super(helper, id, GeneratorsLang.FISSION_REACTOR.translate(), 6, 13, 182, 60);
        icon = helper.drawableBuilder(iconRL, 0, 0, 18, 18).setTextureSize(18, 18).build();
        addElement(new GuiInnerScreen(this, 45, 17, 105, 56, () -> Arrays.asList(
              MekanismLang.STATUS.translate(EnumColor.BRIGHT_GREEN, ActiveDisabled.of(true)),
              GeneratorsLang.GAS_BURN_RATE.translate(0.0),
              GeneratorsLang.FISSION_HEATING_RATE.translate(0),
              MekanismLang.TEMPERATURE.translate(EnumColor.BRIGHT_GREEN, MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, TemperatureUnit.KELVIN, true)),
              GeneratorsLang.FISSION_DAMAGE.translate(EnumColor.BRIGHT_GREEN, TextUtils.getPercent(0))
        )).spacing(2));
        coolantTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        fuelTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13).setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        heatedCoolantTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 152, 13).setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.GRAY)));
        wasteTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 171, 13).setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
    }

    private List<FluidStack> getWaterInput() {
        return FluidTags.WATER.getValues().stream().map(fluid -> new FluidStack(fluid, 1_000)).collect(Collectors.toList());
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
        //TODO - 10.1: Make this show all valid coolants (at the very least sodium, even if we don't allow for creating custom coolants via addons or stuff)
        initFluid(recipeLayout.getFluidStacks(), 0, true, coolantTank, getWaterInput());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, fuelTank, recipe.getInput().getRepresentations());
        initChemical(gasStacks, 1, false, heatedCoolantTank, Collections.singletonList(MekanismGases.STEAM.getStack(1_000)));
        initChemical(gasStacks, 2, false, wasteTank, Collections.singletonList(recipe.getOutputRepresentation()));
    }
}