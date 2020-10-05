package mekanism.client.jei;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.util.ResourceLocation;

public class CatalystRegistryHelper {

    private CatalystRegistryHelper() {
    }

    public static void register(IRecipeCatalystRegistration registry, IBlockProvider mekanismBlock, ResourceLocation... additionalCategories) {
        ResourceLocation[] categories = new ResourceLocation[additionalCategories.length + 1];
        categories[0] = mekanismBlock.getRegistryName();
        System.arraycopy(additionalCategories, 0, categories, 1, additionalCategories.length);
        registerRecipeItem(registry, mekanismBlock, categories);
    }

    public static void registerCondensentrator(IRecipeCatalystRegistration registry) {
        ResourceLocation condensentrating = Mekanism.rl("rotary_condensentrator_condensentrating");
        ResourceLocation decondensentrating = Mekanism.rl("rotary_condensentrator_decondensentrating");
        registry.addRecipeCatalyst(MekanismBlocks.ROTARY_CONDENSENTRATOR.getItemStack(), condensentrating, decondensentrating);
    }

    public static void registerRecipeItem(IRecipeCatalystRegistration registry, IBlockProvider mekanismBlock, ResourceLocation... categories) {
        registry.addRecipeCatalyst(mekanismBlock.getItemStack(), categories);
        Attribute.ifHas(mekanismBlock.getBlock(), AttributeFactoryType.class, attr -> {
            for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
                registry.addRecipeCatalyst(MekanismBlocks.getFactory(tier, attr.getFactoryType()).getItemStack(), categories);
            }
        });
    }

    public static void registerRecipeItem(IRecipeCatalystRegistration registry, IItemProvider mekanismItem, ResourceLocation... categories) {
        registry.addRecipeCatalyst(mekanismItem.getItemStack(), categories);
    }
}