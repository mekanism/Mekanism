package mekanism.generators.common.registries;

import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.core.component.DataComponentType;

public class GeneratorsDataComponents {

    private GeneratorsDataComponents() {
    }

    public static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(MekanismGenerators.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FissionReactorLogic>> FISSION_LOGIC_TYPE = DATA_COMPONENTS.simple("fission_logic",
          builder -> builder.persistent(FissionReactorLogic.CODEC)
                .networkSynchronized(FissionReactorLogic.STREAM_CODEC)
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FusionReactorLogic>> FUSION_LOGIC_TYPE = DATA_COMPONENTS.simple("fusion_logic",
          builder -> builder.persistent(FusionReactorLogic.CODEC)
                .networkSynchronized(FusionReactorLogic.STREAM_CODEC)
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE_COOLED = DATA_COMPONENTS.registerBoolean("active_cooled");
}