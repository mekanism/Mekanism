package mekanism.generators.client.recipe_viewer.jei;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import mekanism.api.heat.HeatAPI;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.BooleanStateDisplay.ActiveDisabled;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.recipe_viewer.recipe.FissionRecipeViewerRecipe;
import mekanism.generators.common.GeneratorsLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FissionReactorRecipeCategory extends BaseRecipeCategory<FissionRecipeViewerRecipe> {

    private final GuiGauge<?> coolantTank;
    private final GuiGauge<?> fuelTank;
    private final GuiGauge<?> heatedCoolantTank;
    private final GuiGauge<?> wasteTank;

    public FissionReactorRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<FissionRecipeViewerRecipe> recipeType) {
        super(helper, recipeType);
        addElement(new GuiInnerScreen(this, 45, 17, 105, 56, () -> List.of(
              MekanismLang.STATUS.translate(EnumColor.BRIGHT_GREEN, ActiveDisabled.of(true)),
              GeneratorsLang.GAS_BURN_RATE.translate(1.0),
              GeneratorsLang.FISSION_HEATING_RATE.translate(0),
              MekanismLang.TEMPERATURE.translate(EnumColor.BRIGHT_GREEN, MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, TemperatureUnit.KELVIN, true)),
              GeneratorsLang.FISSION_DAMAGE.translate(EnumColor.BRIGHT_GREEN, TextUtils.getPercent(0))
        )).spacing(1));
        coolantTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        fuelTank = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 25, 13).setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)));
        heatedCoolantTank = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 152, 13).setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.GRAY)));
        wasteTank = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 171, 13).setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, FissionRecipeViewerRecipe recipe, @NotNull IFocusGroup focusGroup) {
        //Handle the coolant either special cased water or the proper coolant
        if (recipe.inputCoolant() == null) {
            initFluid(builder, RecipeIngredientRole.INPUT, coolantTank, recipe.waterInput().getRepresentations());
        } else {
            initChemical(builder, RecipeIngredientRole.INPUT, coolantTank, recipe.inputCoolant().getRepresentations());
        }
        initChemical(builder, RecipeIngredientRole.INPUT, fuelTank, recipe.fuel().getRepresentations());
        initChemical(builder, RecipeIngredientRole.OUTPUT, heatedCoolantTank, Collections.singletonList(recipe.outputCoolant()));
        initChemical(builder, RecipeIngredientRole.OUTPUT, wasteTank, Collections.singletonList(recipe.waste()));
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(@NotNull FissionRecipeViewerRecipe recipe) {
        return recipe.id();
    }

    @NotNull
    @Override
    public Codec<FissionRecipeViewerRecipe> getCodec(@NotNull ICodecHelper codecHelper, @NotNull IRecipeManager recipeManager) {
        return FissionRecipeViewerRecipe.CODEC;
    }
}