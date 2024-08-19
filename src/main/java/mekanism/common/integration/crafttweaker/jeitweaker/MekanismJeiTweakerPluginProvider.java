package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientType;
import com.blamejared.jeitweaker.common.api.plugin.JeiIngredientTypeRegistration;
import com.blamejared.jeitweaker.common.api.plugin.JeiTweakerPlugin;
import com.blamejared.jeitweaker.common.api.plugin.JeiTweakerPluginProvider;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;

@JeiTweakerPlugin(CrTConstants.JEI_PLUGIN_NAME)
public class MekanismJeiTweakerPluginProvider implements JeiTweakerPluginProvider {

    @Override
    public void registerIngredientTypes(JeiIngredientTypeRegistration registration) {
        registration.registerIngredientType(
              JeiIngredientType.of(Mekanism.rl("chemical"), ChemicalStack.class, ICrTChemicalStack.class),
              new JeiChemicalIngredientConverter(),
              MekanismJEI.TYPE_CHEMICAL
        );
    }

}