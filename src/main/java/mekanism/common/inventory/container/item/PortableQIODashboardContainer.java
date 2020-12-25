package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.PortableQIODashboardInventory;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class PortableQIODashboardContainer extends QIOItemViewerContainer {

    protected final Hand hand;
    protected final ItemStack stack;

    private PortableQIODashboardContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack, boolean remote) {
        super(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, id, inv, remote, new PortableQIODashboardInventory(stack, inv == null ? null : inv.player.getEntityWorld()));
        this.hand = hand;
        this.stack = stack;
        addSlotsAndOpen();
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
        PortableQIODashboardContainer container = new PortableQIODashboardContainer(windowId, inv, hand, stack);
        sync(container);
        return container;
    }

    @Override
    public QIOFrequency getFrequency() {
        if (!inv.player.world.isRemote()) {
            FrequencyIdentity identity = ((IFrequencyItem) stack.getItem()).getFrequency(stack);
            if (identity == null) {
                return null;
            }
            FrequencyManager<QIOFrequency> manager = identity.isPublic() ? FrequencyType.QIO.getManager(null) : FrequencyType.QIO.getManager(inv.player.getUniqueID());
            QIOFrequency freq = manager.getFrequency(identity.getKey());
            // if this frequency no longer exists, remove the reference from the stack
            if (freq == null) {
                ((IFrequencyItem) stack.getItem()).setFrequency(stack, null);
            }
            return freq;
        }
        return null;
    }

    @Override
    protected HotBarSlot createHotBarSlot(@Nonnull PlayerInventory inv, int index, int x, int y) {
        // special handling to prevent removing the dashboard from the player's inventory slot
        if (index == inv.currentItem) {
            return new HotBarSlot(inv, index, x, y) {
                @Override
                public boolean canTakeStack(@Nonnull PlayerEntity player) {
                    return false;
                }
            };
        }
        return super.createHotBarSlot(inv, index, x, y);
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return SecurityUtils.wrapSecurityItem(stack);
    }
}
