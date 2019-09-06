package mekanism.common.base;

import java.util.List;
import mekanism.api.recipes.IMekanismRecipe;

/**
 * Internal interface containing methods that are shared by many core Mekanism machines.  TODO: remove next minor MC version.
 *
 * @author AidanBrady
 */
public interface IElectricMachine<RECIPE extends IMekanismRecipe> {

    /**
     * Update call for machines. Use instead of updateEntity() - it's called every tick.
     */
    void onUpdate();

    /**
     * Whether or not this machine can operate.
     *
     * @return can operate
     */
    boolean canOperate(RECIPE recipe);

    /**
     * Runs this machine's operation -- or smelts the item.
     */
    void operate(RECIPE recipe);

    /**
     * Gets this machine's recipes.
     */
    List<RECIPE> getRecipes();

    RECIPE getRecipe();
}