package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.frequency.IFrequencyItem;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemPortableItemDashboard extends Item implements IFrequencyItem {

    public ItemPortableItemDashboard(Properties properties) {
        super(properties.maxStackSize(1));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (getOwnerUUID(stack) == null) {
                setOwnerUUID(stack, player.getUniqueID());
                Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(player.getUniqueID(), null));
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, MekanismLang.NOW_OWN.translateColored(EnumColor.GRAY)));
            } else if (SecurityUtils.canAccess(player, stack)) {
                // open GUI
            } else {
                SecurityUtils.displayNoAccess(player);
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
