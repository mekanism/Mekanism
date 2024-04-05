package mekanism.tools.client.jei;

import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.jei.RecipeRegistryHelper;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
        if (MekanismJEI.shouldLoad()) {
            //Add the Anvil repair recipes to JEI for all the different tools and armors in Mekanism Tools
            for (Holder<Item> toolsItem : ToolsItems.ITEMS.getEntries()) {
                RecipeRegistryHelper.addAnvilRecipes(registry, toolsItem.value(), item -> item instanceof IHasRepairType hasRepairType ? hasRepairType.getRepairMaterial().getItems() : null);
            }
        }
    }
}