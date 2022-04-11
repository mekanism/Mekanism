package mekanism.common.integration;

import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.lookingat.theoneprobe.TOPProvider;
import mekanism.common.integration.projecte.NSSHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public static final String CC_MOD_ID = "computercraft";
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";
    public static final String CURIOS_MODID = "curios";
    public static final String FLUX_NETWORKS_MOD_ID = "fluxnetworks";
    public static final String IC2_MOD_ID = "ic2";
    public static final String JEI_MOD_ID = "jei";
    public static final String JEITWEAKER_MOD_ID = "jeitweaker";
    public static final String OC2_MOD_ID = "oc2";
    public static final String PROJECTE_MOD_ID = "projecte";
    public static final String TOP_MOD_ID = "theoneprobe";
    public static final String WILDFIRE_GENDER_MOD_ID = "wildfire_gender";

    public boolean CCLoaded;
    public boolean CraftTweakerLoaded;
    public boolean CuriosLoaded;
    public boolean FluxNetworksLoaded;
    public boolean IC2Loaded;
    public boolean JEILoaded;
    public boolean OC2Loaded;
    public boolean ProjectELoaded;
    public boolean TOPLoaded;
    public boolean WildfireGenderModLoaded;

    public void hookConstructor(final IEventBus bus) {
        ModList modList = ModList.get();
        CraftTweakerLoaded = modList.isLoaded(CRAFTTWEAKER_MOD_ID);
        CuriosLoaded = modList.isLoaded(CURIOS_MODID);
        if (CuriosLoaded) {
            CuriosIntegration.addListeners(bus);
        }
    }

    public void hookCommonSetup() {
        ModList modList = ModList.get();
        CCLoaded = modList.isLoaded(CC_MOD_ID);
        IC2Loaded = modList.isLoaded(IC2_MOD_ID);
        JEILoaded = modList.isLoaded(JEI_MOD_ID);
        OC2Loaded = modList.isLoaded(OC2_MOD_ID);
        ProjectELoaded = modList.isLoaded(PROJECTE_MOD_ID);
        TOPLoaded = modList.isLoaded(TOP_MOD_ID);
        FluxNetworksLoaded = modList.isLoaded(FLUX_NETWORKS_MOD_ID);
        WildfireGenderModLoaded = modList.isLoaded(WILDFIRE_GENDER_MOD_ID);
        if (CCLoaded) {
            CCCapabilityHelper.registerCCMathHelper();
        }
    }

    public void sendIMCMessages(InterModEnqueueEvent event) {
        if (TOPLoaded) {
            InterModComms.sendTo(TOP_MOD_ID, "getTheOneProbe", TOPProvider::new);
        }
        if (ProjectELoaded) {
            NSSHelper.init();
        }
        if (CuriosLoaded) {
            CuriosIntegration.sendIMC();
        }
    }

    public boolean computerCompatEnabled() {
        return CCLoaded || OC2Loaded;
    }

}