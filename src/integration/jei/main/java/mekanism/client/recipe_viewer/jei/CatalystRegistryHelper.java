package mekanism.client.recipe_viewer.jei;

import java.util.List;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class CatalystRegistryHelper {

    private CatalystRegistryHelper() {
    }

    public static void register(IRecipeCatalystRegistration registry, IRecipeViewerRecipeType<?>... categories) {
        for (IRecipeViewerRecipeType<?> category : categories) {
            register(registry, MekanismJEI.genericRecipeType(category), category.workstations());
        }
    }

    public static void register(IRecipeCatalystRegistration registry, IRecipeType<?> recipeType, List<ItemLike> workstations) {
        for (ItemLike workstation : workstations) {
            Item item = workstation.asItem();
            registry.addCraftingStation(recipeType, item);
            FactoryType factoryType = item.components().get(MekanismDataComponents.FACTORY_TYPE);
            if (factoryType != null) {
                for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
                    registry.addCraftingStation(recipeType, MekanismBlocks.getFactory(tier, factoryType));
                }
            }
        }
    }
}