package mekanism.common.integration.crafttweaker.content;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.ScriptLoadingOptions;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

/**
 * Helper class for registering chemicals via CraftTweaker. This is sort of akin to how ContentTweaker allows registering items/blocks via CraftTweaker
 */
public class CrTContentUtils {

    private static Map<ResourceLocation, Gas> queuedGases = new HashMap<>();
    private static Map<ResourceLocation, InfuseType> queuedInfuseTypes = new HashMap<>();
    private static Map<ResourceLocation, Pigment> queuedPigments = new HashMap<>();
    private static Map<ResourceLocation, Slurry> queuedSlurries = new HashMap<>();

    /**
     * Queues a {@link Gas} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link Gas}.
     * @param gas          {@link Gas} to queue for registration.
     */
    public static void queueGasForRegistration(ResourceLocation registryName, Gas gas) {
        queueChemicalForRegistration("Gas", queuedGases, registryName, gas);
    }

    /**
     * Queues an {@link InfuseType} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link InfuseType}.
     * @param infuseType   {@link InfuseType} to queue for registration.
     */
    public static void queueInfuseTypeForRegistration(ResourceLocation registryName, InfuseType infuseType) {
        queueChemicalForRegistration("Infuse Type", queuedInfuseTypes, registryName, infuseType);
    }

    /**
     * Queues a {@link Pigment} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link Pigment}.
     * @param pigment      {@link Pigment} to queue for registration.
     */
    public static void queuePigmentForRegistration(ResourceLocation registryName, Pigment pigment) {
        queueChemicalForRegistration("Pigment", queuedPigments, registryName, pigment);
    }

    /**
     * Queues a {@link Slurry} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link Slurry}.
     * @param slurry       {@link Slurry} to queue for registration.
     */
    public static void queueSlurryForRegistration(ResourceLocation registryName, Slurry slurry) {
        queueChemicalForRegistration("Slurry", queuedSlurries, registryName, slurry);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>> void queueChemicalForRegistration(String type, @Nullable Map<ResourceLocation, CHEMICAL> queuedChemicals,
          ResourceLocation registryName, CHEMICAL chemical) {
        //Only queue our chemicals for registration on the first run of our loader
        if (queuedChemicals == null) {
            CraftTweakerAPI.logError("Cannot register %s '%s' since it was called too late. Registering must be done during '#loader " +
                                     CrTConstants.CONTENT_LOADER + "'!", type, registryName);
        } else if (queuedChemicals.put(registryName, chemical) == null) {
            CraftTweakerAPI.logInfo("Queueing %s '%s' for registration.", type, registryName);
        } else {
            CraftTweakerAPI.logWarning("Registration for %s '%s' is already queued, skipping duplicate.", type, registryName);
        }
    }

    public static void registerCrTGases(RegistryEvent.Register<Gas> event) {
        //We register and load our content scripts here in the first registry event of ours for our types of content
        // to make sure that the new registry events have fired and that the registries exist and the bracket handler
        // validators won't choke
        CraftTweakerAPI.loadScripts(new ScriptLoadingOptions().setLoaderName(CrTConstants.CONTENT_LOADER).execute());
        registerQueuedChemicals(event, queuedGases, () -> queuedGases = null, "Gas", "gases");
    }

    public static void registerCrTInfuseTypes(RegistryEvent.Register<InfuseType> event) {
        registerQueuedChemicals(event, queuedInfuseTypes, () -> queuedInfuseTypes = null, "Infuse Type", "infuse types");
    }

    public static void registerCrTPigments(RegistryEvent.Register<Pigment> event) {
        registerQueuedChemicals(event, queuedPigments, () -> queuedPigments = null, "Pigment", "pigments");
    }

    public static void registerCrTSlurries(RegistryEvent.Register<Slurry> event) {
        registerQueuedChemicals(event, queuedSlurries, () -> queuedSlurries = null, "Slurry", "slurries");
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>> void registerQueuedChemicals(RegistryEvent.Register<CHEMICAL> event, Map<ResourceLocation, CHEMICAL> queued,
          Runnable setNull, String type, String plural) {
        if (queued != null) {//Validate it isn't null, it shouldn't be but just in case the event gets fired again or something
            //The reference got copied as needed to our parameter, so we can invalidate the other reference to it safely, so that
            // we properly don't allow more registration to happen once we start registering a specific chemical type
            setNull.run();
            int count = queued.size();
            CraftTweakerAPI.logInfo("Registering %d custom %s.", count, count == 1 ? type.toLowerCase(Locale.ROOT) : plural);
            for (Map.Entry<ResourceLocation, CHEMICAL> entry : queued.entrySet()) {
                ResourceLocation registryName = entry.getKey();
                event.getRegistry().register(entry.getValue().setRegistryName(registryName));
                CraftTweakerAPI.logInfo("Registered %s: '%s'.", type, registryName);
            }
        }
    }
}