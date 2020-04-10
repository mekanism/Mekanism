package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.frequency.Frequency;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.security.IOwnerItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemPortableTeleporter extends ItemEnergized implements IOwnerItem {

    private static final FloatingLong MAX_ENERGY = FloatingLong.createConst(1_000_000);//TODO: Config

    public ItemPortableTeleporter(Properties properties) {
        super(MAX_ENERGY, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        if (getFrequency(stack) != null) {
            tooltip.add(MekanismLang.FREQUENCY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getFrequency(stack).name));
            tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, !getFrequency(stack).publicFreq ? MekanismLang.PRIVATE : MekanismLang.PUBLIC));
        }
        super.addInformation(stack, world, tooltip, flag);
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
    public void setOwnerUUID(@Nonnull ItemStack stack, @Nullable UUID owner) {
        setFrequency(stack, null);
        //TODO: Should setFrequency be pulled out of this method and then it can just use default impl from the interface
        if (owner == null) {
            ItemDataUtils.removeData(stack, NBTConstants.OWNER_UUID);
        } else {
            ItemDataUtils.setUUID(stack, NBTConstants.OWNER_UUID, owner);
        }
    }

    public Frequency.Identity getFrequency(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.FREQUENCY, NBT.TAG_COMPOUND)) {
            return Frequency.Identity.load(ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY));
        }
        return null;
    }

    public void setFrequency(ItemStack stack, Frequency frequency) {
        if (frequency == null) {
            ItemDataUtils.removeData(stack, NBTConstants.FREQUENCY);
        } else {
            ItemDataUtils.setCompound(stack, NBTConstants.FREQUENCY, frequency.getIdentity().serialize());
        }
    }
}