/**
 * Team CoFH
 * 
 * Thermal Expansion
 */

package thermalexpansion.api.crafting;

/**
 * Allows access to all of the Thermal Expansion crafting managers. Add your recipes during @PostInit
 * or risk a null pointer. :)
 */

public class CraftingManagers {

    /**
     * Allows you to add recipes to the Magma Crucible. See {@link ICrucibleManager} for details.
     */
    public static ICrucibleManager crucibleManager;

    /**
     * Allows you to add recipes to the Liquid Transposer. See {@link ITransposerManager} for
     * details.
     */
    public static ITransposerManager transposerManager;

    /**
     * Allows you to add recipes to the Powered Furnace. See {@link IFurnaceManager} for details.
     */
    public static IFurnaceManager furnaceManager;

    /**
     * Allows you to add recipes to the Pulverizer. See {@link IPulverizerManager} for details.
     */
    public static IPulverizerManager pulverizerManager;

    /**
     * Allows you to add recipes to the Sawmill. See {@link ISawmillManager} for details.
     */
    public static ISawmillManager sawmillManager;

    /**
     * Allows you to add recipes to the Induction Smelter. See {@link ISmelterManager} for details.
     */
    public static ISmelterManager smelterManager;
}
