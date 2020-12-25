package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.PortableQIODashboardInventory;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
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

    private PortableQIODashboardContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack, boolean remote) {
        this(id, inv, hand, stack, remote, new PortableQIODashboardInventory(stack, inv));
    }

    /**
     * @apiNote Call from the server
     */
    public PortableQIODashboardContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        this(id, inv, hand, stack, false);
    }

    /**
     * @apiNote Call from the client
     */
    public PortableQIODashboardContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), MekanismItemContainer.getStackFromBuffer(buf, ItemPortableQIODashboard.class), true);
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public PortableQIODashboardContainer recreate() {
        PortableQIODashboardContainer container = new PortableQIODashboardContainer(windowId, inv, hand, stack, true, craftingWindowHolder);
        sync(container);
        return container;
    }

    @Override
    protected HotBarSlot createHotBarSlot(@Nonnull PlayerInventory inv, int index, int x, int y) {
        // special handling to prevent removing the dashboard from the player's inventory slot
        if (index == inv.currentItem && hand == Hand.MAIN_HAND) {
            return new HotBarSlot(inv, index, x, y) {
                @Override
                public boolean canTakeStack(@Nonnull PlayerEntity player) {
                    return false;
                }
            };
        }
        return super.createHotBarSlot(inv, index, x, y);
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull PlayerEntity player) {
        //Block pressing f to swap it when it is in the offhand
        if (clickType == ClickType.SWAP && dragType == 40 && hand == Hand.OFF_HAND) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.OFFHAND);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemPortableQIODashboard) {
                return ItemStack.EMPTY;
            }
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return SecurityUtils.wrapSecurityItem(stack);
    }
}
