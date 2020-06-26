package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.PlacementDeferredRegister;
import mekanism.common.registration.impl.PlacementRegistryObject;
import mekanism.common.world.TopSolidRetrogenPlacement;
import net.minecraft.world.gen.placement.FrequencyConfig;

public class MekanismPlacements {

    public static final PlacementDeferredRegister PLACEMENTS = new PlacementDeferredRegister(Mekanism.MODID);

    public static final PlacementRegistryObject<FrequencyConfig, TopSolidRetrogenPlacement> TOP_SOLID_RETROGEN = PLACEMENTS.register("top_solid_retrogen", () -> new TopSolidRetrogenPlacement(FrequencyConfig.field_236971_a_));
}