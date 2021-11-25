package mekanism.tools.client.jei;

import javax.annotation.Nonnull;
import mekanism.api.providers.IItemProvider;
import mekanism.client.jei.RecipeRegistryHelper;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class ToolsJEI implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return MekanismTools.rl("jei_plugin");
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registry) {
        //Add the Anvil repair recipes to JEI for all the different tools and armors in Mekanism Tools
        for (IItemProvider toolsItem : ToolsItems.ITEMS.getAllItems()) {
            RecipeRegistryHelper.addAnvilRecipes(registry, toolsItem, item -> {
                if (item instanceof IHasRepairType) {
                    return ((IHasRepairType) item).getRepairMaterial().getItems();
                }
                return null;
            });
        }
    }
}