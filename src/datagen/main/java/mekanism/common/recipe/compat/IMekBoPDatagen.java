package mekanism.common.recipe.compat;

import mekanism.api.MekanismAPI;

@FunctionalInterface
public interface IMekBoPDatagen extends ICompatRecipeDatagen {

    IMekBoPDatagen INSTANCE = MekanismAPI.getService(IMekBoPDatagen.class);
}