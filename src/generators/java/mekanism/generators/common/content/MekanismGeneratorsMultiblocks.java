package mekanism.generators.common.content;

import mekanism.common.lib.multiblock.MekanismMultiblockRegistry;
import mekanism.common.lib.multiblock.MultiblockType;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorCache;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator;
import mekanism.generators.common.content.fusion.FusionReactorCache;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.content.fusion.FusionReactorValidator;
import mekanism.generators.common.content.turbine.TurbineCache;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineValidator;
import net.neoforged.bus.api.IEventBus;

public class MekanismGeneratorsMultiblocks {

    private static final MekanismMultiblockRegistry REGISTRY = new MekanismMultiblockRegistry(MekanismGenerators.MODID);

    public static final MultiblockType<TurbineMultiblockData> TURBINE = REGISTRY.registerMultiblock("industrial_turbine", TurbineCache::new, TurbineValidator::new);
    public static final MultiblockType<FissionReactorMultiblockData> FISSION_REACTOR = REGISTRY.registerMultiblock("fission_reactor", FissionReactorCache::new, FissionReactorValidator::new);
    public static final MultiblockType<FusionReactorMultiblockData> FUSION_REACTOR = REGISTRY.registerMultiblock("fusion_reactor", FusionReactorCache::new, FusionReactorValidator::new);

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
