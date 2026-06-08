package mekanism.common.item.interfaces;

import java.util.function.Function;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.common.Mekanism;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModeItem {

    /**
     * Changes the current mode of the item
     *
     * @param player        The player who made the mode change.
     * @param itemAccess    The item access representing the item to change the mode of.
     * @param shift         The amount to shift the mode by, may be negative for indicating the mode should decrease.
     * @param displayChange {@code true} if a message should be displayed when the mode changes
     */
    void changeMode(@NotNull Player player, ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction);

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean supportsSlotType(ITEM instance, @NotNull EquipmentSlot slotType) {
        return slotType == EquipmentSlot.MAINHAND || slotType == EquipmentSlot.OFFHAND;
    }

    @Nullable
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getScrollTextComponent(@NotNull ITEM instance) {
        return null;
    }

    static boolean isModeItem(@NotNull Player player, @NotNull EquipmentSlot slotType) {
        return isModeItem(player, slotType, true);
    }

    static boolean isModeItem(@NotNull Player player, @NotNull EquipmentSlot slotType, boolean allowRadial) {
        return isModeItem(player.getItemBySlot(slotType), slotType, allowRadial);
    }

    static <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean isModeItem(@NotNull ITEM instance, @NotNull EquipmentSlot slotType) {
        return isModeItem(instance, slotType, true);
    }

    static <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean isModeItem(@NotNull ITEM instance, @NotNull EquipmentSlot slotType, boolean allowRadial) {
        if (instance.typeHolder().value() instanceof IModeItem modeItem && modeItem.supportsSlotType(instance, slotType)) {
            return allowRadial || !(modeItem instanceof IGenericRadialModeItem radialModeItem) || radialModeItem.getRadialData(instance) == null;
        }
        return false;
    }

    static void displayModeChange(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Mekanism.packetHandler().showModeChange(serverPlayer);
        } else {
            MekanismStatusOverlay.INSTANCE.setTimer();
        }
    }

    enum DisplayChange {
        NONE,
        MAIN_HAND,
        OTHER;

        public <DATA> void sendMessage(Player player, DATA data, Function<DATA, Component> message) {
            if (this == MAIN_HAND) {
                //TODO: Eventually decide if we want to make it so that it checks if IModeItem#getScrollTextComponent is null and otherwise just make it a system message
                displayModeChange(player);
            } else if (this == OTHER) {
                player.sendOverlayMessage(message.apply(data));
            }
        }
    }

    interface IAttachmentBasedModeItem<MODE> extends IModeItem {

        DataComponentType<MODE> getModeDataType();

        MODE getDefaultMode();

        default MODE getMode(ItemAccess itemAccess) {
            return getMode(itemAccess.getResource());
        }

        default <ITEM extends TypedInstance<Item> & DataComponentGetter> MODE getMode(ITEM instance) {
            return instance.getOrDefault(getModeDataType(), getDefaultMode());
        }

        default boolean setMode(ItemAccess itemAccess, Player player, MODE mode, @Nullable TransactionContext transaction) {
            return ItemAccessUtils.exchange(itemAccess, itemAccess.getResource().with(getModeDataType(), mode), transaction);
        }
    }
}