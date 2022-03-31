package mekanism.generators.client.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.BooleanStateDisplay.ActiveDisabled;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.jei.recipe.FissionJEIRecipe;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FissionReactorRecipeCategory extends BaseRecipeCategory<FissionJEIRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "radioactive.png");
    private final GuiGauge<?> coolantTank;
    private final GuiGauge<?> fuelTank;
    private final GuiGauge<?> heatedCoolantTank;
    private final GuiGauge<?> wasteTank;

    public FissionReactorRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<FissionJEIRecipe> recipeType) {
        super(helper, recipeType, GeneratorsLang.FISSION_REACTOR.translate(), createIcon(helper, iconRL), 6, 13, 182, 60);
        addElement(new GuiInnerScreen(this, 45, 17, 105, 56, () -> List.of(
              MekanismLang.STATUS.translate(EnumColor.BRIGHT_GREEN, ActiveDisabled.of(true)),
              GeneratorsLang.GAS_BURN_RATE.translate(1.0),
              GeneratorsLang.FISSION_HEATING_RATE.translate(0),
              MekanismLang.TEMPERATURE.translate(EnumColor.BRIGHT_GREEN, MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, TemperatureUnit.KELVIN, true)),
              GeneratorsLang.FISSION_DAMAGE.translate(EnumColor.BRIGHT_GREEN, TextUtils.getPercent(0))
        )).spacing(2));
        coolantTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        fuelTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13).setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        heatedCoolantTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 152, 13).setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.GRAY)));
        wasteTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 171, 13).setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
    }

    private List<FluidStack> getWaterInput(FissionJEIRecipe recipe) {
        int amount = MathUtils.clampToInt(recipe.outputCoolant().getAmount());
        return TagUtils.tag(ForgeRegistries.FLUIDS, FluidTags.WATER).stream().map(fluid -> new FluidStack(fluid, amount)).toList();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, FissionJEIRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        //Handle the coolant either special cased water or the proper coolant
        if (recipe.inputCoolant() == null) {
            initFluid(builder, RecipeIngredientRole.INPUT, coolantTank, getWaterInput(recipe));
        } else {
            initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, coolantTank, recipe.inputCoolant().getRepresentations());
        }
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, fuelTank, recipe.fuel().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, heatedCoolantTank, Collections.singletonList(recipe.outputCoolant()));
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, wasteTank, Collections.singletonList(recipe.waste()));
    }

    public static List<FissionJEIRecipe> getFissionRecipes() {
        //Note: The recipes below ignore thermal conductivity and just take enthalpy into account and it rounds the amount of coolant
        //TODO: Eventually we may want to try and improve on that but for now this should be fine
        List<FissionJEIRecipe> recipes = new ArrayList<>();
        double energyPerFuel = MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue();
        //Special case water recipe
        long coolantAmount = Math.round(energyPerFuel * HeatUtils.getSteamEnergyEfficiency() / HeatUtils.getWaterThermalEnthalpy());
        recipes.add(new FissionJEIRecipe(null, IngredientCreatorAccess.gas().from(MekanismGases.FISSILE_FUEL, 1),
              MekanismGases.STEAM.getStack(coolantAmount), MekanismGases.NUCLEAR_WASTE.getStack(1)));
        //Go through all gases and add each coolant
        for (Gas gas : MekanismAPI.gasRegistry()) {
            CooledCoolant cooledCoolant = gas.get(CooledCoolant.class);
            if (cooledCoolant != null) {
                //If it is a cooled coolant add a recipe for it
                Gas heatedCoolant = cooledCoolant.getHeatedGas();
                coolantAmount = Math.round(energyPerFuel / cooledCoolant.getThermalEnthalpy());
                recipes.add(new FissionJEIRecipe(IngredientCreatorAccess.gas().from(gas, coolantAmount), IngredientCreatorAccess.gas().from(MekanismGases.FISSILE_FUEL, 1),
                      heatedCoolant.getStack(coolantAmount), MekanismGases.NUCLEAR_WASTE.getStack(1)));
            }
        }
        return recipes;
    }
}