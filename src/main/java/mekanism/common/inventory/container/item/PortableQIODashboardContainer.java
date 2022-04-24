package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.PortableQIODashboardInventory;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class PortableQIODashboardContainer extends QIOItemViewerContainer {

    protected final InteractionHand hand;
    protected final ItemStack stack;

    private PortableQIODashboardContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack, boolean remote, IQIOCraftingWindowHolder craftingWindowHolder) {
        super(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, id, inv, remote, craftingWindowHolder);
        this.hand = hand;
        this.stack = stack;
        addSlotsAndOpen();
    }

    public PortableQIODashboardContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack, boolean remote) {
        this(id, inv, hand, stack, remote, new PortableQIODashboardInventory(stack, inv));
    }

    public InteractionHand getHand() {
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
    protected HotBarSlot createHotBarSlot(@Nonnull Inventory inv, int index, int x, int y) {
        // special handling to prevent removing the dashboard from the player's inventory slot
        if (index == inv.selected && hand == InteractionHand.MAIN_HAND) {
            return new HotBarSlot(inv, index, x, y) {
                @Override
                public boolean mayPickup(@Nonnull Player player) {
                    return false;
                }
            };
        }
        return super.createHotBarSlot(inv, index, x, y);
    }

    @Override
    public void clicked(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull Player player) {
        if (clickType == ClickType.SWAP) {
            if (hand == InteractionHand.OFF_HAND && dragType == 40) {
                //Block pressing f to swap it when it is in the offhand
                return;
            } else if (hand == InteractionHand.MAIN_HAND && dragType >= 0 && dragType < Inventory.getSelectionSize()) {
                //Block taking out of the selected slot (we don't validate we have a hotbar slot as we always should for this container)
                if (!hotBarSlots.get(dragType).mayPickup(player)) {
                    return;
                }
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Nullable
    @Override
    public ICapabilityProvider getSecurityObject() {
        return stack;
    }
}
