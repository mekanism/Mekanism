package mekanism.client.jei;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.base.IHasFactoryType;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.integration.crafttweaker.handlers.EnergizedSmelter;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.util.ResourceLocation;

public class CatalystRegistryHelper {

    public static void register(IRecipeCatalystRegistration registry, IBlockProvider mekanismBlock) {
        registerRecipeItem(registry, mekanismBlock, mekanismBlock.getRegistryName());
    }

    public static void registerCondensentrator(IRecipeCatalystRegistration registry) {
        ResourceLocation condensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_condensentrating");
        ResourceLocation decondensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_decondensentrating");
        registry.addRecipeCatalyst(MekanismBlocks.ROTARY_CONDENSENTRATOR.getItemStack(), condensentrating, decondensentrating);
    }

    public static void registerSmelter(IRecipeCatalystRegistration registry) {
        register(registry, MekanismBlocks.ENERGIZED_SMELTER);
        if (!Mekanism.hooks.CraftTweakerLoaded || !EnergizedSmelter.hasRemovedRecipe()) {
            //Vanilla catalyst
            registerRecipeItem(registry, MekanismBlocks.ENERGIZED_SMELTER, VanillaRecipeCategoryUid.FURNACE);
        }
    }

    public static void registerRecipeItem(IRecipeCatalystRegistration registry, IBlockProvider mekanismBlock, ResourceLocation category) {
        registry.addRecipeCatalyst(mekanismBlock.getItemStack(), category);
        if (mekanismBlock.getBlock() instanceof IHasFactoryType) {
            FactoryType factoryType = ((IHasFactoryType) mekanismBlock).getFactoryType();
            if (factoryType != null) {
                for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
                    registry.addRecipeCatalyst(MekanismBlocks.getFactory(tier, factoryType).getItemStack(), category);
                }
            }
        }
    }
}