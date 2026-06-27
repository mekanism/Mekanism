package mekanism.common.integration;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.integration.gender.MekanismGenderArmor;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.common.registries.MekanismItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/// Hooks for Mekanism. Use to grab items or blocks out of different mods.
public final class MekanismHooks {

    //Note: These have to be static for use in CraftTweaker/Mod entrypoint annotations
    public static final String CURIOS_MOD_ID = "curios";
    public static final String FRAMED_BLOCKS_MOD_ID = "framedblocks";
    public static final String JEITWEAKER_MOD_ID = "jeitweaker";
    public static final String PROJECTE_MOD_ID = "projecte";
    public static final String TOP_MOD_ID = "theoneprobe";

    public record IntegrationInfo(String modid, boolean isLoaded) {

        private IntegrationInfo(String modid, Predicate<String> loadedCheck) {
            this(modid, loadedCheck.test(modid));
        }

        public void sendImc(String method, Supplier<?> toSend) {
            InterModComms.sendTo(modid, method, toSend);
        }

        public Identifier rl(String path) {
            return Identifier.fromNamespaceAndPath(modid, path);
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
    public final IntegrationInfo framedBlocks;
    public final IntegrationInfo genderMod;
    public final IntegrationInfo jei;
    public final IntegrationInfo jeiTweaker;
    public final IntegrationInfo projecte;
    public final IntegrationInfo recipeStages;
    public final IntegrationInfo theOneProbe;

    private final EntityCapability<ResourceHandler<ItemResource>, @Nullable Void> curiosItemHandler;

    public MekanismHooks() {
        ModList modList = ModList.get();
        //Note: The modlist is null when running tests
        Predicate<String> loadedCheck = modList == null ? _ -> false : modList::isLoaded;
        computerCraft = new IntegrationInfo("computercraft", loadedCheck);
        craftTweaker = new IntegrationInfo("crafttweaker", loadedCheck);
        curios = new IntegrationInfo(CURIOS_MOD_ID, loadedCheck);
        darkModeEverywhere = new IntegrationInfo("darkmodeeverywhere", loadedCheck);
        jei = new IntegrationInfo("jei", loadedCheck);
        emi = new IntegrationInfo("emi", loadedCheck);
        jeiTweaker = new IntegrationInfo(JEITWEAKER_MOD_ID, loadedCheck);
        projecte = new IntegrationInfo(PROJECTE_MOD_ID, loadedCheck);
        recipeStages = new IntegrationInfo("recipestages", loadedCheck);
        theOneProbe = new IntegrationInfo("TOP_MOD_ID", loadedCheck);
        genderMod = new IntegrationInfo("wildfire_gender", loadedCheck);
        framedBlocks = new IntegrationInfo(FRAMED_BLOCKS_MOD_ID, loadedCheck);
        curiosItemHandler = EntityCapability.createVoid(curios.rl("item_handler"), ResourceHandler.asClass());
    }

    @Nullable
    public ResourceHandler<ItemResource> getCuriosInventory(LivingEntity entity) {
        return entity.getCapability(curiosItemHandler);
    }

    public void hookCapabilityRegistration(RegisterCapabilitiesEvent event) {
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
    }

    public boolean computerCompatEnabled() {
        return computerCraft.isLoaded();
    }

    public boolean recipeViewerCompatEnabled() {
        return emi.isLoaded() || jei.isLoaded();
    }

    /// @apiNote DME only uses strings in IMC, so we can safely just include them here without worrying about classloading issues
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
