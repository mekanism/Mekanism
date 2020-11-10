package mekanism.common.integration.crafttweaker.content;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

/**
 * Helper class for registering chemicals via CraftTweaker. This is sort of akin to how ContentTweaker allows registering items/blocks via CraftTweaker
 */
public class CrTContentUtils {

    //TODO: Do we want to add some sort of auto generation resource wise like CoT has, for if a resource is declared instead of
    // the default being used?
    private static Map<ResourceLocation, GasBuilder> queuedGases = new HashMap<>();
    private static Map<ResourceLocation, InfuseTypeBuilder> queuedInfuseTypes = new HashMap<>();
    private static Map<ResourceLocation, PigmentBuilder> queuedPigments = new HashMap<>();
    private static Map<ResourceLocation, SlurryBuilder> queuedSlurries = new HashMap<>();

    public static void queueGasForRegistration(ResourceLocation registryName, GasBuilder builder) {
        queueChemicalForRegistration("Gas", queuedGases, registryName, builder);
    }

    public static void queueInfuseTypeForRegistration(ResourceLocation registryName, InfuseTypeBuilder builder) {
        queueChemicalForRegistration("Infuse Type", queuedInfuseTypes, registryName, builder);
    }

    public static void queuePigmentForRegistration(ResourceLocation registryName, PigmentBuilder builder) {
        queueChemicalForRegistration("Pigment", queuedPigments, registryName, builder);
    }

    public static void queueSlurryForRegistration(ResourceLocation registryName, SlurryBuilder builder) {
        queueChemicalForRegistration("Slurry", queuedSlurries, registryName, builder);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>> void queueChemicalForRegistration(String type,
          @Nullable Map<ResourceLocation, BUILDER> queuedChemicals, ResourceLocation registryName, BUILDER builder) {
        //TODO: Figure out if this really should be in some sort of action instead of just being ran directly
        if (queuedChemicals == null) {
            //TODO: Figure out loader what we want to call the loader and actually register it
            CraftTweakerAPI.logError("Cannot register %s '%s' since it was called too late. Registering must be done during '#loader mekanism'!",
                  type, registryName);
        } else if (queuedChemicals.containsKey(registryName)) {
            CraftTweakerAPI.logWarning("Registration for %s '%s' is already queued, skipping duplicate.", type, registryName);
        } else {
            CraftTweakerAPI.logInfo("Queueing %s '%s' for registration.", type, registryName);
            queuedChemicals.put(registryName, builder);
        }
    }

    //TODO: Register these listeners iff CrT is loaded
    public static void registerCrTGases(RegistryEvent.Register<Gas> event) {
        if (queuedGases != null) {//Validate it isn't null, it shouldn't be but just in case the event gets fired again or something
            //Copy the reference and then invalidate the other reference so that we properly don't allow more registration to
            // happen once we start registering it
            Map<ResourceLocation, GasBuilder> queued = queuedGases;
            queuedGases = null;
            queued.forEach((registryName, builder) -> event.getRegistry().register(new Gas(builder).setRegistryName(registryName)));
        }
    }

    public static void registerCrTInfuseTypes(RegistryEvent.Register<InfuseType> event) {
        if (queuedInfuseTypes != null) {//Validate it isn't null, it shouldn't be but just in case the event gets fired again or something
            //Copy the reference and then invalidate the other reference so that we properly don't allow more registration to
            // happen once we start registering it
            Map<ResourceLocation, InfuseTypeBuilder> queued = queuedInfuseTypes;
            queuedInfuseTypes = null;
            queued.forEach((registryName, builder) -> event.getRegistry().register(new InfuseType(builder).setRegistryName(registryName)));
        }
    }

    public static void registerCrTPigments(RegistryEvent.Register<Pigment> event) {
        if (queuedPigments != null) {//Validate it isn't null, it shouldn't be but just in case the event gets fired again or something
            //Copy the reference and then invalidate the other reference so that we properly don't allow more registration to
            // happen once we start registering it
            Map<ResourceLocation, PigmentBuilder> queued = queuedPigments;
            queuedPigments = null;
            queued.forEach((registryName, builder) -> event.getRegistry().register(new Pigment(builder).setRegistryName(registryName)));
        }
    }

    public static void registerCrTSlurry(RegistryEvent.Register<Slurry> event) {
        if (queuedSlurries != null) {//Validate it isn't null, it shouldn't be but just in case the event gets fired again or something
            //Copy the reference and then invalidate the other reference so that we properly don't allow more registration to
            // happen once we start registering it
            Map<ResourceLocation, SlurryBuilder> queued = queuedSlurries;
            queuedSlurries = null;
            queued.forEach((registryName, builder) -> event.getRegistry().register(new Slurry(builder).setRegistryName(registryName)));
        }
    }
}