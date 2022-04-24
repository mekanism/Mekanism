package mekanism.tools.common.registries;

import mekanism.common.recipe.serializer.WrappedShapedRecipeSerializer;
import mekanism.common.registration.impl.RecipeSerializerDeferredRegister;
import mekanism.common.registration.impl.RecipeSerializerRegistryObject;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.recipe.MekBannerShieldRecipe;
import mekanism.tools.common.recipe.PaxelRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

public class ToolsRecipeSerializers {

    private ToolsRecipeSerializers() {
    }

    public static final RecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new RecipeSerializerDeferredRegister(MekanismTools.MODID);

    public static final RecipeSerializerRegistryObject<MekBannerShieldRecipe> BANNER_SHIELD = RECIPE_SERIALIZERS.register("banner_shield", () -> new SimpleRecipeSerializer<>(MekBannerShieldRecipe::new));
    public static final RecipeSerializerRegistryObject<PaxelRecipe> PAXEL = RECIPE_SERIALIZERS.register("paxel", () -> new WrappedShapedRecipeSerializer<>(PaxelRecipe::new));
}