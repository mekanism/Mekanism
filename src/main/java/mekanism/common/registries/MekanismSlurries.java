package mekanism.common.registries;

import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.SlurryDeferredRegister;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.EnumUtils;

public class MekanismSlurries {

    private MekanismSlurries() {
    }

    public static final SlurryDeferredRegister SLURRIES = new SlurryDeferredRegister(Mekanism.MODID);

    public static final Map<PrimaryResource, SlurryRegistryObject<Slurry, Slurry>> PROCESSED_RESOURCES = new LinkedHashMap<>();

    static {
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            PROCESSED_RESOURCES.put(resource, SLURRIES.register(resource));
        }
    }
}