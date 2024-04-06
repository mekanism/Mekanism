package mekanism.common.integration;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.jsonthings.JsonThingsIntegration;
import mekanism.common.integration.lookingat.theoneprobe.TOPProvider;
import mekanism.common.integration.projecte.MekanismNormalizedSimpleStacks;
import mekanism.common.recipe.bin.BinInsertRecipe;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public static final String CC_MOD_ID = "computercraft";
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";
    public static final String CURIOS_MODID = "curios";
    public static final String DARK_MODE_EVERYWHERE_MODID = "darkmodeeverywhere";
    public static final String FLUX_NETWORKS_MOD_ID = "fluxnetworks";
    public static final String JEI_MOD_ID = "jei";
    public static final String EMI_MOD_ID = "emi";
    public static final String JEITWEAKER_MOD_ID = "jeitweaker";
    public static final String JSON_THINGS_MOD_ID = "jsonthings";
    public static final String OC2_MOD_ID = "oc2";
    public static final String PROJECTE_MOD_ID = "projecte";
    public static final String RECIPE_STAGES_MOD_ID = "recipestages";
    public static final String TOP_MOD_ID = "theoneprobe";
    public static final String WILDFIRE_GENDER_MOD_ID = "wildfire_gender";

    public final boolean CCLoaded;
    public final boolean CraftTweakerLoaded;
    public final boolean CuriosLoaded;
    public final boolean DMELoaded;
    public final boolean EmiLoaded;
    public final boolean FluxNetworksLoaded;
    public final boolean JEILoaded;
    public final boolean JsonThingsLoaded;
    public final boolean OC2Loaded;
    public final boolean ProjectELoaded;
    public final boolean RecipeStagesLoaded;
    public final boolean TOPLoaded;
    public final boolean WildfireGenderModLoaded;

    public MekanismHooks() {
        ModList modList = ModList.get();
        //Note: The modlist is null when running tests
        Predicate<String> loadedCheck = modList == null ? modid -> false : modList::isLoaded;
        CCLoaded = loadedCheck.test(CC_MOD_ID);
        CraftTweakerLoaded = loadedCheck.test(CRAFTTWEAKER_MOD_ID);
        CuriosLoaded = loadedCheck.test(CURIOS_MODID);
        DMELoaded = loadedCheck.test(DARK_MODE_EVERYWHERE_MODID);
        EmiLoaded = loadedCheck.test(EMI_MOD_ID);
        FluxNetworksLoaded = loadedCheck.test(FLUX_NETWORKS_MOD_ID);
        JEILoaded = loadedCheck.test(JEI_MOD_ID);
        JsonThingsLoaded = loadedCheck.test(JSON_THINGS_MOD_ID);
        OC2Loaded = loadedCheck.test(OC2_MOD_ID);
        ProjectELoaded = loadedCheck.test(PROJECTE_MOD_ID);
        RecipeStagesLoaded = loadedCheck.test(RECIPE_STAGES_MOD_ID);
        TOPLoaded = loadedCheck.test(TOP_MOD_ID);
        WildfireGenderModLoaded = loadedCheck.test(WILDFIRE_GENDER_MOD_ID);
    }

    public void hookConstructor(final IEventBus bus) {
        if (CuriosLoaded) {
            CuriosIntegration.addListeners(bus);
        }
        if (CraftTweakerLoaded && !DatagenModLoader.isRunningDataGen()) {
            //Attempt to grab the mod event bus for CraftTweaker so that we can register our custom content in their namespace
            // to make it clearer which chemicals were added by CraftTweaker, and which are added by actual mods.
            // Gracefully fallback to our event bus if something goes wrong with getting CrT's and just then have the log have
            // warnings about us registering things in their namespace.
            IEventBus crtModEventBus = ModList.get().getModContainerById(MekanismHooks.CRAFTTWEAKER_MOD_ID)
                  .map(ModContainer::getEventBus)
                  .orElse(bus);
            //Register our CrT listener at lowest priority to try and ensure they get later ids than our normal registries
            crtModEventBus.addListener(EventPriority.LOWEST, CrTContentUtils::registerCrTContent);
        }
        if (JsonThingsLoaded) {
            JsonThingsIntegration.hook(bus);
        }
        if (ProjectELoaded) {
            MekanismNormalizedSimpleStacks.NSS_SERIALIZERS.register(bus);
        }
    }

    public void hookCapabilityRegistration() {
        EnergyCompatUtils.initLoadedCache();
    }

    public void hookCommonSetup() {
        if (computerCompatEnabled()) {
            FactoryRegistry.load();
            if (CCLoaded) {
                CCCapabilityHelper.registerApis();
            }
        }

        //TODO - 1.20: Move this out of here and back to always being registered whenever it gets fixed in Neo.
        // Modifying the result doesn't apply properly when "quick crafting"
        if (ModList.get().isLoaded("fastbench")) {
            NeoForge.EVENT_BUS.addListener(BinInsertRecipe::onCrafting);
        }
    }

    public void sendIMCMessages(InterModEnqueueEvent event) {
        if (DMELoaded) {
            //Note: While it is only strings, so it is safe to call and IMC validates the mods are loaded
            // we add this check here, so we can skip iterating the list of things we want to blacklist when it is not present
            sendDarkModeEverywhereIMC();
        }
        if (TOPLoaded) {
            InterModComms.sendTo(TOP_MOD_ID, "getTheOneProbe", TOPProvider::new);
        }
    }

    public boolean computerCompatEnabled() {
        return CCLoaded || OC2Loaded;
    }

    public boolean recipeViewerCompatEnabled() {
        return EmiLoaded || JEILoaded;
    }

    /**
     * @apiNote DME only uses strings in IMC, so we can safely just include them here without worrying about classloading issues
     */
    private void sendDarkModeEverywhereIMC() {
        List<Supplier<String>> methodBlacklist = List.of(
              //Used for drawing fluids and chemicals in various GUIs including JEI as well as similar styled things
              () -> "mekanism.client.gui.GuiUtils:drawTiledSprite",
              //MekaSuit HUD rendering (already configurable by the user)
              () -> "mekanism.client.render.HUDRenderer:renderCompass",
              () -> "mekanism.client.render.HUDRenderer:renderHUDElement"
        );
        for (Supplier<String> method : methodBlacklist) {
            InterModComms.sendTo(DARK_MODE_EVERYWHERE_MODID, "dme-shaderblacklist", method);
        }
    }
}
