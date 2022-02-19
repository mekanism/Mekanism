package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.PortableQIODashboardInventory;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class PortableQIODashboardContainer extends QIOItemViewerContainer {

    protected final Hand hand;
    protected final ItemStack stack;

    private PortableQIODashboardContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack, boolean remote, IQIOCraftingWindowHolder craftingWindowHolder) {
        super(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, id, inv, remote, craftingWindowHolder);
        this.hand = hand;
        this.stack = stack;
        addSlotsAndOpen();
    }

    public PortableQIODashboardContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack, boolean remote) {
        this(id, inv, hand, stack, remote, new PortableQIODashboardInventory(stack, inv));
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public PortableQIODashboardContainer recreate() {
        PortableQIODashboardContainer container = new PortableQIODashboardContainer(containerId, inv, hand, stack, true, craftingWindowHolder);
        sync(container);
        return container;
    }

    @Override
    protected HotBarSlot createHotBarSlot(@Nonnull PlayerInventory inv, int index, int x, int y) {
        // special handling to prevent removing the dashboard from the player's inventory slot
        if (index == inv.selected && hand == Hand.MAIN_HAND) {
            return new HotBarSlot(inv, index, x, y) {
                @Override
                public boolean mayPickup(@Nonnull PlayerEntity player) {
                    return false;
                }
            };
        }
        return super.createHotBarSlot(inv, index, x, y);
    }

    @Nonnull
    @Override
    public ItemStack clicked(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull PlayerEntity player) {
        if (clickType == ClickType.SWAP) {
            if (hand == Hand.OFF_HAND && dragType == 40) {
                //Block pressing f to swap it when it is in the offhand
                return ItemStack.EMPTY;
            } else if (hand == Hand.MAIN_HAND && dragType >= 0 && dragType < PlayerInventory.getSelectionSize()) {
                //Block taking out of the selected slot (we don't validate we have a hotbar slot as we always should for this container)
                if (!hotBarSlots.get(dragType).mayPickup(player)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return SecurityUtils.wrapSecurityItem(stack);
    }
}
