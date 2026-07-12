package mekanism.common.recipe.compat;

import mekanism.api.MekanismAPI;

@FunctionalInterface
public interface IMekFarmersDatagen extends ICompatRecipeDatagen {

    IMekFarmersDatagen INSTANCE = MekanismAPI.getService(IMekFarmersDatagen.class);
}