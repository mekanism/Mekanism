package mekanism.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalResource.EmptyChemicalResource;
import mekanism.api.chemical.ChemicalSerializationHelper;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.registration.DatapackDeferredRegister;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;

public class MekanismChemicals {

    private MekanismChemicals() {
    }

    private static final DatapackDeferredRegister<Chemical> CHEMICALS = DatapackDeferredRegister.chemicals(Mekanism.MODID);

    public static final DeferredMapCodecHolder<Chemical, Chemical> BASIC_SERIALIZER = CHEMICALS.registerCodec("basic", () -> ChemicalSerializationHelper.NETWORK_CODEC);

    //TODO - 26.2: Do we want to expose these ids to the api?
    public static final ResourceKey<Chemical> BIO = CHEMICALS.dataKey("bio");
    public static final ResourceKey<Chemical> FUNGI = CHEMICALS.dataKey("fungi");
    public static final ResourceKey<Chemical> TIN = CHEMICALS.dataKey("tin");
    public static final ResourceKey<Chemical> GOLD = CHEMICALS.dataKey("gold");
    public static final ResourceKey<Chemical> REFINED_OBSIDIAN = CHEMICALS.dataKey("refined_obsidian");
    public static final ResourceKey<Chemical> DIAMOND = CHEMICALS.dataKey("diamond");
    public static final ResourceKey<Chemical> REDSTONE = CHEMICALS.dataKey("redstone");
    public static final ResourceKey<Chemical> CARBON = CHEMICALS.dataKey("carbon");

    public static final ResourceKey<Chemical> HYDROGEN = dataKey(ChemicalConstants.HYDROGEN);
    public static final ResourceKey<Chemical> OXYGEN = dataKey(ChemicalConstants.OXYGEN);
    public static final ResourceKey<Chemical> STEAM = CHEMICALS.dataKey("steam");
    public static final ResourceKey<Chemical> WATER_VAPOR = CHEMICALS.dataKey("water_vapor");
    public static final ResourceKey<Chemical> CHLORINE = dataKey(ChemicalConstants.CHLORINE);
    public static final ResourceKey<Chemical> SULFUR_DIOXIDE = dataKey(ChemicalConstants.SULFUR_DIOXIDE);
    public static final ResourceKey<Chemical> SULFUR_TRIOXIDE = dataKey(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final ResourceKey<Chemical> SULFURIC_ACID = dataKey(ChemicalConstants.SULFURIC_ACID);
    public static final ResourceKey<Chemical> HYDROGEN_CHLORIDE = dataKey(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final ResourceKey<Chemical> HYDROFLUORIC_ACID = dataKey(ChemicalConstants.HYDROFLUORIC_ACID);
    public static final ResourceKey<Chemical> URANIUM_OXIDE = dataKey(ChemicalConstants.URANIUM_OXIDE);
    public static final ResourceKey<Chemical> URANIUM_HEXAFLUORIDE = dataKey(ChemicalConstants.URANIUM_HEXAFLUORIDE);
    //Internal gases
    public static final ResourceKey<Chemical> ETHENE = dataKey(ChemicalConstants.ETHENE);
    public static final ResourceKey<Chemical> SODIUM = dataKey(ChemicalConstants.SODIUM);
    public static final ResourceKey<Chemical> SUPERHEATED_SODIUM = dataKey(ChemicalConstants.SUPERHEATED_SODIUM);
    public static final ResourceKey<Chemical> BRINE = CHEMICALS.dataKey("brine");
    public static final ResourceKey<Chemical> LITHIUM = dataKey(ChemicalConstants.LITHIUM);
    public static final ResourceKey<Chemical> OSMIUM = CHEMICALS.dataKey("osmium");
    public static final ResourceKey<Chemical> FISSILE_FUEL = CHEMICALS.dataKey("fissile_fuel");
    public static final ResourceKey<Chemical> NUCLEAR_WASTE = CHEMICALS.dataKey("nuclear_waste");
    public static final ResourceKey<Chemical> SPENT_NUCLEAR_WASTE = CHEMICALS.dataKey("spent_nuclear_waste");
    public static final ResourceKey<Chemical> PLUTONIUM = CHEMICALS.dataKey("plutonium");
    public static final ResourceKey<Chemical> POLONIUM = CHEMICALS.dataKey("polonium");
    public static final ResourceKey<Chemical> ANTIMATTER = CHEMICALS.dataKey("antimatter");

    public static EnumColorCollection<ResourceKey<Chemical>> SIMPLE_PIGMENTS = EnumColorCollection.VALUES.map(color -> CHEMICALS.dataKey(color.getRegistryPrefix()));
    public static final Map<PrimaryResource, CleanDirtySlurryId> PROCESSED_RESOURCES = Util.make(() -> {
        Map<PrimaryResource, CleanDirtySlurryId> slurries = new EnumMap<>(PrimaryResource.class);
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            String baseName = resource.getRegistrySuffix();
            slurries.put(resource, new CleanDirtySlurryId(CHEMICALS.dataKey("clean_" + baseName), CHEMICALS.dataKey("dirty_" + baseName)));
        }
        return Collections.unmodifiableMap(slurries);
    });

    private static ResourceKey<Chemical> dataKey(IChemicalConstant constant) {
        return CHEMICALS.dataKey(constant.getName());
    }

    public static void createAndRegisterDatapack(IEventBus modEventBus) {
        CHEMICALS.createAndRegisterDatapack(modEventBus, ChemicalSerializationHelper.DIRECT_CODEC, ChemicalSerializationHelper.NETWORK_CODEC.codec(),
              registryBuilder -> registryBuilder
                    .defaultKey(MekanismAPI.EMPTY_CHEMICAL_KEY)
                    .onBake(registry -> ((EmptyChemicalResource) ChemicalResource.EMPTY).updateEmptyHolder(registry.getOrThrow(MekanismAPI.EMPTY_CHEMICAL_KEY)))
        );
    }

    private static Registry<Chemical> getChemicalRegistry(RegistryAccess registryAccess) {
        //TODO - 26.2: Just do registryAccess#getOrThrow(key), as it can handle looking up the registry
        return registryAccess.lookupOrThrow(MekanismAPI.CHEMICAL_REGISTRY_NAME);
    }

    public static Reference<Chemical> get(RegistryAccess registryAccess, ResourceKey<Chemical> key) {
        Registry<Chemical> registry = getChemicalRegistry(registryAccess);
        Optional<Reference<Chemical>> value = registry.get(key);
        //noinspection OptionalIsPresent - Capturing lambda
        if (value.isPresent()) {
            return value.get();
        }
        return registry.getOrThrow(MekanismAPI.EMPTY_CHEMICAL_KEY);
    }
}