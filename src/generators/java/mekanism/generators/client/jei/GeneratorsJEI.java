package mekanism.generators.client.jei;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.registries.MekanismGases;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
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
        MekanismJEI.registerItemSubtypes(registry, GeneratorsBlocks.BLOCKS.getAllBlocks());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FissionReactorRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        GasToGasRecipe recipe = new GasToGasRecipe(null, GasStackIngredient.from(MekanismGases.FISSILE_FUEL, 1), MekanismGases.NUCLEAR_WASTE.getGasStack(1)) {
            @Override
            public IRecipeSerializer<?> getSerializer() { return null; }
            @Override
            public IRecipeType<?> getType() { return null; }
        };
        registry.addRecipes(Collections.singletonList(recipe), GeneratorsBlocks.FISSION_REACTOR_CASING.getRegistryName());
    }
}