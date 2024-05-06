package mekanism.tools.common.item;

import java.util.List;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.tools.common.integration.gender.ToolsGenderCapabilityHelper;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class ItemMekanismArmor extends ArmorItem implements ICapabilityAware {

    public ItemMekanismArmor(Holder<ArmorMaterial> material, ArmorItem.Type armorType, Item.Properties properties) {
        super(material, armorType, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        ToolsGenderCapabilityHelper.addGenderCapability(this, event);
    }
}