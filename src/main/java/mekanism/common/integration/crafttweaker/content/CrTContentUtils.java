package mekanism.common.integration.crafttweaker.content;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.zencode.scriptrun.ScriptRunConfiguration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Helper class for registering chemicals via CraftTweaker. This is sort of akin to how ContentTweaker allows registering items/blocks via CraftTweaker
 */
public class CrTContentUtils {

    private static Map<ResourceLocation, Gas> queuedGases = new HashMap<>();
    private static Map<ResourceLocation, InfuseType> queuedInfuseTypes = new HashMap<>();
    private static Map<ResourceLocation, Pigment> queuedPigments = new HashMap<>();
    private static Map<ResourceLocation, Slurry> queuedSlurries = new HashMap<>();
    private static Map<ResourceLocation, RobitSkin> queuedRobitSkins = new HashMap<>();

    /**
     * Queues a {@link Gas} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link Gas}.
     * @param gas          {@link Gas} to queue for registration.
     */
    public static void queueGasForRegistration(ResourceLocation registryName, Gas gas) {
        queueForRegistration("Gas", queuedGases, registryName, gas);
    }

    /**
     * Queues an {@link InfuseType} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link InfuseType}.
     * @param infuseType   {@link InfuseType} to queue for registration.
     */
    public static void queueInfuseTypeForRegistration(ResourceLocation registryName, InfuseType infuseType) {
        queueForRegistration("Infuse Type", queuedInfuseTypes, registryName, infuseType);
    }

    /**
     * Queues a {@link Pigment} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link Pigment}.
     * @param pigment      {@link Pigment} to queue for registration.
     */
    public static void queuePigmentForRegistration(ResourceLocation registryName, Pigment pigment) {
        queueForRegistration("Pigment", queuedPigments, registryName, pigment);
    }

    /**
     * Queues a {@link Slurry} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link Slurry}.
     * @param slurry       {@link Slurry} to queue for registration.
     */
    public static void queueSlurryForRegistration(ResourceLocation registryName, Slurry slurry) {
        queueForRegistration("Slurry", queuedSlurries, registryName, slurry);
    }

    /**
     * Queues a {@link RobitSkin} to be registered with the given registry name.
     *
     * @param registryName Registry name to give the {@link RobitSkin}.
     * @param skin         {@link RobitSkin} to queue for registration.
     */
    public static void queueRobitSkinForRegistration(ResourceLocation registryName, RobitSkin skin) {
        queueForRegistration("Robit Skin", queuedRobitSkins, registryName, skin);
    }

    private static <V extends IForgeRegistryEntry<V>> void queueForRegistration(String type, @Nullable Map<ResourceLocation, V> queued, ResourceLocation registryName,
          V element) {
        //Only queue our chemicals for registration on the first run of our loader
        if (queued != null) {
            if (queued.put(registryName, element) == null) {
                CraftTweakerAPI.LOGGER.info("Queueing {} '{}' for registration.", type, registryName);
            } else {
                CraftTweakerAPI.LOGGER.warn("Registration for {} '{}' is already queued, skipping duplicate.", type, registryName);
            }
        }
    }

    public static void registerCrTGases(RegistryEvent.Register<Gas> event) {
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
            CraftTweakerAPI.LOGGER.error("Unable to register chemicals due to an error.", e);
        }
        registerQueued(event, queuedGases, () -> queuedGases = null, "Gas", "gases");
    }

    public static void registerCrTInfuseTypes(RegistryEvent.Register<InfuseType> event) {
        registerQueued(event, queuedInfuseTypes, () -> queuedInfuseTypes = null, "Infuse Type", "infuse types");
    }

    public static void registerCrTPigments(RegistryEvent.Register<Pigment> event) {
        registerQueued(event, queuedPigments, () -> queuedPigments = null, "Pigment", "pigments");
    }

    public static void registerCrTSlurries(RegistryEvent.Register<Slurry> event) {
        registerQueued(event, queuedSlurries, () -> queuedSlurries = null, "Slurry", "slurries");
    }

    public static void registerCrTRobitSkins(RegistryEvent.Register<RobitSkin> event) {
        registerQueued(event, queuedRobitSkins, () -> queuedRobitSkins = null, "Robit Skin", "robit skins");
    }

    private static <V extends IForgeRegistryEntry<V>> void registerQueued(RegistryEvent.Register<V> event, Map<ResourceLocation, V> queued, Runnable setNull, String type,
          String plural) {
        if (queued != null) {//Validate it isn't null, it shouldn't be but just in case the event gets fired again or something
            //The reference got copied as needed to our parameter, so we can invalidate the other reference to it safely, so that
            // we properly don't allow more registration to happen once we start registering a specific chemical type
            setNull.run();
            int count = queued.size();
            CraftTweakerAPI.LOGGER.info("Registering {} custom {}.", count, count == 1 ? type.toLowerCase(Locale.ROOT) : plural);
            for (Map.Entry<ResourceLocation, V> entry : queued.entrySet()) {
                ResourceLocation registryName = entry.getKey();
                event.getRegistry().register(entry.getValue().setRegistryName(registryName));
                CraftTweakerAPI.LOGGER.info("Registered {}: '{}'.", type, registryName);
            }
        }
    }
}