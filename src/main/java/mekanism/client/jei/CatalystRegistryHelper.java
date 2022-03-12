package mekanism.client.jei;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
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

    public static void register(IRecipeCatalystRegistration registry, IBlockProvider mekanismBlock, MekanismJEIRecipeType<?>... additionalCategories) {
        MekanismJEIRecipeType<?>[] categories = new MekanismJEIRecipeType<?>[additionalCategories.length + 1];
        categories[0] = MekanismJEIRecipeType.findType(mekanismBlock.getRegistryName());
        System.arraycopy(additionalCategories, 0, categories, 1, additionalCategories.length);
        registerRecipeItem(registry, mekanismBlock, categories);
    }

    public static void registerRecipeItem(IRecipeCatalystRegistration registry, IItemProvider mekanismItem, MekanismJEIRecipeType<?>... categories) {
        registerRecipeItem(registry, mekanismItem, MekanismJEI.recipeType(categories));
    }

    public static void registerRecipeItem(IRecipeCatalystRegistration registry, IItemProvider mekanismItem, MekanismJEIRecipeType<?> category,
          RecipeType<?>... additionalCategories) {
        RecipeType<?>[] categories = new RecipeType<?>[additionalCategories.length + 1];
        categories[0] = MekanismJEI.recipeType(category);
        System.arraycopy(additionalCategories, 0, categories, 1, additionalCategories.length);
        registerRecipeItem(registry, mekanismItem, categories);
    }

    public static void registerRecipeItem(IRecipeCatalystRegistration registry, IItemProvider mekanismItem, RecipeType<?>... categories) {
        registry.addRecipeCatalyst(mekanismItem.getItemStack(), categories);
        if (mekanismItem instanceof IBlockProvider mekanismBlock) {
            Attribute.ifHas(mekanismBlock.getBlock(), AttributeFactoryType.class, attr -> {
                for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
                    registry.addRecipeCatalyst(MekanismBlocks.getFactory(tier, attr.getFactoryType()).getItemStack(), categories);
                }
            });
        }
    }

    public static void register(IRecipeCatalystRegistration registry, MekanismJEIRecipeType<?> category, IItemProvider... catalysts) {
        RecipeType<?> recipeType = MekanismJEI.recipeType(category);
        for (IItemProvider catalyst : catalysts) {
            registry.addRecipeCatalyst(catalyst.getItemStack(), recipeType);
        }
    }
}