package mekanism.tools.common.registries;

import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.recipe.MekBannerShieldRecipe;
import mekanism.tools.common.recipe.PaxelRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class ToolsRecipeSerializers {

    private ToolsRecipeSerializers() {
    }

    public static final MekanismDeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = new MekanismDeferredRegister<>(Registries.RECIPE_SERIALIZER, MekanismTools.MODID);

    public static final MekanismDeferredHolder<RecipeSerializer<?>, RecipeSerializer<MekBannerShieldRecipe>> BANNER_SHIELD = RECIPE_SERIALIZERS.register("banner_shield", () -> new SimpleCraftingRecipeSerializer<>(MekBannerShieldRecipe::new));
    public static final MekanismDeferredHolder<RecipeSerializer<?>, RecipeSerializer<PaxelRecipe>> PAXEL = RECIPE_SERIALIZERS.register("paxel", () -> MekanismRecipeSerializer.wrapped(PaxelRecipe::new));
}