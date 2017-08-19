package mekanism.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;

import java.util.LinkedList;
import java.util.List;

public class CrafttweakerIntegration
{
    public static final List<IAction> LATE_REMOVALS = new LinkedList<>();
    public static final List<IAction> LATE_ADDITIONS = new LinkedList<>();

    /**
     * Apply after (machine)recipes have been applied, but before the FMLLoadCompleteEvent is fired.
     * Preferbly in (post)init.
     *
     * Applying to early causes remove to mallfunction as no recipes have been registered.
     * Applying to late causes JEI to not pickup the changes.
     */
    public static void apply()
    {
        LATE_REMOVALS.forEach(CraftTweakerAPI::apply);
        LATE_ADDITIONS.forEach(CraftTweakerAPI::apply);
    }
}
