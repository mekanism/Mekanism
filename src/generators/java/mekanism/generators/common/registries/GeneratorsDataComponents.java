package mekanism.generators.common.registries;

import mekanism.api.chemical.ChemicalResource;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemTooltipUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

public class GeneratorsDataComponents {

    private GeneratorsDataComponents() {
    }

    private static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(MekanismGenerators.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FissionReactorLogic>> FISSION_LOGIC_TYPE = DATA_COMPONENTS.simple("fission_logic",
          FissionReactorLogic.CODEC, FissionReactorLogic.STREAM_CODEC
    );
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<FusionReactorLogic>> FUSION_LOGIC_TYPE = DATA_COMPONENTS.simple("fusion_logic",
          FusionReactorLogic.CODEC, FusionReactorLogic.STREAM_CODEC
    );

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE_COOLED = DATA_COMPONENTS.registerBoolean("active_cooled");

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Unit>> REACTION_STARTER = DATA_COMPONENTS.registerUnit("reaction_starter");

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(RegisterTooltipAppendersEvent.class, GeneratorsDataComponents::registerTooltipAppenders);
    }

    private static void registerTooltipAppenders(RegisterTooltipAppendersEvent event) {
        event.registerComponentAppenderAfter(FISSION_LOGIC_TYPE, MekanismDataComponents.FACTORY_TYPE.get(), ItemTooltipUtils.createTrivialAppender(FISSION_LOGIC_TYPE,
              logicType -> GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(logicType.getColor(), logicType)
        ));
        event.registerComponentAppenderAfter(FUSION_LOGIC_TYPE, FISSION_LOGIC_TYPE.get(), ItemTooltipUtils.createTrivialAppender(FUSION_LOGIC_TYPE,
              logicType -> GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(logicType.getColor(), logicType)
        ));
        event.registerComponentAppenderAfter(ACTIVE_COOLED, FUSION_LOGIC_TYPE.get(), ItemTooltipUtils.createTrivialAppender(ACTIVE_COOLED,
              activeCooled -> GeneratorsLang.REACTOR_LOGIC_ACTIVE_COOLING.translate(EnumColor.RED, OnOff.of(activeCooled))
        ));
        event.registerComponentAppenderAfter(REACTION_STARTER, MekanismDataComponents.ATTACHED_CHEMICALS.get(), ItemTooltipUtils.createSimpleAppender(REACTION_STARTER, (stack, _, _, _, _, _, builder) -> {
            //Display hohlraum's ready for reaction *after* the stored chemical
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getQueryOnlyCapability(stack);
            if (handler != null && ResourceHandlerUtil.isFull(handler)) {
                builder.accept(GeneratorsLang.READY_FOR_REACTION.translateColored(EnumColor.DARK_GREEN));
            } else {
                builder.accept(GeneratorsLang.INSUFFICIENT_FUEL.translateColored(EnumColor.DARK_RED));
            }
        }));
    }
}