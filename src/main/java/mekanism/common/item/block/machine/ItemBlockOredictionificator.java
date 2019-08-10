package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.block.machine.BlockOredictionificator;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockOredictionificator extends ItemBlockAdvancedTooltip<BlockOredictionificator> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockOredictionificator(BlockOredictionificator block) {
        super(block, new Item.Properties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(SecurityUtils.getOwnerDisplay(Minecraft.getInstance().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        tooltip.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Dist.CLIENT));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
        }
        tooltip.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                 LangUtils.transYesNo(getInventory(itemstack) != null && !getInventory(itemstack).isEmpty()));
    }
}