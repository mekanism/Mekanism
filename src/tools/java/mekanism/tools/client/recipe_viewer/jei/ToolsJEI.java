package mekanism.tools.client.recipe_viewer.jei;

import mekanism.client.recipe_viewer.jei.JEIAliasHelper;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.jei.RecipeRegistryHelper;
import mekanism.tools.client.recipe_viewer.aliases.ToolsAliasMapping;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Repairable;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ToolsJEI implements IModPlugin {

    @NotNull
    @Override
    public Identifier getPluginUid() {
        //Note: Can't use MekanismTools.rl, as JEI needs this in the constructor and the class may not be loaded yet.
        // we can still reference the modid though because of constant inlining
        return Identifier.fromNamespaceAndPath(MekanismTools.MODID, "jei_plugin");
    }

    @Override
    public void registerIngredientAliases(@NotNull IIngredientAliasRegistration registration) {
        new ToolsAliasMapping().addAliases(new JEIAliasHelper(registration));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registry) {
        if (MekanismJEI.shouldLoad()) {
            //Add the Anvil repair recipes to JEI for all the different tools and armors in Mekanism Tools
            //TODO - 26.1: check that we need to still do this - JEI doesn't seem to have a reference to Repairable (yet?)
            for (Holder<Item> toolsItem : ToolsItems.ITEMS.getEntries()) {
                RecipeRegistryHelper.addAnvilRecipes(registry, toolsItem, item -> {
                    Repairable repairable = item.components().get(DataComponents.REPAIRABLE);
                    if (repairable != null) {
                        return repairable.items();
                    }
                    return null;
                });
            }
        }
    }
}