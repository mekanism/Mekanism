package mekanism.common.integration;

import mekanism.common.integration.projecte.NSSHelper;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public static final String IC2_MOD_ID = "ic2";
    public static final String TOP_MOD_ID = "theoneprobe";
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";
    public static final String PROJECTE_MOD_ID = "projecte";

    public boolean CraftTweakerLoaded = false;
    public boolean IC2Loaded = false;
    public boolean ProjectELoaded = false;
    public boolean TOPLoaded = false;

    public void hookPreInit() {
        ModList modList = ModList.get();
        CraftTweakerLoaded = modList.isLoaded(CRAFTTWEAKER_MOD_ID);
        IC2Loaded = modList.isLoaded(IC2_MOD_ID);
        ProjectELoaded = modList.isLoaded(PROJECTE_MOD_ID);
        TOPLoaded = modList.isLoaded(TOP_MOD_ID);
    }

    public void sendIMCMessages(InterModEnqueueEvent event) {
        if (TOPLoaded) {
            InterModComms.sendTo(TOP_MOD_ID, "getTheOneProbe", TOPProvider::new);
        }
        if (ProjectELoaded) {
            NSSHelper.init();
        }
    }

    public void hookCommonSetup() {
        if (CraftTweakerLoaded) {
            //CraftTweaker must be ran after all other recipe changes
            //CrafttweakerIntegration.registerCommands();
            //CrafttweakerIntegration.applyRecipeChanges();
        }
    }
}