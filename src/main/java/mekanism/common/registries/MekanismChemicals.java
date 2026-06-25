package mekanism.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalResource.EmptyChemicalResource;
import mekanism.api.chemical.ChemicalSerializationHelper;
import mekanism.api.chemical.CleanDirtySlurryId;
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

    //TODO - 26.2: Do we want to expose these to the API?
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
                    .defaultKey(ChemicalIds.EMPTY)
                    .onBake(registry -> ((EmptyChemicalResource) ChemicalResource.EMPTY).updateEmptyHolder(registry.getOrThrow(ChemicalIds.EMPTY)))
        );
    }

    private static Registry<Chemical> getChemicalRegistry(RegistryAccess registryAccess) {
        //TODO - 26.2: Just do registryAccess#getOrThrow(key), as it can handle looking up the registry
        return registryAccess.lookupOrThrow(MekanismRegistries.Keys.CHEMICAL);
    }

    public static Reference<Chemical> get(RegistryAccess registryAccess, ResourceKey<Chemical> key) {
        Registry<Chemical> registry = getChemicalRegistry(registryAccess);
        Optional<Reference<Chemical>> value = registry.get(key);
        //noinspection OptionalIsPresent - Capturing lambda
        if (value.isPresent()) {
            return value.get();
        }
        return registry.getOrThrow(ChemicalIds.EMPTY);
    }
}