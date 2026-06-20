package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.plugin.CraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ICraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.IRecipeComponentRegistrationHandler;
import mekanism.common.Mekanism;

@CraftTweakerPlugin(Mekanism.MODID + ":crt_plugin")
public class MekCraftTweakerPlugin implements ICraftTweakerPlugin {

    @Override
    public void registerRecipeComponents(IRecipeComponentRegistrationHandler handler) {
        //Input/Output
        handler.registerRecipeComponent(CrTRecipeComponents.CHEMICAL.input());
        handler.registerRecipeComponent(CrTRecipeComponents.CHEMICAL.output());
        //Misc
        handler.registerRecipeComponent(CrTRecipeComponents.CHANCE);
        handler.registerRecipeComponent(CrTRecipeComponents.ENERGY);
        handler.registerRecipeComponent(CrTRecipeComponents.PER_TICK_USAGE);
    }
}