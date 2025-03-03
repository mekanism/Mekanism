package mekanism.tools.client.recipe_viewer.jei;

import mekanism.client.recipe_viewer.jei.JEIAliasHelper;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.jei.RecipeRegistryHelper;
import mekanism.tools.client.recipe_viewer.aliases.ToolsAliasMapping;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TieredItem;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ToolsJEI implements IModPlugin {

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        //Note: Can't use MekanismTools.rl, as JEI needs this in the constructor and the class may not be loaded yet.
        // we can still reference the modid though because of constant inlining
        return ResourceLocation.fromNamespaceAndPath(MekanismTools.MODID, "jei_plugin");
    }

    @Override
    public void registerIngredientAliases(@NotNull IIngredientAliasRegistration registration) {
        new ToolsAliasMapping().addAliases(new JEIAliasHelper(registration));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registry) {
        if (MekanismJEI.shouldLoad()) {
            //Add the Anvil repair recipes to JEI for all the different tools and armors in Mekanism Tools
            for (Holder<Item> toolsItem : ToolsItems.ITEMS.getEntries()) {
                RecipeRegistryHelper.addAnvilRecipes(registry, toolsItem, item -> {
                    if (item instanceof ItemMekanismShield shieldItem) {
                        return shieldItem.getRepairMaterial().getItems();
                    } else if (item instanceof ArmorItem armorItem) {
                        return armorItem.getMaterial().value().repairIngredient().get().getItems();
                    } else if (item instanceof TieredItem tieredItem) {
                        return tieredItem.getTier().getRepairIngredient().getItems();
                    }
                    return null;
                });
            }
        }
    }
}