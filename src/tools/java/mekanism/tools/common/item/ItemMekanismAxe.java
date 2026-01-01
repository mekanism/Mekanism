package mekanism.tools.common.item;

import java.util.List;
import java.util.function.Consumer;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

public class ItemMekanismAxe extends AxeItem {

    public ItemMekanismAxe(MaterialCreator material, Item.Properties properties) {
        super(material, properties.attributes(createAttributes(material, material.getAxeDamage(), material.getAxeAtkSpeed())));
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        ToolsUtils.addDurability(tooltipAdder, stack);
    }
}