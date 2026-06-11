package mekanism.tools.common.util;

import java.util.function.Consumer;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.config.MekanismToolsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ToolsUtils {

    /**
     * Adds durability to the tooltip if enabled in the config
     *
     * @apiNote Only call on client
     */
    public static void addDurability(Consumer<Component> tooltipAdder, ItemStack stack) {
        if (MekanismToolsConfig.toolsClient.displayDurabilityTooltips.get()) {
            tooltipAdder.accept(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamageValue()));
        }
    }
}