package mekanism.common.lib.security;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IItemSecurityUtils#INSTANCE}
 */
@NothingNullByDefault
public class ItemSecurityUtils implements IItemSecurityUtils {

    private static final ItemCapability<IOwnerObject, Void> OWNER_CAPABILITY = ItemCapability.createVoid(Capabilities.OWNER_OBJECT_NAME, IOwnerObject.class);
    private static final ItemCapability<ISecurityObject, Void> SECURITY_CAPABILITY = ItemCapability.createVoid(Capabilities.SECURITY_OBJECT_NAME, ISecurityObject.class);

    public static ItemSecurityUtils get() {
        return (ItemSecurityUtils) INSTANCE;
    }

    @Override
    public ItemCapability<IOwnerObject, Void> ownerCapability() {
        return OWNER_CAPABILITY;
    }

    @Override
    public ItemCapability<ISecurityObject, Void> securityCapability() {
        return SECURITY_CAPABILITY;
    }

    @Override
    public void addOwnerTooltip(ItemStack stack, List<Component> tooltip) {
        Objects.requireNonNull(stack, "Stack to add tooltip for may not be null.");
        Objects.requireNonNull(tooltip, "List of tooltips to add to may not be null.");
        IOwnerObject ownerObject = ownerCapability(stack);
        if (ownerObject != null) {
            tooltip.add(OwnerDisplay.of(MekanismUtils.tryGetClientPlayer(), ownerObject.getOwnerUUID()).getTextComponent());
        }
    }

    @Override
    public void addSecurityTooltip(ItemStack stack, List<Component> tooltip) {
        addOwnerTooltip(stack, tooltip);
        ISecurityObject security = securityCapability(stack);
        if (security != null) {
            SecurityData data = SecurityUtils.get().getFinalData(security, true);
            tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, data.mode()));
            if (data.override()) {
                tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
        }
    }

    public InteractionResultHolder<ItemStack> claimOrOpenGui(Level level, Player player, InteractionHand hand, TriConsumer<ServerPlayer, InteractionHand, ItemStack> openGui) {
        ItemStack stack = player.getItemInHand(hand);
        if (!tryClaimItem(level, player, stack)) {
            if (!INSTANCE.canAccessOrDisplayError(player, stack)) {
                return InteractionResultHolder.fail(stack);
            } else if (stack.getCount() > 1) {
                //If the item is currently stacked, don't allow opening the GUI
                return InteractionResultHolder.pass(stack);
            } else if (!level.isClientSide) {
                openGui.accept((ServerPlayer) player, hand, stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public boolean tryClaimItem(Level level, Player player, ItemStack stack) {
        IOwnerObject ownerObject = ownerCapability(stack);
        if (ownerObject != null && ownerObject.getOwnerUUID() == null) {
            if (!level.isClientSide) {
                ownerObject.setOwnerUUID(player.getUUID());
                PacketDistributor.sendToAllPlayers(new PacketSyncSecurity(player.getUUID()));
                player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.NOW_OWN));
            }
            return true;
        }
        return false;
    }
}