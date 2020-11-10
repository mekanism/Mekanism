package mekanism.common.integration;

import mekanism.common.integration.lookingat.theoneprobe.TOPProvider;
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
    public static final String JEI_MOD_ID = "jei";
    public static final String TOP_MOD_ID = "theoneprobe";
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";
    public static final String PROJECTE_MOD_ID = "projecte";
    public static final String FLUX_NETWORKS_MOD_ID = "fluxnetworks";

    public boolean JEILoaded = false;
    public boolean CraftTweakerLoaded = false;
    public boolean IC2Loaded = false;
    public boolean FluxNetworksLoaded = false;
    public boolean ProjectELoaded = false;
    public boolean TOPLoaded = false;

    public void hookConstructor() {
        ModList modList = ModList.get();
        CraftTweakerLoaded = modList.isLoaded(CRAFTTWEAKER_MOD_ID);
    }

    public void hookCommonSetup() {
        ModList modList = ModList.get();
        IC2Loaded = modList.isLoaded(IC2_MOD_ID);
        JEILoaded = modList.isLoaded(JEI_MOD_ID);
        ProjectELoaded = modList.isLoaded(PROJECTE_MOD_ID);
        TOPLoaded = modList.isLoaded(TOP_MOD_ID);
        FluxNetworksLoaded = modList.isLoaded(FLUX_NETWORKS_MOD_ID);
    }

    public void sendIMCMessages(InterModEnqueueEvent event) {
        if (TOPLoaded) {
            InterModComms.sendTo(TOP_MOD_ID, "getTheOneProbe", TOPProvider::new);
        }
        if (ProjectELoaded) {
            //NSSHelper.init();//TODO - ProjectE
        }
    }
}