package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import mekanism.common.security.IOwnerItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemPortableTeleporter extends ItemEnergized implements IOwnerItem {

    public ItemPortableTeleporter() {
        super("portable_teleporter", 1_000_000);
    }

    public static double calculateEnergyCost(Entity entity, Coord4D coords) {
        if (coords == null) {
            return 0;
        }
        int neededEnergy = MekanismConfig.usage.teleporterBase.get();
        if (entity.world.getDimension().getType().equals(coords.dimension)) {
            int distance = (int) Math.sqrt(entity.getDistanceSq(coords.x, coords.y, coords.z));
            neededEnergy += distance * MekanismConfig.usage.teleporterDistance.get();
        } else {
            neededEnergy += MekanismConfig.usage.teleporterDimensionPenalty.get();
        }
        return neededEnergy;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack)).getTextComponent());
        if (getFrequency(itemstack) != null) {
            tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("gui.mekanism.frequency"), ": ", EnumColor.GRAY, getFrequency(itemstack).name));
            tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("gui.mekanism.mode"), ": ", EnumColor.GRAY,
                  Translation.of("gui." + (!getFrequency(itemstack).publicFreq ? "private" : "public"))));
        }
        super.addInformation(itemstack, world, tooltip, flag);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (getOwnerUUID(stack) == null) {
                setOwnerUUID(stack, player.getUniqueID());
                Mekanism.packetHandler.sendToAll(new PacketSecurityUpdate(SecurityPacket.UPDATE, player.getUniqueID(), null));
                player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY, Translation.of("gui.mekanism.nowOwn")));
            } else if (SecurityUtils.canAccess(player, stack)) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new PortableTeleporterContainer(i, inv, hand, stack)), buf -> {
                    buf.writeEnumValue(hand);
                    buf.writeItemStack(stack);
                });
            } else {
                SecurityUtils.displayNoAccess(player);
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
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