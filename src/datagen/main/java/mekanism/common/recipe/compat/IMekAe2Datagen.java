package mekanism.common.recipe.compat;

import mekanism.api.MekanismAPI;

@FunctionalInterface
public interface IMekAe2Datagen extends ICompatRecipeDatagen {

    IMekAe2Datagen INSTANCE = MekanismAPI.getService(IMekAe2Datagen.class);
}