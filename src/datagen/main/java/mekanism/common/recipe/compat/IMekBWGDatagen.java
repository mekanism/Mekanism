package mekanism.common.recipe.compat;

import mekanism.api.MekanismAPI;

@FunctionalInterface
public interface IMekBWGDatagen extends ICompatRecipeDatagen {

    IMekBWGDatagen INSTANCE = MekanismAPI.getService(IMekBWGDatagen.class);
}