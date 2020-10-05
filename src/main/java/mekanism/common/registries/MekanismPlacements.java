package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.PlacementDeferredRegister;
import mekanism.common.registration.impl.PlacementRegistryObject;
import mekanism.common.world.TopSolidRetrogenPlacement;
import net.minecraft.world.gen.placement.NoPlacementConfig;

public class MekanismPlacements {

    private MekanismPlacements() {
    }

    public static final PlacementDeferredRegister PLACEMENTS = new PlacementDeferredRegister(Mekanism.MODID);

    public static final PlacementRegistryObject<NoPlacementConfig, TopSolidRetrogenPlacement> TOP_SOLID_RETROGEN = PLACEMENTS.register("top_solid_retrogen", () -> new TopSolidRetrogenPlacement(NoPlacementConfig.field_236555_a_));
}