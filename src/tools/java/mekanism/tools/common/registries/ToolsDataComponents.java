package mekanism.tools.common.registries;

import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import mekanism.common.util.ItemTooltipUtils;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.config.MekanismToolsConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;

public class ToolsDataComponents {

    private ToolsDataComponents() {
    }

    private static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(MekanismTools.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Unit>> DISPLAY_HP = DATA_COMPONENTS.registerUnit("hp");

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(RegisterTooltipAppendersEvent.class, ToolsDataComponents::registerTooltipAppenders);
    }

    private static void registerTooltipAppenders(RegisterTooltipAppendersEvent event) {
        event.registerComponentAppenderBefore(DISPLAY_HP, DataComponents.UNBREAKABLE, ItemTooltipUtils.createSimpleAppender(DISPLAY_HP, (stack, _, _, _, _, _, builder) -> {
            if (MekanismToolsConfig.toolsClient.displayDurabilityTooltips.get() && stack.isDamageableItem()) {
                builder.accept(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamageValue()));
            }
        }));
    }
}