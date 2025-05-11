package mekanism.tools.common.registries;

import mekanism.common.recipe.serializer.MekanismRecipeSerializer;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.recipe.MekBannerShieldRecipe;
import mekanism.tools.common.recipe.PaxelRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ToolsRecipeSerializers {

    private ToolsRecipeSerializers() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MekanismTools.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MekBannerShieldRecipe>> BANNER_SHIELD = RECIPE_SERIALIZERS.register("banner_shield", () -> new SimpleCraftingRecipeSerializer<>(MekBannerShieldRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PaxelRecipe>> PAXEL = RECIPE_SERIALIZERS.register("paxel", () -> MekanismRecipeSerializer.wrapped(PaxelRecipe::new));
}