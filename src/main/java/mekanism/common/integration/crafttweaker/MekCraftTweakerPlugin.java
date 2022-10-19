package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.plugin.CraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ICraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ILoaderRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.IRecipeComponentRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.IScriptLoadSourceRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.IScriptRunModuleConfiguratorRegistrationHandler;
import com.blamejared.crafttweaker.api.zencode.scriptrun.IScriptRunModuleConfigurator;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;

@CraftTweakerPlugin(Mekanism.MODID + ":crt_plugin")
public class MekCraftTweakerPlugin implements ICraftTweakerPlugin {

    @Override
    public void registerLoadSource(IScriptLoadSourceRegistrationHandler handler) {
        handler.registerLoadSource(CrTConstants.CONTENT_LOADER_SOURCE_ID);
    }

    @Override
    public void registerLoaders(ILoaderRegistrationHandler handler) {
        //TODO: Eventually we may want to limit what we actually provide access to of CrT's loader in our loader
        handler.registerLoader(CrTConstants.CONTENT_LOADER, CraftTweakerConstants.DEFAULT_LOADER_NAME);
    }

    @Override
    public void registerModuleConfigurators(IScriptRunModuleConfiguratorRegistrationHandler handler) {
        IScriptRunModuleConfigurator defaultConfig = IScriptRunModuleConfigurator.createDefault(CraftTweakerConstants.DEFAULT_LOADER_NAME);
        handler.registerConfigurator(CrTConstants.CONTENT_LOADER, defaultConfig);
    }

    @Override
    public void registerRecipeComponents(IRecipeComponentRegistrationHandler handler) {
        //Input/Output
        //Note: We only register the input for items as the output uses the one built into crafttweaker
        handler.registerRecipeComponent(CrTRecipeComponents.ITEM.input());
        handler.registerRecipeComponent(CrTRecipeComponents.FLUID.input());
        handler.registerRecipeComponent(CrTRecipeComponents.FLUID.output());
        for (ChemicalRecipeComponent<?, ?, ?, ?> chemicalComponent : CrTRecipeComponents.CHEMICAL_COMPONENTS) {
            handler.registerRecipeComponent(chemicalComponent.input());
            handler.registerRecipeComponent(chemicalComponent.output());
        }
        //Misc
        handler.registerRecipeComponent(CrTRecipeComponents.CHANCE);
        handler.registerRecipeComponent(CrTRecipeComponents.ENERGY);
    }
}