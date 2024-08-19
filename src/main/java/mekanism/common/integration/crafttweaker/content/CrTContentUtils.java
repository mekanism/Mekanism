package mekanism.common.integration.crafttweaker.content;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.zencode.scriptrun.ScriptRunConfiguration;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Helper class for registering chemicals via CraftTweaker. This is sort of akin to how ContentTweaker allows registering items/blocks via CraftTweaker
 */
public class CrTContentUtils {

    private static Map<ResourceLocation, Chemical> queuedChemicals = new HashMap<>();

    /**
     * Queues a {@link Chemical} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link Chemical}.
     * @param chemical          {@link Chemical} to queue for registration.
     */
    public static void queueChemicalForRegistration(ResourceLocation registryName, Chemical chemical) {
        //Only queue our chemicals for registration on the first run of our loader
        if (queuedChemicals != null) {
            if (queuedChemicals.put(registryName, chemical) == null) {
                CrTConstants.CRT_LOGGER.info("Queueing Chemical '{}' for registration.", registryName);
            } else {
                CrTConstants.CRT_LOGGER.warn("Registration for Chemical '{}' is already queued, skipping duplicate.", registryName);
            }
        }
    }

    public static void registerCrTContent(RegisterEvent event) {
        event.register(MekanismAPI.CHEMICAL_REGISTRY_NAME, helper -> {
            //We load our content scripts here in the first registry event of ours for our types of content
            // to make sure that the new registry events have fired and that the registries exist and the bracket handler
            // validators won't choke
            try {
                CraftTweakerAPI.getScriptRunManager().createScriptRun(new ScriptRunConfiguration(
                      CrTConstants.CONTENT_LOADER,
                      CrTConstants.CONTENT_LOADER_SOURCE_ID,
                      ScriptRunConfiguration.RunKind.EXECUTE
                )).execute();
            } catch (Throwable e) {
                CrTConstants.CRT_LOGGER.error("Unable to register chemicals due to an error.", e);
            }
            if (queuedChemicals != null) {//Validate it isn't null, it shouldn't be but just in case the event gets fired again or something
                int count = queuedChemicals.size();
                CrTConstants.CRT_LOGGER.info("Registering {} custom {}.", count, count == 1 ? "chemical" : "chemicals");
                for (Map.Entry<ResourceLocation, Chemical> entry : queuedChemicals.entrySet()) {
                    ResourceLocation registryName = entry.getKey();
                    helper.register(registryName, entry.getValue());
                    CrTConstants.CRT_LOGGER.info("Registered Chemical: '{}'.", registryName);
                }
                // invalidate the reference to it, so that
                // we properly don't allow more registration to happen once we start registering a specific chemical type
                queuedChemicals = null;
            }
        });
    }

}