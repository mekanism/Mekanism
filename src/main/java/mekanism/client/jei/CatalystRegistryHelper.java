package mekanism.client.jei;

import mekanism.api.block.FactoryType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.integration.crafttweaker.handlers.EnergizedSmelter;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.util.ResourceLocation;

public class CatalystRegistryHelper {

    public static void register(IRecipeCatalystRegistration registry, MekanismBlock mekanismBlock) {
        registerRecipeItem(registry, mekanismBlock, mekanismBlock.getRegistryName());
    }

    public static void registerCondensentrator(IRecipeCatalystRegistration registry) {
        ResourceLocation condensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_condensentrating");
        ResourceLocation decondensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_decondensentrating");
        registry.addRecipeCatalyst(MekanismBlock.ROTARY_CONDENSENTRATOR.getItemStack(), condensentrating, decondensentrating);
    }

    public static void registerSmelter(IRecipeCatalystRegistration registry) {
        register(registry, MekanismBlock.ENERGIZED_SMELTER);
        if (!Mekanism.hooks.CraftTweakerLoaded || !EnergizedSmelter.hasRemovedRecipe()) {
            //Vanilla catalyst
            registerRecipeItem(registry, MekanismBlock.ENERGIZED_SMELTER, VanillaRecipeCategoryUid.FURNACE);
        }
    }

    public static void registerRecipeItem(IRecipeCatalystRegistration registry, MekanismBlock mekanismBlock, ResourceLocation category) {
        registry.addRecipeCatalyst(mekanismBlock.getItemStack(), category);
        FactoryType factoryType = mekanismBlock.getFactoryType();
        if (factoryType != null) {
            for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
                registry.addRecipeCatalyst(MekanismBlock.getFactory(tier, factoryType).getItemStack(), category);
            }
        }
    }
}