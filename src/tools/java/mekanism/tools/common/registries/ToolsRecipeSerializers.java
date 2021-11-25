package mekanism.tools.common.registries;

import mekanism.common.recipe.serializer.WrappedShapedRecipeSerializer;
import mekanism.common.registration.impl.IRecipeSerializerDeferredRegister;
import mekanism.common.registration.impl.IRecipeSerializerRegistryObject;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.recipe.MekBannerShieldRecipe;
import mekanism.tools.common.recipe.PaxelRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;

public class ToolsRecipeSerializers {

    private ToolsRecipeSerializers() {
    }

    public static final IRecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new IRecipeSerializerDeferredRegister(MekanismTools.MODID);

    public static final IRecipeSerializerRegistryObject<MekBannerShieldRecipe> BANNER_SHIELD = RECIPE_SERIALIZERS.register("banner_shield", () -> new SpecialRecipeSerializer<>(MekBannerShieldRecipe::new));
    public static final IRecipeSerializerRegistryObject<PaxelRecipe> PAXEL = RECIPE_SERIALIZERS.register("paxel", () -> new WrappedShapedRecipeSerializer<>(PaxelRecipe::new));
}