package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.PlacementDeferredRegister;
import mekanism.common.registration.impl.PlacementRegistryObject;
import mekanism.common.world.AdjustableCountPlacement;
import mekanism.common.world.AdjustableSpreadConfig;
import mekanism.common.world.ResizableRangePlacement;
import mekanism.common.world.ResizableTopSolidRangeConfig;
import mekanism.common.world.TopSolidRetrogenPlacement;
import net.minecraft.world.gen.placement.NoPlacementConfig;

public class MekanismPlacements {

    private MekanismPlacements() {
    }

    public static final PlacementDeferredRegister PLACEMENTS = new PlacementDeferredRegister(Mekanism.MODID);

    public static final PlacementRegistryObject<AdjustableSpreadConfig, AdjustableCountPlacement> ADJUSTABLE_COUNT = PLACEMENTS.register("adjustable_count", () -> new AdjustableCountPlacement(AdjustableSpreadConfig.CODEC));
    public static final PlacementRegistryObject<ResizableTopSolidRangeConfig, ResizableRangePlacement> RESIZABLE_RANGE = PLACEMENTS.register("resizable_range", () -> new ResizableRangePlacement(ResizableTopSolidRangeConfig.CODEC));
    public static final PlacementRegistryObject<NoPlacementConfig, TopSolidRetrogenPlacement> TOP_SOLID_RETROGEN = PLACEMENTS.register("top_solid_retrogen", () -> new TopSolidRetrogenPlacement(NoPlacementConfig.CODEC));
}