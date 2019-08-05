package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import mekanism.common.security.IOwnerItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPortableTeleporter extends ItemEnergized implements IOwnerItem {

    public ItemPortableTeleporter() {
        super("portable_teleporter", 1_000_000);
    }

    public static double calculateEnergyCost(Entity entity, Coord4D coords) {
        if (coords == null) {
            return 0;
        }
        int neededEnergy = MekanismConfig.current().usage.teleporterBase.val();
        if (entity.world.provider.getDimension() != coords.dimensionId) {
            neededEnergy += MekanismConfig.current().usage.teleporterDimensionPenalty.val();
        } else {
            int distance = (int) entity.getDistance(coords.x, coords.y, coords.z);
            neededEnergy += distance * MekanismConfig.current().usage.teleporterDistance.val();
        }
        return neededEnergy;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        if (getFrequency(itemstack) != null) {
            list.add(EnumColor.INDIGO + LangUtils.localize("gui.frequency") + ": " + EnumColor.GREY + getFrequency(itemstack).name);
            list.add(EnumColor.INDIGO + LangUtils.localize("gui.mode") + ": " + EnumColor.GREY +
                     LangUtils.localize("gui." + (!getFrequency(itemstack).publicFreq ? "private" : "public")));
        }
        super.addInformation(itemstack, world, list, flag);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, @Nonnull EnumHand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (!world.isRemote) {
            if (getOwnerUUID(itemstack) == null) {
                setOwnerUUID(itemstack, entityplayer.getUniqueID());
                Mekanism.packetHandler.sendToAll(new SecurityUpdateMessage(SecurityPacket.UPDATE, entityplayer.getUniqueID(), null));
                entityplayer.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY + LangUtils.localize("gui.nowOwn")));
            } else if (SecurityUtils.canAccess(entityplayer, itemstack)) {
                MekanismUtils.openItemGui(entityplayer, hand, 14);
            } else {
                SecurityUtils.displayNoAccess(entityplayer);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public void setOwnerUUID(@Nonnull ItemStack stack, @Nullable UUID owner) {
        setFrequency(stack, null);
        //TODO: Should setFrequency be pulled out of this method and then it can just use default impl from the interface
        if (owner == null) {
            ItemDataUtils.removeData(stack, "ownerUUID");
        } else {
            ItemDataUtils.setString(stack, "ownerUUID", owner.toString());
        }
    }

    public Frequency.Identity getFrequency(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, "frequency")) {
            return Frequency.Identity.load(ItemDataUtils.getCompound(stack, "frequency"));
        }
        return null;
    }

    public void setFrequency(ItemStack stack, Frequency frequency) {
        if (frequency == null) {
            ItemDataUtils.removeData(stack, "frequency");
            return;
        }
        ItemDataUtils.setCompound(stack, "frequency", frequency.getIdentity().serialize());
    }
}