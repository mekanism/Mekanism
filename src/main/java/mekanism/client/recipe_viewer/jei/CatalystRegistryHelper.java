package mekanism.client.recipe_viewer.jei;

import java.util.List;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;

public class CatalystRegistryHelper {

    private CatalystRegistryHelper() {
    }

    public static void register(IRecipeCatalystRegistration registry, IRecipeViewerRecipeType<?>... categories) {
        for (IRecipeViewerRecipeType<?> category : categories) {
            register(registry, MekanismJEI.genericRecipeType(category), category.workstations());
        }
    }

    public static void register(IRecipeCatalystRegistration registry, RecipeType<?> recipeType, List<IItemProvider> workstations) {
        for (IItemProvider workstation : workstations) {
            registry.addRecipeCatalyst(workstation.getItemStack(), recipeType);
            if (workstation instanceof IBlockProvider mekanismBlock) {
                AttributeFactoryType factoryType = Attribute.get(mekanismBlock.getBlock(), AttributeFactoryType.class);
                if (factoryType != null) {
                    for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
                        registry.addRecipeCatalyst(MekanismBlocks.getFactory(tier, factoryType.getFactoryType()).getItemStack(), recipeType);
                    }
                }
            }
        }
    }
}