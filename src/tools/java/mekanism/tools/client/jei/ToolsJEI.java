package mekanism.tools.client.jei;

import mekanism.api.providers.IItemProvider;
import mekanism.client.jei.RecipeRegistryHelper;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ToolsJEI implements IModPlugin {

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return MekanismTools.rl("jei_plugin");
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registry) {
        //Add the Anvil repair recipes to JEI for all the different tools and armors in Mekanism Tools
        for (IItemProvider toolsItem : ToolsItems.ITEMS.getAllItems()) {
            RecipeRegistryHelper.addAnvilRecipes(registry, toolsItem, item -> item instanceof IHasRepairType hasRepairType ? hasRepairType.getRepairMaterial().getItems() : null);
        }
    }
}