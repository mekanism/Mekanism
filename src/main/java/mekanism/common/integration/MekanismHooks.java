package mekanism.common.integration;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.framedblocks.FramedBlocksIntegration;
import mekanism.common.integration.gender.MekanismGenderArmor;
import mekanism.common.integration.jsonthings.JsonThingsIntegration;
import mekanism.common.integration.lookingat.theoneprobe.TOPProvider;
import mekanism.common.integration.projecte.MekanismNormalizedSimpleStacks;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.common.registries.MekanismItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public record IntegrationInfo(String modid, boolean isLoaded) {

        private IntegrationInfo(String modid, Predicate<String> loadedCheck) {
            this(modid, loadedCheck.test(modid));
        }

        private void sendImc(String method, Supplier<?> toSend) {
            InterModComms.sendTo(modid, method, toSend);
        }

        public ResourceLocation rl(String path) {
            return ResourceLocation.fromNamespaceAndPath(modid, path);
        }

        public void assertLoaded() {
            if (!isLoaded) {
                throw new IllegalStateException(modid + " is not loaded");
            }
        }
    }

    public final IntegrationInfo computerCraft;
    public final IntegrationInfo craftTweaker;
    public final IntegrationInfo curios;
    public final IntegrationInfo darkModeEverywhere;
    public final IntegrationInfo emi;
    public final IntegrationInfo fluxNetworks;
    public final IntegrationInfo framedBlocks;
    public final IntegrationInfo genderMod;
    public final IntegrationInfo grandPower;
    public final IntegrationInfo jei;
    public final IntegrationInfo jeiTweaker;
    public final IntegrationInfo jsonThings;
    public final IntegrationInfo oc2;
    public final IntegrationInfo projecte;
    public final IntegrationInfo recipeStages;
    public final IntegrationInfo theOneProbe;

    //Note: These have to be static for use in CraftTweaker annotations
    public static final String JEITWEAKER_MOD_ID = "jeitweaker";
    public static final String PROJECTE_MOD_ID = "projecte";

    public MekanismHooks() {
        ModList modList = ModList.get();
        //Note: The modlist is null when running tests
        Predicate<String> loadedCheck = modList == null ? modid -> false : modList::isLoaded;
        computerCraft = new IntegrationInfo("computercraft", loadedCheck);
        craftTweaker = new IntegrationInfo("crafttweaker", loadedCheck);
        curios = new IntegrationInfo("curios", loadedCheck);
        darkModeEverywhere = new IntegrationInfo("darkmodeeverywhere", loadedCheck);
        fluxNetworks = new IntegrationInfo("fluxnetworks", loadedCheck);
        grandPower = new IntegrationInfo("grandpower", loadedCheck);
        jei = new IntegrationInfo("jei", loadedCheck);
        emi = new IntegrationInfo("emi", loadedCheck);
        jeiTweaker = new IntegrationInfo("jeitweaker", loadedCheck);
        jsonThings = new IntegrationInfo("jsonthings", loadedCheck);
        oc2 = new IntegrationInfo("oc2", loadedCheck);
        projecte = new IntegrationInfo("projecte", loadedCheck);
        recipeStages = new IntegrationInfo("recipestages", loadedCheck);
        theOneProbe = new IntegrationInfo("theoneprobe", loadedCheck);
        genderMod = new IntegrationInfo("wildfire_gender", loadedCheck);
        framedBlocks = new IntegrationInfo("framedblocks", loadedCheck);
    }

    public void hookConstructor(final IEventBus modEventBus) {
        if (curios.isLoaded()) {
            CuriosIntegration.addListeners(modEventBus);
        }
        if (craftTweaker.isLoaded() && !DatagenModLoader.isRunningDataGen()) {
            //Register our CrT listener at lowest priority to try and ensure they get later ids than our normal registries
            modEventBus.addListener(EventPriority.LOWEST, CrTContentUtils::registerCrTContent);
        }
        if (jsonThings.isLoaded()) {
            JsonThingsIntegration.hook(modEventBus);
        }
        if (projecte.isLoaded()) {
            MekanismNormalizedSimpleStacks.NSS_SERIALIZERS.register(modEventBus);
        }
        if (framedBlocks.isLoaded()) {
            FramedBlocksIntegration.init(modEventBus);
        }
    }

    public void hookCapabilityRegistration(RegisterCapabilitiesEvent event) {
        EnergyCompatUtils.initLoadedCache();
        if (genderMod.isLoaded()) {
            MekanismGenderArmor.HAZMAT.register(event, MekanismItems.HAZMAT_GOWN);
            MekanismGenderArmor.OPEN_FRONT.register(event, MekanismItems.JETPACK, MekanismItems.SCUBA_TANK);
            MekanismGenderArmor.HIDES_BREASTS.register(event, MekanismItems.ARMORED_JETPACK, MekanismItems.MEKASUIT_BODYARMOR);
        }
    }

    public void hookCommonSetup() {
        if (computerCompatEnabled()) {
            FactoryRegistry.load();
            if (computerCraft.isLoaded()) {
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
        if (darkModeEverywhere.isLoaded()) {
            //Note: While it is only strings, so it is safe to call and IMC validates the mods are loaded
            // we add this check here, so we can skip iterating the list of things we want to blacklist when it is not present
            sendDarkModeEverywhereIMC();
        }
        if (theOneProbe.isLoaded()) {
            theOneProbe.sendImc("getTheOneProbe", TOPProvider::new);
        }
    }

    public boolean computerCompatEnabled() {
        return computerCraft.isLoaded() || oc2.isLoaded();
    }

    public boolean recipeViewerCompatEnabled() {
        return emi.isLoaded() || jei.isLoaded();
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
            darkModeEverywhere.sendImc("dme-shaderblacklist", method);
        }
    }
}
