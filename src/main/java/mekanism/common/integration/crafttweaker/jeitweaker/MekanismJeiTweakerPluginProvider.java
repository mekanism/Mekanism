package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientType;
import com.blamejared.jeitweaker.common.api.plugin.JeiIngredientTypeRegistration;
import com.blamejared.jeitweaker.common.api.plugin.JeiTweakerPlugin;
import com.blamejared.jeitweaker.common.api.plugin.JeiTweakerPluginProvider;
import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mezz.jei.api.ingredients.IIngredientType;

@JeiTweakerPlugin(CrTConstants.JEI_PLUGIN_NAME)
public class MekanismJeiTweakerPluginProvider implements JeiTweakerPluginProvider {

    @Override
    public void registerIngredientTypes(JeiIngredientTypeRegistration registration) {
        registerType(registration, "gas", GasStack.class, ICrTGasStack.class, MekanismJEI.TYPE_GAS, CrTUtils.GAS_CONVERTER);
        registerType(registration, "infusion", InfusionStack.class, ICrTInfusionStack.class, MekanismJEI.TYPE_INFUSION, CrTUtils.INFUSION_CONVERTER);
        registerType(registration, "pigment", PigmentStack.class, ICrTPigmentStack.class, MekanismJEI.TYPE_PIGMENT, CrTUtils.PIGMENT_CONVERTER);
        registerType(registration, "slurry", SlurryStack.class, ICrTSlurryStack.class, MekanismJEI.TYPE_SLURRY, CrTUtils.SLURRY_CONVERTER);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> void
    registerType(JeiIngredientTypeRegistration registration, String type, Class<STACK> clazz, Class<CRT_STACK> crtClass, IIngredientType<STACK> ingredientType,
          Function<STACK, CRT_STACK> converter) {
        registration.registerIngredientType(
              JeiIngredientType.of(Mekanism.rl(type), clazz, crtClass),
              new JeiChemicalIngredientConverter<>(converter),
              ingredientType
        );
    }
}