package mekanism.generators.client.jei;

import javax.annotation.Nonnull;
import mekanism.client.jei.CatalystRegistryHelper;
import mekanism.client.jei.MekanismJEI;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class GeneratorsJEI implements IModPlugin {

    private static final ResourceLocation FISSION = MekanismGenerators.rl("fission");

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return MekanismGenerators.rl("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistration registry) {
        MekanismJEI.registerItemSubtypes(registry, GeneratorsItems.ITEMS.getAllItems());
        MekanismJEI.registerItemSubtypes(registry, GeneratorsBlocks.BLOCKS.getAllBlocks());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FissionReactorRecipeCategory(guiHelper, FISSION));
    }

    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, FISSION, GeneratorsBlocks.FISSION_REACTOR_CASING, GeneratorsBlocks.FISSION_REACTOR_PORT,
              GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(FissionReactorRecipeCategory.getFissionRecipes(), FISSION);
    }
}