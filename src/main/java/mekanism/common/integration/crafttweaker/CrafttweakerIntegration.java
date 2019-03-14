package mekanism.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.mc1120.commands.CTChatCommand;
import java.util.LinkedList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.commands.GasesCommand;
import mekanism.common.integration.crafttweaker.commands.InfuseTypesCommand;
import mekanism.common.integration.crafttweaker.commands.MekRecipesCommand;

public class CrafttweakerIntegration {

    public static final List<IAction> LATE_REMOVALS = new LinkedList<>();
    public static final List<IAction> LATE_ADDITIONS = new LinkedList<>();

    /**
     * Apply after (machine)recipes have been applied, but before the FMLLoadCompleteEvent is fired. Preferably in
     * (post)init.
     * <p>
     * Applying to early causes remove to malfunction as no recipes have been registered. Applying to late causes JEI to
     * not pickup the changes.
     */
    public static void applyRecipeChanges() {
        //Remove before addition, so recipes can be overwritten
        applyChanges(LATE_REMOVALS);
        applyChanges(LATE_ADDITIONS);
    }

    private static void applyChanges(List<IAction> actions) {
        actions.forEach(action -> {
            try {
                CraftTweakerAPI.apply(action);
            } catch (Exception e) {
                Mekanism.logger.error("CT action failed", e);
                CraftTweakerAPI.logError(Mekanism.MOD_NAME + " CT action failed", e);
            }
        });
    }

    public static void registerCommands() {
        CTChatCommand.registerCommand(new GasesCommand());
        CTChatCommand.registerCommand(new InfuseTypesCommand());
        CTChatCommand.registerCommand(new MekRecipesCommand());
    }
}