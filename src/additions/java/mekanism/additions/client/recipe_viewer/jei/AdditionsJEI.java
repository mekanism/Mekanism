package mekanism.additions.client.recipe_viewer.jei;

import mekanism.additions.client.recipe_viewer.aliases.AdditionsAliasMapping;
import mekanism.additions.common.MekanismAdditions;
import mekanism.client.recipe_viewer.jei.JEIAliasHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class AdditionsJEI implements IModPlugin {

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        //Note: Can't use MekanismTools.rl, as JEI needs this in the constructor and the class may not be loaded yet.
        // we can still reference the modid though because of constant inlining
        return ResourceLocation.fromNamespaceAndPath(MekanismAdditions.MODID, "jei_plugin");
    }

    @Override
    public void registerIngredientAliases(@NotNull IIngredientAliasRegistration registration) {
        new AdditionsAliasMapping().addAliases(new JEIAliasHelper(registration));
    }
}