package mekanism.generators.client;

import javax.annotation.Nonnull;
import mekanism.client.jei.MekanismJEI;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class GeneratorsJEI implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return MekanismGenerators.rl("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        MekanismJEI.registerItemSubtypes(registry, GeneratorsItems.ITEMS.getAllItems());
    }
}