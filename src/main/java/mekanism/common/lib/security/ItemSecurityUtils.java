package mekanism.common.lib.security;

import java.util.Objects;
import java.util.function.Consumer;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/// @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via [IItemSecurityUtils#INSTANCE]
public class ItemSecurityUtils implements IItemSecurityUtils {

    private static final ItemCapability<IOwnerObject, ItemAccess> OWNER_CAPABILITY = ItemCapability.create(Capabilities.OWNER_OBJECT_NAME, IOwnerObject.class, ItemAccess.class);
    private static final ItemCapability<ISecurityObject, ItemAccess> SECURITY_CAPABILITY = ItemCapability.create(Capabilities.SECURITY_OBJECT_NAME, ISecurityObject.class, ItemAccess.class);

    public static ItemSecurityUtils get() {
        return (ItemSecurityUtils) INSTANCE;
    }

    @Override
    public ItemCapability<IOwnerObject, ItemAccess> ownerCapability() {
        return OWNER_CAPABILITY;
    }

    @Override
    public ItemCapability<ISecurityObject, ItemAccess> securityCapability() {
        return SECURITY_CAPABILITY;
    }

    @Override
    public void addOwnerTooltip(ItemAccess itemAccess, Consumer<Component> tooltipAdder) {
        Objects.requireNonNull(itemAccess, "Item access to add tooltip for may not be null.");
        Objects.requireNonNull(tooltipAdder, "Tooltip consumer may not be null.");
        IOwnerObject ownerObject = ownerCapability(itemAccess);
        if (ownerObject != null) {
            tooltipAdder.accept(OwnerDisplay.of(MekanismUtils.tryGetClientPlayer(), ownerObject.getOwnerUUID()).getTextComponent());
        }
    }

    @Override
    public void addSecurityTooltip(ItemAccess itemAccess, Consumer<Component> tooltipAdder) {
        addOwnerTooltip(itemAccess, tooltipAdder);
        ISecurityObject security = securityCapability(itemAccess);
        if (security != null) {
            SecurityData data = SecurityUtils.get().getFinalData(security, true);
            tooltipAdder.accept(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, data.mode()));
            if (data.override()) {
                tooltipAdder.accept(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
        }
    }

    public InteractionResult claimOrOpenGui(Level level, Player player, InteractionHand hand, GuiItemOpener openGui) {
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
            if (!tryClaimItem(level, player, itemAccess, transaction)) {
                if (!INSTANCE.canAccessOrDisplayError(player, itemAccess)) {
                    return InteractionResult.FAIL;
                } else if (itemAccess.getAmount() > 1) {
                    //If the item is currently stacked, don't allow opening the GUI
                    return InteractionResult.PASS;
                } else if (!level.isClientSide()) {
                    if (itemAccess.getResource().getItem() instanceof IFrequencyItem frequencyItem) {
                        frequencyItem.pruneInvalidTrusted(itemAccess, transaction);
                    }
                    openGui.open(player, hand, itemAccess, transaction);
                }
            }
            transaction.commit();
            //Transform it in case it got modified (such as part of pruning invalid trusted frequencies)
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
        }
    }

    public boolean tryClaimItem(Level level, Player player, ItemAccess itemAccess, TransactionContext transaction) {
        IOwnerObject ownerObject = ownerCapability(itemAccess);
        if (ownerObject != null && ownerObject.getOwnerUUID() == null) {
            if (!level.isClientSide()) {
                ownerObject.setOwnerUUID(player.getUUID(), transaction);
                PacketDistributor.sendToAllPlayers(new PacketSyncSecurity(player.getUUID()));
                player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.NOW_OWN));
            }
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface GuiItemOpener {

        void open(Player player, InteractionHand hand, ItemAccess itemAccess, TransactionContext transaction);
    }
}