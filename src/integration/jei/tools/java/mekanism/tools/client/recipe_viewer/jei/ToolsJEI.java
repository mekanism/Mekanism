package mekanism.tools.client.recipe_viewer.jei;

import mekanism.client.recipe_viewer.jei.JEIAliasHelper;
import mekanism.tools.client.recipe_viewer.aliases.ToolsAliasMapping;
import mekanism.tools.common.MekanismTools;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class ToolsJEI implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        //Note: Can't use MekanismTools.rl, as JEI needs this in the constructor and the class may not be loaded yet.
        // we can still reference the modid though because of constant inlining
        return Identifier.fromNamespaceAndPath(MekanismTools.MODID, "jei_plugin");
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        new ToolsAliasMapping().addAliases(new JEIAliasHelper(registration));
    }
}