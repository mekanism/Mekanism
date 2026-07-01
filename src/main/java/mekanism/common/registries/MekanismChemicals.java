package mekanism.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalResource.EmptyChemicalResource;
import mekanism.api.chemical.ChemicalSerializationHelper;
import mekanism.api.chemical.ChemicalSerializer;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.common.Mekanism;
import mekanism.common.chemical.EnumColorPigment;
import mekanism.common.registration.AdvancedDatapackDeferredRegister;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;

public class MekanismChemicals {

    private MekanismChemicals() {
    }

    private static final AdvancedDatapackDeferredRegister<Chemical, ChemicalSerializer> CHEMICALS = AdvancedDatapackDeferredRegister.chemicals(Mekanism.MODID);

    public static final MekanismDeferredHolder<ChemicalSerializer, ChemicalSerializer> BASIC_SERIALIZER = CHEMICALS.register("basic", () -> ChemicalSerializer.defaultNetwork(ChemicalSerializationHelper.DEFAULT_NETWORK_CODEC));
    public static final MekanismDeferredHolder<ChemicalSerializer, ChemicalSerializer> ENUM_COLOR_PIGMENT = CHEMICALS.register("enum_color_pigment", () -> ChemicalSerializer.both(EnumColorPigment.CODEC));

    public static final Map<PrimaryResource, CleanDirtySlurryId> PROCESSED_RESOURCES = Util.make(() -> {
        Map<PrimaryResource, CleanDirtySlurryId> slurries = new EnumMap<>(PrimaryResource.class);
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            String baseName = resource.getRegistrySuffix();
            slurries.put(resource, new CleanDirtySlurryId(CHEMICALS.dataKey("clean_" + baseName), CHEMICALS.dataKey("dirty_" + baseName)));
        }
        return Collections.unmodifiableMap(slurries);
    });

    public static void createAndRegisterDatapack(IEventBus modEventBus) {
        CHEMICALS.createAndRegisterDatapack(modEventBus, ChemicalSerializationHelper.DIRECT_CODEC, ChemicalSerializationHelper.NETWORK_CODEC,
              registryBuilder -> registryBuilder
                    .defaultKey(ChemicalIds.EMPTY)
                    .onBake(registry -> ((EmptyChemicalResource) ChemicalResource.EMPTY).updateEmptyHolder(registry.getOrThrow(ChemicalIds.EMPTY)))
        );
    }
}