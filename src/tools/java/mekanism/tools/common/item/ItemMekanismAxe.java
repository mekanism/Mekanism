package mekanism.tools.common.item;

import java.util.function.Consumer;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemMekanismAxe extends AxeItem implements IsMekanismTool {

    public ItemMekanismAxe(MaterialCreator material, Item.Properties properties) {
        //super(material, properties.attributes(createAttributes(material, material.getAxeDamage(), material.getAxeAtkSpeed())));
        super(material.toToolMaterial(), material.getAxeDamage(), material.getAxeAtkSpeed(), properties);
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        ToolsUtils.addDurability(tooltipAdder, stack);
    }
}