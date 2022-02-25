package mekanism.tools.common.util;

import java.util.List;
import javax.annotation.Nonnull;
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
    public static void addDurability(@Nonnull List<Component> tooltip, @Nonnull ItemStack stack) {
        if (MekanismToolsConfig.toolsClient.displayDurabilityTooltips.get()) {
            tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamageValue()));
        }
    }
}