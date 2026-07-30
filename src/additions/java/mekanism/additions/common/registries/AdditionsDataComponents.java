package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemWalkieTalkie.WalkieData;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemTooltipUtils;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;

public class AdditionsDataComponents {

    private AdditionsDataComponents() {
    }

    private static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(MekanismAdditions.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<WalkieData>> WALKIE_DATA = DATA_COMPONENTS.simple("walkie_data",
          WalkieData.CODEC, WalkieData.STREAM_CODEC
    );

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(RegisterTooltipAppendersEvent.class, AdditionsDataComponents::registerTooltipAppenders);
    }

    private static void registerTooltipAppenders(RegisterTooltipAppendersEvent event) {
        event.registerComponentAppenderAfter(WALKIE_DATA, MekanismDataComponents.SCUBA_TANK_MODE.get(), ItemTooltipUtils.createComponentAppender(WALKIE_DATA));
    }
}