package mekanism.tools.common.item;

import java.util.function.Consumer;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemMekanismHoe extends HoeItem implements IsMekanismTool {

    public ItemMekanismHoe(MaterialCreator material, Item.Properties properties) {
        super(material.toToolMaterial(), material.getHoeDamage(), material.getHoeAtkSpeed(), properties);
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        ToolsUtils.addDurability(tooltipAdder, stack);
    }
}